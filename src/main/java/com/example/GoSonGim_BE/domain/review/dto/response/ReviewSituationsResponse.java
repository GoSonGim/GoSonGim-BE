package com.example.GoSonGim_BE.domain.review.dto.response;

import java.util.List;

public record ReviewSituationsResponse(
    List<ReviewSituationItemResponse> items,
    int count,
    ReviewSituationsPageInfo pageInfo
) {
    public static ReviewSituationsResponse of(List<ReviewSituationItemResponse> items,
                                              int page,
                                              int size,
                                              boolean hasNext) {
        return new ReviewSituationsResponse(items, items.size(), new ReviewSituationsPageInfo(page, size, hasNext));
    }
}
