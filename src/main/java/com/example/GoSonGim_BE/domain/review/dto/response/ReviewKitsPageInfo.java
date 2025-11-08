package com.example.GoSonGim_BE.domain.review.dto.response;

public record ReviewKitsPageInfo(
    int page,
    int size,
    boolean hasNext
) {
}
