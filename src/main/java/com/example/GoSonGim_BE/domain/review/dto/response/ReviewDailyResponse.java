package com.example.GoSonGim_BE.domain.review.dto.response;

import java.util.List;

/**
 * 일별 학습 기록 조회 응답
 */
public record ReviewDailyResponse(
    List<ReviewDailyItemResponse> items,
    int count
) {
    public static ReviewDailyResponse of(List<ReviewDailyItemResponse> items) {
        return new ReviewDailyResponse(items, items.size());
    }
}

