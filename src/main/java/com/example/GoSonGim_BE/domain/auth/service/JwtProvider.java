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

import com.example.GoSonGim_BE.domain.auth.entity.RefreshToken;
import com.example.GoSonGim_BE.domain.auth.repository.RefreshTokenRepository;
import com.example.GoSonGim_BE.domain.users.entity.User;
import com.example.GoSonGim_BE.domain.users.service.UserService;
import com.example.GoSonGim_BE.global.config.JwtProperties;

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
     * Access Token 생성
     * 
     * @param userId 사용자 ID
     * @return JWT Access Token
     */
    public String generateAcessToken(Long userId) {
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
     * @return RefreshToken 엔티티
     */
    public RefreshToken generateRefreshToken(Long userId) {
        // 1. 기존 토큰 삭제 (OneToOne)
        refreshTokenRepository.deleteByUserId(userId);
        
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
        
        // 4. 토큰 해시 생성
        String tokenHash = hashToken(tokenValue);
        
        // 5. 만료 시각 계산
        long expirationMillis = jwtProperties.getRefreshToken().getExpiration();
        LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(expirationMillis / 1000);
        
        // 6. User 조회
        User user = userService.findById(userId);
        
        // 7. RefreshToken 엔티티 생성 & 저장
        RefreshToken refreshToken = RefreshToken.builder()
            .user(user)
            .jti(jti)
            .tokenHash(tokenHash)
            .expiresAt(expiresAt)
            .build();
        
        return refreshTokenRepository.save(refreshToken);
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
}
