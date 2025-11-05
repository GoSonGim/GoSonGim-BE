package com.example.GoSonGim_BE.domain.situation.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * 상황극 학습 세션 종료 요청 DTO
 */
public record SituationSessionEndRequest(
    @NotBlank(message = "세션 ID는 필수입니다.")
    String sessionId
) {}

