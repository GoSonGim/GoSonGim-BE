package com.example.GoSonGim_BE.domain.situation.dto.response;

/**
 * 상황극 학습 세션 시작 응답 DTO
 */
public record SituationSessionStartResponse(
    String sessionId,
    String question
) {}

