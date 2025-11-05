package com.example.GoSonGim_BE.domain.situation.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * 상황극 학습 세션 답변 및 평가 요청 DTO
 */
public record SituationSessionReplyRequest(
    @NotBlank(message = "세션 ID는 필수입니다.")
    String sessionId,
    
    @NotBlank(message = "답변은 필수입니다.")
    String answer,
    
    /**
     * 오디오 파일 S3 키 (선택적, 복습용)
     */
    String audioFileKey
) {}

