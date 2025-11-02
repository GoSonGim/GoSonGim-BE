package com.example.GoSonGim_BE.domain.bookmarks.dto.response;

import com.example.GoSonGim_BE.domain.bookmarks.entity.BookmarkedTargetType;

import java.util.List;

public record BookmarkListResponse(
    BookmarkedTargetType type,
    String sort,
    int totalCount,
    List<BookmarkResponse> data
) {
}