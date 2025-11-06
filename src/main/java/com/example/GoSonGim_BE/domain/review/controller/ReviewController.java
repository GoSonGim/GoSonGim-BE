package com.example.GoSonGim_BE.domain.review.controller;

import com.example.GoSonGim_BE.domain.review.dto.response.ReviewWordsResponse;
import com.example.GoSonGim_BE.domain.review.service.ReviewService;
import com.example.GoSonGim_BE.global.constant.ApiVersion;
import com.example.GoSonGim_BE.global.dto.ApiResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 복습 API 컨트롤러
 */
@Tag(name = "Review API")
@RestController
@RequestMapping(ApiVersion.CURRENT + "/review")
@RequiredArgsConstructor
public class ReviewController {
    
    private final ReviewService reviewService;
    
    /**
     * 복습 단어 랜덤 조회
     * 사용자의 학습 기록에서 랜덤으로 최대 5개의 단어를 반환합니다.
     */
    @Operation(summary = "복습 단어 랜덤 조회", description = "사용자의 학습 기록에서 랜덤으로 최대 5개의 단어를 반환합니다.")
    @GetMapping("/words")
    public ResponseEntity<ApiResult<ReviewWordsResponse>> getRandomReviewWords(
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        ReviewWordsResponse result = reviewService.getRandomReviewWords(userId);
        ApiResult<ReviewWordsResponse> response = ApiResult.success(200, "복습 단어 조회 성공", result);
        return ResponseEntity.ok(response);
    }
}
