package com.example.GoSonGim_BE.domain.review.dto.response;

public record ReviewSituationsPageInfo(
    int page,
    int size,
    boolean hasNext
) {
}
