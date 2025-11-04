package com.example.GoSonGim_BE.domain.bookmarks.controller;

import com.example.GoSonGim_BE.domain.bookmarks.dto.request.AddKitBookmarkRequest;
import com.example.GoSonGim_BE.domain.bookmarks.dto.request.AddSituationBookmarkRequest;
import com.example.GoSonGim_BE.domain.bookmarks.dto.response.BookmarkListResponse;
import com.example.GoSonGim_BE.domain.bookmarks.dto.response.BookmarkPreviewListResponse;
import com.example.GoSonGim_BE.domain.bookmarks.entity.BookmarkedTargetType;
import com.example.GoSonGim_BE.domain.bookmarks.service.BookmarkService;
import com.example.GoSonGim_BE.global.constant.ApiVersion;
import com.example.GoSonGim_BE.global.dto.ApiResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Bookmark API")
@RestController
@RequestMapping(ApiVersion.CURRENT + "/bookmark")
@RequiredArgsConstructor
public class BookmarkController {
    
    private final BookmarkService bookmarkService;
    
    @Operation(summary = "내 학습(북마크) 추가 – 조음발음")
    @PostMapping("/kit")
    public ResponseEntity<ApiResult<Void>> addKitBookmarks(
            Authentication authentication,
            @Valid @RequestBody AddKitBookmarkRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        bookmarkService.addKitBookmarks(userId, request);
        
        ApiResult<Void> response = ApiResult.success(201, "키트가 내 학습에 추가되었습니다.", null);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @Operation(summary = "내 학습(북마크) 추가 – 상황극")
    @PostMapping("/situation")
    public ResponseEntity<ApiResult<Void>> addSituationBookmarks(
            Authentication authentication,
            @Valid @RequestBody AddSituationBookmarkRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        bookmarkService.addSituationBookmarks(userId, request);
        
        ApiResult<Void> response = ApiResult.success(201, "상황극이 내 학습에 추가되었습니다.", null);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @Operation(summary = "내 학습(북마크) 삭제")
    @DeleteMapping("/{bookmarkId}")
    public ResponseEntity<ApiResult<Void>> deleteBookmark(
            Authentication authentication,
            @PathVariable Long bookmarkId) {
        Long userId = (Long) authentication.getPrincipal();
        bookmarkService.deleteBookmark(userId, bookmarkId);
        
        ApiResult<Void> response = ApiResult.success(200, "북마크가 삭제되었습니다.", null);
        return ResponseEntity.ok(response);
    }
    
    @Operation(summary = "내 학습(북마크) 조회")
    @GetMapping
    public ResponseEntity<ApiResult<BookmarkListResponse>> getBookmarks(
            Authentication authentication,
            @RequestParam BookmarkedTargetType type,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String sort) {
        Long userId = (Long) authentication.getPrincipal();
        BookmarkListResponse result = bookmarkService.getBookmarks(userId, type, category, sort);
        
        ApiResult<BookmarkListResponse> response = ApiResult.success(200, "요청한 내학습(북마크)가 조회되었습니다.", result);
        return ResponseEntity.ok(response);
    }
    
    @Operation(summary = "내 학습 최신순 10개 조회")
    @GetMapping("/preview")
    public ResponseEntity<ApiResult<BookmarkPreviewListResponse>> getBookmarkPreview(
            Authentication authentication,
            @RequestParam(defaultValue = "10") int limit) {
        Long userId = (Long) authentication.getPrincipal();
        BookmarkPreviewListResponse result = bookmarkService.getBookmarkPreview(userId, limit);
        
        ApiResult<BookmarkPreviewListResponse> response = ApiResult.success(200, "내학습(북마크) 미리보기가 조회되었습니다.", result);
        return ResponseEntity.ok(response);
    }
}