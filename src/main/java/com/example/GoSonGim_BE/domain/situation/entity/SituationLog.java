package com.example.GoSonGim_BE.domain.situation.entity;

import com.example.GoSonGim_BE.domain.users.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 상황극 학습 기록
 */
@Entity
@Table(name = "situation_log")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class SituationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
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

    @CreatedDate
    @Comment("생성 일시")
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}