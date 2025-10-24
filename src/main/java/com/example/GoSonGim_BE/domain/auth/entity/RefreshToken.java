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
import jakarta.persistence.OneToOne;
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
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Comment("토큰 고유 식별자")
    @Column(name = "token", nullable = false, unique = true)
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
    private String revokedReason;

    @Builder
    public RefreshToken(User user, String jti, String tokenHash, LocalDateTime expiresAt, LocalDateTime revokedAt, String revokedReason) {
        this.user = user;
        this.jti = jti;
        this.tokenHash = tokenHash;
        this.expiresAt = expiresAt;
        this.revokedAt = revokedAt;
        this.revokedReason = revokedReason;
    }
}
