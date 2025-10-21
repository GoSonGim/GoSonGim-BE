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
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_local_credentials")
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserLocalCredential extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @EqualsAndHashCode.Include
    private Long id;

    @Comment("사용자")
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Comment("이메일")
    @Column(name = "email", nullable = false, length = 255, unique = true)
    private String email;

    @Comment("이메일 인증 여부")
    @Column(name = "email_verified", nullable = false)
    private Boolean emailVerified;

    @Comment("비밀번호 해시")
    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;
    
    @Builder
    public UserLocalCredential(User user, String email, Boolean emailVerified, String passwordHash) {
        this.user = user;
        this.email = email;
        this.emailVerified = emailVerified != null ? emailVerified : false;
        this.passwordHash = passwordHash;
    }
    
    @PrePersist
    private void prePersistDefaults() {
        if (this.emailVerified == null) this.emailVerified = false;
    }
}
