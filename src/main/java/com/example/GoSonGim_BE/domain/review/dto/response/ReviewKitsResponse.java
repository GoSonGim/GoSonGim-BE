package com.example.GoSonGim_BE.domain.review.dto.response;

import java.util.List;

public record ReviewKitsResponse(
    List<ReviewKitItemResponse> items,
    int count,
    ReviewKitsPageInfo pageInfo
) {
    public static ReviewKitsResponse of(List<ReviewKitItemResponse> items,
                                        int page,
                                        int size,
                                        boolean hasNext) {
        return new ReviewKitsResponse(items, items.size(), new ReviewKitsPageInfo(page, size, hasNext));
    }
}
