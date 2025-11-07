package com.example.GoSonGim_BE.domain.review.dto.response;

import java.util.List;

public record ReviewKitRecordsResponse(
    Long kitId,
    String kitName,
    List<ReviewKitRecordItemResponse> records
) {
    public static ReviewKitRecordsResponse of(Long kitId, String kitName, List<ReviewKitRecordItemResponse> records) {
        return new ReviewKitRecordsResponse(kitId, kitName, records);
    }
}
