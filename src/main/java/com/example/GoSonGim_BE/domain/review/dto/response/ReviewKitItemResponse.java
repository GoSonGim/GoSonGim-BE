package com.example.GoSonGim_BE.domain.review.dto.response;

import java.time.LocalDateTime;

public record ReviewKitItemResponse(
    Long kitId,
    String kitName,
    LocalDateTime createdAt
) {
}
