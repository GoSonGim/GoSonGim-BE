package com.example.GoSonGim_BE.domain.situation.dto.response;

import java.util.List;

/**
 * 상황극 목록 응답 DTO
 */
public record SituationListResponse(
    List<SituationItem> situations
) {
    public record SituationItem(
        Long situationId,
        String situationName
    ) {}
}

