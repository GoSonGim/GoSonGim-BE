package com.example.GoSonGim_BE.domain.situation.entity;

import org.hibernate.annotations.Comment;

import com.example.GoSonGim_BE.domain.users.entity.User;
import com.example.GoSonGim_BE.global.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 상황극 학습 기록
 */
@Entity
@Table(name = "situation_log", indexes = {
    @Index(name = "idx_situation_log_user", columnList = "user_id"),
    @Index(name = "idx_situation_log_situation", columnList = "situation_id"),
    @Index(name = "idx_situation_log_created", columnList = "created_at")
})
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SituationLog extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @EqualsAndHashCode.Include
    private Long id;

    @Comment("상황극 참조")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "situation_id", nullable = false)
    private Situation situation;

    @Comment("사용자 참조")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Comment("AI 영상 URL")
    @Column(name = "ai_video_url")
    private String aiVideoUrl;

    @Comment("녹음 파일 S3 경로")
    @Column(name = "audio_file_key")
    private String audioFileKey;

    @Comment("학습 단어")
    @Column(name = "target_word")
    private String targetWord;

    @Comment("대화 내용")
    @Column(name = "conversation", columnDefinition = "LONGTEXT")
    private String conversation;

    @Comment("학습 성공 여부")
    @Column(name = "is_success")
    private Boolean isSuccess;

    @Comment("평가 점수")
    @Column(name = "evaluation_score")
    private Float evaluationScore;

    @Comment("평가 피드백")
    @Column(name = "evaluation_feedback", columnDefinition = "LONGTEXT")
    private String evaluationFeedback;

    @Builder
    public SituationLog(Situation situation, User user, String aiVideoUrl,
                        String audioFileKey, String targetWord, String conversation,
                        Boolean isSuccess, Float evaluationScore, String evaluationFeedback) {
        this.situation = situation;
        this.user = user;
        this.aiVideoUrl = aiVideoUrl;
        this.audioFileKey = audioFileKey;
        this.targetWord = targetWord;
        this.conversation = conversation;
        this.isSuccess = isSuccess;
        this.evaluationScore = evaluationScore;
        this.evaluationFeedback = evaluationFeedback;
    }
}