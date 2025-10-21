package com.example.GoSonGim_BE.domain.users.entity;

import com.example.GoSonGim_BE.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @EqualsAndHashCode.Include
    private Long id;

    @Comment("닉네임")
    @Column(name = "nickname", nullable = true, length = 50)
    private String nickname;

    @Comment("연속 학습 일수")
    @Column(name = "streak_days", nullable = false)
    @ColumnDefault("0")
    private Integer streakDays;

    @Comment("마지막 활동 일자")
    @Column(name = "last_activity_date")
    private LocalDate lastActivityDate;

    @Comment("총 학습 성공 횟수")
    @Column(name = "total_success_count", nullable = false)
    @ColumnDefault("0")
    private Integer totalSuccessCount;

    @Comment("학습 레벨")
    @Enumerated(EnumType.STRING)
    @Column(name = "level", nullable = false, length = 20)
    private UserLevel level;

    @Comment("소프트 삭제 여부")
    @Column(name = "is_deleted", nullable = false)
    @ColumnDefault("0")
    private Boolean isDeleted;

    @Comment("삭제 일시")
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Comment("정보제공 동의 버전")
    @Column(name = "consent_type", length = 50)
    private String consentType;

    @Comment("동의 일시")
    @Column(name = "consented_at")
    private LocalDateTime consentedAt;

    @Builder
    public User(String nickname, Integer streakDays, LocalDate lastActivityDate,
                Integer totalSuccessCount, UserLevel level, String consentType,
                LocalDateTime consentedAt) {
        this.nickname = nickname;
        this.streakDays = streakDays != null ? streakDays : 0;
        this.lastActivityDate = lastActivityDate;
        this.totalSuccessCount = totalSuccessCount != null ? totalSuccessCount : 0;
        this.level = level != null ? level : UserLevel.BEGINNER_1;
        this.isDeleted = false;
        this.deletedAt = null;
        this.consentType = consentType;
        this.consentedAt = consentedAt;
    }

    @PrePersist
    private void prePersistDefaults() {
        if (this.streakDays == null) this.streakDays = 0;
        if (this.totalSuccessCount == null) this.totalSuccessCount = 0;
        if (this.isDeleted == null) this.isDeleted = false;
    }

    /**
     * 정보제공 동의 업데이트
     *
     * @param consentType 동의 버전
     */
    public void updateConsent(String consentType) {
        this.consentType = consentType;
        this.consentedAt = LocalDateTime.now();
    }

    /**
     * 소프트 삭제 처리
     */
    public void delete() {
        this.isDeleted = true;
        this.deletedAt = LocalDateTime.now();
    }

    /**
     * 삭제 취소 (복구)
     */
    public void restore() {
        this.isDeleted = false;
        this.deletedAt = null;
    }
}

