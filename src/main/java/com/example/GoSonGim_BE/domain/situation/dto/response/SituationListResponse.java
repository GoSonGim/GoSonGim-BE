package com.example.GoSonGim_BE.domain.situation.dto.response;

import com.example.GoSonGim_BE.domain.situation.entity.Situation;

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
    ) {
        public static SituationItem from(Situation situation) {
            return new SituationItem(
                situation.getId(),
                situation.getSituationName()
            );
        }
    }

    public static SituationListResponse from(List<Situation> situations) {
        List<SituationItem> items = situations.stream()
            .map(SituationItem::from)
            .toList();
        return new SituationListResponse(items);
    }
}

