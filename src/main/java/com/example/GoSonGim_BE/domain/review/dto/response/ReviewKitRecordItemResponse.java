package com.example.GoSonGim_BE.domain.review.dto.response;

import java.time.LocalDateTime;

public record ReviewKitRecordItemResponse(
    Long id,
    Long kitStageId,
    String kitStageName,
    Float evaluationScore,
    String evaluationFeedback,
    Boolean isSuccess,
    String targetWord,
    String audioFileUrl,
    LocalDateTime createdAt
) {
}
