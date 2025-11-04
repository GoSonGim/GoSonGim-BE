package com.example.GoSonGim_BE.domain.bookmarks.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public record BookmarkResponse(
    Long bookmarkId,
    Long kitId,
    String kitName,
    String kitCategory,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime createdAt
) {
}