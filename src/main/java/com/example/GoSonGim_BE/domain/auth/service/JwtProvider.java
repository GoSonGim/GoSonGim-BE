package com.example.GoSonGim_BE.domain.auth.service;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.example.GoSonGim_BE.domain.auth.entity.RefreshToken;
import com.example.GoSonGim_BE.domain.auth.entity.RevokedReason;
import com.example.GoSonGim_BE.domain.auth.exception.AuthExceptions;
import com.example.GoSonGim_BE.domain.auth.repository.RefreshTokenRepository;
import com.example.GoSonGim_BE.domain.users.entity.User;
import com.example.GoSonGim_BE.domain.users.service.UserService;
import com.example.GoSonGim_BE.global.config.JwtProperties;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtProvider {
    private final JwtProperties jwtProperties;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserService userService;
    private SecretKey secretKey;

    @PostConstruct
    void init() {
      byte[] keyBytes = jwtProperties.getSecret().getBytes(Charset.forName("UTF-8"));
      this.secretKey = Keys.hmacShaKeyFor(keyBytes);
    }
    
    /**
     * JwtProperties Getter
     */
    public JwtProperties getJwtProperties() {
        return jwtProperties;
    }

    /**
     * Access Token 생성
     * 
     * @param userId 사용자 ID
     * @return JWT Access Token
     */
    public String generateAccessToken(Long userId) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + jwtProperties.getAccessToken().getExpiration()); 

        return Jwts.builder()
            .subject(userId.toString())
            .issuedAt(now)
            .expiration(expiration)
            .signWith(secretKey)
            .compact();
    }

    /**
     * Refresh Token 생성 및 DB 저장
     * 
     * @param userId 사용자 ID
     * @return RefreshToken JWT 문자열
     */
    public String generateRefreshToken(Long userId) {
        // 1. 기존 활성 토큰이 있으면 소프트 폐기 처리
        refreshTokenRepository.findByUserIdAndRevokedAtIsNull(userId).ifPresent(active -> {
            active.revoke(RevokedReason.ROTATED);
            refreshTokenRepository.save(active);
        });
        
        // 2. JTI 생성
        String jti = UUID.randomUUID().toString();
        
        // 3. Refresh Token JWT 생성
        Date now = new Date();
        Date expiration = new Date(now.getTime() + jwtProperties.getRefreshToken().getExpiration());
        
        String tokenValue = Jwts.builder()
            .subject(String.valueOf(userId))
            .id(jti)
            .issuedAt(now)
            .expiration(expiration)
            .signWith(secretKey)
            .compact();
        
        // 4. DB 저장
        saveRefreshTokenToDb(userId, jti, tokenValue);
        
        // 5. JWT 문자열 반환
        return tokenValue;
    }

    /**
     * 토큰 해시 생성 (SHA-256)
     * 
     * @param token 원본 토큰
     * @return Base64 인코딩된 해시값
     */
    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 알고리즘을 찾을 수 없습니다.", e);
        }
    }

    /**
     * Refresh Token DB 저장
     * 
     * @param userId 사용자 ID
     * @param jti JWT ID
     * @param tokenValue Refresh Token JWT 문자열
     */
    private void saveRefreshTokenToDb(Long userId, String jti, String tokenValue) {
        String tokenHash = hashToken(tokenValue);
        long expirationMillis = jwtProperties.getRefreshToken().getExpiration();
        LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(expirationMillis / 1000);
        
        User user = userService.findById(userId);
        
        RefreshToken refreshToken = RefreshToken.builder()
            .user(user)
            .jti(jti)
            .tokenHash(tokenHash)
            .expiresAt(expiresAt)
            .build();
        
        refreshTokenRepository.save(refreshToken);
    }

    /**
     * 토큰 검증
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * 토큰에서 Claims 추출
     */
    private Claims getClaims(String token) {
        return Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }

    /**
     * 토큰에서 userId 추출
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = getClaims(token);
        return Long.parseLong(claims.getSubject());
    }

    /**
     * 토큰에서 JTI 추출
     * 
     * @param token JWT 토큰
     * @return JTI
     */
    public String getJtiFromToken(String token) {
        Claims claims = getClaims(token);
        return claims.getId();
    }

    /**
     * Refresh Token 검증 및 조회
     * 
     * @param refreshTokenValue JWT 문자열
     * @return RefreshToken 엔티티
     * @throws AuthExceptions 검증 실패 시
     */
    public RefreshToken validateAndGetRefreshToken(String refreshTokenValue) {
        // 1. JWT 검증
        if (!validateToken(refreshTokenValue)) {
            throw new AuthExceptions.InvalidRefreshTokenException();
        }
        
        // 2. JTI 추출
        String jti = getJtiFromToken(refreshTokenValue);
        
        // 3. DB에서 조회
        RefreshToken refreshToken = refreshTokenRepository.findByJti(jti)
            .orElseThrow(() -> new AuthExceptions.RefreshTokenNotFoundException());
        
        // 4. 폐기 여부 확인
        if (refreshToken.isRevoked()) {
            throw new AuthExceptions.RefreshTokenRevokedException();
        }
        
        // 5. 만료 여부 확인
        if (refreshToken.isExpired()) {
            throw new AuthExceptions.RefreshTokenExpiredException();
        }
        
        // 6. 토큰 해시 검증
        String tokenHash = hashToken(refreshTokenValue);
        if (!tokenHash.equals(refreshToken.getTokenHash())) {
            throw new AuthExceptions.InvalidRefreshTokenException();
        }
        
        return refreshToken;
    }

    /**
     * Refresh Token 무효화 및 새 토큰 발급
     * 
     * @param oldRefreshToken 기존 Refresh Token 엔티티
     * @return 새로운 JWT 문자열
     */
    @Transactional
    public String rotateRefreshToken(RefreshToken oldRefreshToken) {
        // 1. 기존 토큰 무효화
        oldRefreshToken.revoke(RevokedReason.ROTATED);
        refreshTokenRepository.save(oldRefreshToken);
        
        // 2. 새 토큰 발급
        Long userId = oldRefreshToken.getUser().getId();
        return generateRefreshToken(userId);
    }
    
    /**
     * Refresh Token 로그아웃 처리 (폐기)
     * - 이미 폐기된 토큰이면 RefreshTokenRevokedException 발생
     * - 유효하지 않은 토큰이면 해당 예외 발생
     * 
     * @param refreshTokenValue 로그아웃할 Refresh Token JWT 문자열
     * @throws AuthExceptions.RefreshTokenRevokedException 이미 폐기된 토큰
     * @throws AuthExceptions.InvalidRefreshTokenException 유효하지 않은 토큰
     * @throws AuthExceptions.RefreshTokenExpiredException 만료된 토큰
     * @throws AuthExceptions.RefreshTokenNotFoundException 존재하지 않는 토큰
     */
    @Transactional
    public void revokeRefreshTokenForLogout(String refreshTokenValue) {
        // 1. Refresh Token 검증 및 조회 (예외 발생 시 그대로 throw)
        RefreshToken refreshToken = validateAndGetRefreshToken(refreshTokenValue);
        
        // 2. LOGOUT 이유로 폐기
        refreshToken.revoke(RevokedReason.LOGOUT);
        refreshTokenRepository.save(refreshToken);
    }
}
