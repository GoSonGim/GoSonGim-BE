package com.example.GoSonGim_BE.domain.situation.dto.response;

/**
 * 상황극 상세 응답 DTO
 */
public record SituationDetailResponse(
    Long situationId,
    String situationName,
    String description,
    String image
) {}

