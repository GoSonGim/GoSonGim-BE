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
        
        // 2. 새 토큰 생성
        String jti = UUID.randomUUID().toString();
        String tokenValue = buildRefreshTokenJwt(userId, jti);
        
        // 3. DB 저장
        saveRefreshTokenToDb(userId, jti, tokenValue);
        
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
        LocalDateTime expiresAt = calculateRefreshTokenExpiration();
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
     * Refresh Token 회전 (Rotation)
     * - 도메인 로직을 RefreshToken 엔티티에 위임
     * 
     * @param oldRefreshToken 기존 Refresh Token 엔티티
     * @return 새로운 JWT 문자열
     */
    @Transactional
    public String rotateRefreshToken(RefreshToken oldRefreshToken) {
        Long userId = oldRefreshToken.getUser().getId();
        
        // 1. 새 토큰 생성 (JWT 문자열, JTI, 해시, 만료시각)
        String newJti = UUID.randomUUID().toString();
        String newTokenValue = buildRefreshTokenJwt(userId, newJti);
        String newTokenHash = hashToken(newTokenValue);
        LocalDateTime newExpiresAt = calculateRefreshTokenExpiration();
        
        // 2. 도메인 로직 실행: 기존 토큰 폐기 + 새 토큰 생성
        RefreshToken newRefreshToken = oldRefreshToken.rotate(newJti, newTokenHash, newExpiresAt);
        
        // 3. 영속화
        refreshTokenRepository.save(oldRefreshToken);
        refreshTokenRepository.save(newRefreshToken);
        
        return newTokenValue;
    }
    
    /**
     * Refresh Token JWT 문자열 생성 (내부 헬퍼)
     */
    private String buildRefreshTokenJwt(Long userId, String jti) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + jwtProperties.getRefreshToken().getExpiration());
        
        return Jwts.builder()
            .subject(String.valueOf(userId))
            .id(jti)
            .issuedAt(now)
            .expiration(expiration)
            .signWith(secretKey)
            .compact();
    }
    
    /**
     * Refresh Token 만료 시각 계산 (내부 헬퍼)
     */
    private LocalDateTime calculateRefreshTokenExpiration() {
        return LocalDateTime.now()
            .plusSeconds(jwtProperties.getRefreshToken().getExpiration() / 1000);
    }
    
    /**
     * Refresh Token 로그아웃 처리 (폐기)
     * - 이미 폐기된 토큰이면 RefreshTokenRevokedException 발생
     * 
     * @param refreshTokenValue 로그아웃할 Refresh Token JWT 문자열
     * @throws AuthExceptions.RefreshTokenRevokedException 이미 폐기된 토큰
     * @throws AuthExceptions.InvalidRefreshTokenException 유효하지 않은 토큰
     * @throws AuthExceptions.RefreshTokenExpiredException 만료된 토큰
     * @throws AuthExceptions.RefreshTokenNotFoundException 존재하지 않는 토큰
     */
    @Transactional
    public void revokeRefreshTokenForLogout(String refreshTokenValue) {
        // 1. Refresh Token 검증 및 조회
        RefreshToken refreshToken = validateAndGetRefreshToken(refreshTokenValue);
        
        // 2. LOGOUT 이유로 폐기
        refreshToken.logout();
        
        // 3. 영속화
        refreshTokenRepository.save(refreshToken);
    }
}
