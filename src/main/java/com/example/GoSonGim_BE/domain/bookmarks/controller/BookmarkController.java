package com.example.GoSonGim_BE.domain.bookmarks.controller;

import com.example.GoSonGim_BE.domain.bookmarks.dto.request.AddKitBookmarkRequest;
import com.example.GoSonGim_BE.domain.bookmarks.dto.request.AddSituationBookmarkRequest;
import com.example.GoSonGim_BE.domain.bookmarks.dto.response.BookmarkListResponse;
import com.example.GoSonGim_BE.domain.bookmarks.dto.response.BookmarkPreviewListResponse;
import com.example.GoSonGim_BE.domain.bookmarks.entity.BookmarkedTargetType;
import com.example.GoSonGim_BE.domain.bookmarks.service.BookmarkService;
import com.example.GoSonGim_BE.global.constant.ApiVersion;
import com.example.GoSonGim_BE.global.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(ApiVersion.CURRENT + "/bookmark")
@RequiredArgsConstructor
public class BookmarkController {
    
    private final BookmarkService bookmarkService;
    
    @PostMapping("/kit")
    public ResponseEntity<ApiResponse<Void>> addKitBookmarks(
            Authentication authentication,
            @Valid @RequestBody AddKitBookmarkRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        bookmarkService.addKitBookmarks(userId, request);
        
        ApiResponse<Void> response = ApiResponse.success(201, "키트가 내 학습에 추가되었습니다.", null);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @PostMapping("/situation")
    public ResponseEntity<ApiResponse<Void>> addSituationBookmarks(
            Authentication authentication,
            @Valid @RequestBody AddSituationBookmarkRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        bookmarkService.addSituationBookmarks(userId, request);
        
        ApiResponse<Void> response = ApiResponse.success(201, "상황극이 내 학습에 추가되었습니다.", null);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @DeleteMapping("/{bookmarkId}")
    public ResponseEntity<ApiResponse<Void>> deleteBookmark(
            Authentication authentication,
            @PathVariable Long bookmarkId) {
        Long userId = (Long) authentication.getPrincipal();
        bookmarkService.deleteBookmark(userId, bookmarkId);
        
        ApiResponse<Void> response = ApiResponse.success(200, "북마크가 삭제되었습니다.", null);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping
    public ResponseEntity<ApiResponse<BookmarkListResponse>> getBookmarks(
            Authentication authentication,
            @RequestParam BookmarkedTargetType type,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String sort) {
        Long userId = (Long) authentication.getPrincipal();
        BookmarkListResponse result = bookmarkService.getBookmarks(userId, type, category, sort);
        
        ApiResponse<BookmarkListResponse> response = ApiResponse.success(200, "요청한 내학습(북마크)가 조회되었습니다.", result);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/preview")
    public ResponseEntity<ApiResponse<BookmarkPreviewListResponse>> getBookmarkPreview(
            Authentication authentication,
            @RequestParam(defaultValue = "10") int limit) {
        Long userId = (Long) authentication.getPrincipal();
        BookmarkPreviewListResponse result = bookmarkService.getBookmarkPreview(userId, limit);
        
        ApiResponse<BookmarkPreviewListResponse> response = ApiResponse.success(200, "내학습(북마크) 미리보기가 조회되었습니다.", result);
        return ResponseEntity.ok(response);
    }
}