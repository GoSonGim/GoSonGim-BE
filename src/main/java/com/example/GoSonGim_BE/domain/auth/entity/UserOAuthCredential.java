package com.example.GoSonGim_BE.domain.auth.entity;

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
@Table(name = "user_oauth_credentials")
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserOAuthCredential extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @EqualsAndHashCode.Include
    private Long id;

    @Comment("사용자")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Comment("OAuth 공급자")
    @Column(name = "provider", nullable = false)
    private String provider;

    @Comment("OAuth 공급자 ID")
    @Column(name = "provider_id", nullable = false)
    private String providerId;

    @Comment("OAuth 공급자 이메일")
    @Column(name = "provider_email", nullable = false)
    private String providerEmail;

    @Builder
    public UserOAuthCredential(User user, String provider, String providerId, String providerEmail) {
        this.user = user;
        this.provider = provider;
        this.providerId = providerId;
        this.providerEmail = providerEmail;
    }

}
