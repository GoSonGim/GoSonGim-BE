package com.example.GoSonGim_BE.domain.situation.dto.request;

import jakarta.validation.constraints.NotNull;

/**
 * 상황극 학습 세션 시작 요청 DTO
 */
public record SituationSessionStartRequest(
    @NotNull(message = "상황극 ID는 필수입니다.")
    Long situationId
) {}

