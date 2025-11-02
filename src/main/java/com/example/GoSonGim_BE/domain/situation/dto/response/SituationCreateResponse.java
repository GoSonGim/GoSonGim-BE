package com.example.GoSonGim_BE.domain.situation.dto.response;

/**
 * 상황극 생성 응답 DTO
 */
public record SituationCreateResponse(
    Long situationId,
    String situationCategory,
    String situationName,
    String description,
    String image
) {}