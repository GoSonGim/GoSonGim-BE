package com.example.GoSonGim_BE.domain.situation.dto.response;

import com.example.GoSonGim_BE.domain.situation.entity.Situation;

/**
 * 상황극 생성 응답 DTO
 */
public record SituationCreateResponse(
    Long situationId,
    String situationCategory,
    String situationName,
    String description,
    String image
) {
    public static SituationCreateResponse from(Situation situation) {
        return new SituationCreateResponse(
            situation.getId(),
            situation.getSituationCategory().name(),
            situation.getSituationName(),
            situation.getDescription(),
            situation.getImage()
        );
    }
}