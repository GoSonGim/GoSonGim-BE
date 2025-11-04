package com.example.GoSonGim_BE.domain.bookmarks.dto.response;

import java.util.List;

public record BookmarkPreviewListResponse(
    int count,
    List<BookmarkPreviewResponse> bookmarkList
) {
}