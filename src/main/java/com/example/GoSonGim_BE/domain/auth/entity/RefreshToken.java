package com.example.GoSonGim_BE.domain.auth.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.Comment;

import com.example.GoSonGim_BE.domain.users.entity.User;
import com.example.GoSonGim_BE.global.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;    

@Entity
@Table(name = "refresh_tokens")
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshToken extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @EqualsAndHashCode.Include
    private Long id;

    @Comment("사용자")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Comment("토큰 고유 식별자 (JWT ID)")
    @Column(name = "jti", nullable = false, unique = true, length = 36)
    private String jti;   // UUID

    @Comment("토큰 해시")
    @Column(name = "token_hash", nullable = false, length = 255)
    private String tokenHash;

    @Comment("만료 시각")
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;
    
    @Comment("폐기 시각")
    @Column(name = "revoked_at")
    private LocalDateTime revokedAt;

    @Comment("폐기 사유")
    @Column(name="revoked_reason", length = 255)
    private RevokedReason revokedReason;

    @Builder
    public RefreshToken(User user, String jti, String tokenHash, LocalDateTime expiresAt, LocalDateTime revokedAt, RevokedReason revokedReason) {
        this.user = user;
        this.jti = jti;
        this.tokenHash = tokenHash;
        this.expiresAt = expiresAt;
        this.revokedAt = revokedAt;
        this.revokedReason = revokedReason;
    }

    /**
     * 토큰 폐기
     */
    public void revoke(RevokedReason reason) {
        this.revokedAt = LocalDateTime.now();
        this.revokedReason = reason;
    }

    /**
     * 토큰 회전 (Rotation)
     * - 현재 토큰을 ROTATED 사유로 폐기
     * 
     * @param newJti 새로운 JWT ID
     * @param newTokenHash 새로운 토큰 해시
     * @param newExpiresAt 새로운 만료 시각
     * @return 새로운 RefreshToken 엔티티
     */
    public RefreshToken rotate(String newJti, String newTokenHash, LocalDateTime newExpiresAt) {
        // 1. 현재 토큰 폐기
        this.revoke(RevokedReason.ROTATED);
        
        // 2. 새 토큰 생성
        return RefreshToken.builder()
            .user(this.user)
            .jti(newJti)
            .tokenHash(newTokenHash)
            .expiresAt(newExpiresAt)
            .build();
    }

    /**
     * 로그아웃 처리
     * - LOGOUT 사유로 토큰 폐기
     */
    public void logout() {
        this.revoke(RevokedReason.LOGOUT);
    }

    /**
     * 토큰 만료 여부
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiresAt);
    }

    /**
     * 토큰 폐기 여부
     */
    public boolean isRevoked() {
        return this.revokedAt != null;
    }

    /**
     * 토큰 유효성 확인
     */
    public boolean isValid() {
        return !isExpired() && !isRevoked();
    }
}
