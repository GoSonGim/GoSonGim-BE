package com.example.GoSonGim_BE.domain.situation.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 상황극 학습 세션 정보 (Redis 저장용)
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SituationSession implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 세션 ID (user_{userId}_sit_{situationId}_{timestamp} 형식)
     */
    private String sessionId;
    
    /**
     * 사용자 ID
     */
    private Long userId;
    
    /**
     * 상황극 ID
     */
    private Long situationId;
    
    /**
     * HeyGen 세션 ID
     */
    private String heygenSessionId;
    
    /**
     * HeyGen Access Token
     */
    private String heygenAccessToken;
    
    /**
     * HeyGen WebRTC URL
     */
    private String heygenUrl;
    
    /**
     * 현재 대화 단계 (첫 질문부터 시작: 1)
     */
    private Integer currentStep;
    
    /**
     * 대화 내역 (JSON 문자열)
     */
    private String conversationHistory;
    
    /**
     * 세션 생성 시간
     */
    private LocalDateTime createdAt;
    
    /**
     * 세션 만료 시간 (기본 30분)
     */
    private LocalDateTime expiresAt;
    
    /**
     * 세션 상태 (ACTIVE, COMPLETED, EXPIRED)
     */
    private SessionStatus status;
    
    public enum SessionStatus {
        ACTIVE,      // 진행 중
        COMPLETED,   // 완료됨
        EXPIRED      // 만료됨
    }
}

