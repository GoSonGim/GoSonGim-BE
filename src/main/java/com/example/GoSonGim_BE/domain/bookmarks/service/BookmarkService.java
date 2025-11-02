package com.example.GoSonGim_BE.domain.bookmarks.service;

import com.example.GoSonGim_BE.domain.bookmarks.dto.request.AddKitBookmarkRequest;
import com.example.GoSonGim_BE.domain.bookmarks.dto.request.AddSituationBookmarkRequest;
import com.example.GoSonGim_BE.domain.bookmarks.dto.response.BookmarkListResponse;
import com.example.GoSonGim_BE.domain.bookmarks.dto.response.BookmarkPreviewListResponse;
import com.example.GoSonGim_BE.domain.bookmarks.entity.BookmarkedTargetType;

public interface BookmarkService {
    void addKitBookmarks(Long userId, AddKitBookmarkRequest request);
    void addSituationBookmarks(Long userId, AddSituationBookmarkRequest request);
    void deleteBookmark(Long userId, Long bookmarkId);
    BookmarkListResponse getBookmarks(Long userId, BookmarkedTargetType type, String category, String sort);
    BookmarkPreviewListResponse getBookmarkPreview(Long userId, int limit);
}