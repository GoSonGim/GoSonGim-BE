package com.example.GoSonGim_BE.domain.bookmarks.dto.response;

import com.example.GoSonGim_BE.domain.bookmarks.entity.BookmarkedTargetType;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public record BookmarkPreviewResponse(
    Long bookmarkId,
    BookmarkedTargetType type,
    String title,
    String category,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime createdAt
) {
}