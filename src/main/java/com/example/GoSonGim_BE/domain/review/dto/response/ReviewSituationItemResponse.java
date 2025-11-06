package com.example.GoSonGim_BE.domain.review.dto.response;

import java.time.LocalDateTime;

public record ReviewSituationItemResponse(
    Long situationId,
    String situationName,
    Long recordingId,
    LocalDateTime createdAt
) {
}
