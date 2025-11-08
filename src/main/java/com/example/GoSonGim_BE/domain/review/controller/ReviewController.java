package com.example.GoSonGim_BE.domain.review.controller;

import com.example.GoSonGim_BE.domain.review.dto.response.ReviewKitRecordsResponse;
import com.example.GoSonGim_BE.domain.review.dto.response.ReviewKitsResponse;
import com.example.GoSonGim_BE.domain.review.dto.response.ReviewSituationDetailResponse;
import com.example.GoSonGim_BE.domain.review.dto.response.ReviewSituationsResponse;
import com.example.GoSonGim_BE.domain.review.dto.response.ReviewWordsResponse;
import com.example.GoSonGim_BE.domain.review.service.ReviewService;
import com.example.GoSonGim_BE.global.constant.ApiVersion;
import com.example.GoSonGim_BE.global.dto.ApiResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
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
    
    /**
     * 상황극 복습 목록 조회
     * 사용자의 학습 기록 중 상황극을 최신순 또는 오래된 순으로 조회합니다.
     */
    @Operation(summary = "상황극 복습 목록 조회", description = "사용자가 학습한 상황극을 최신 학습 기록 기준으로 조회합니다.")
    @GetMapping("/situations")
    public ResponseEntity<ApiResult<ReviewSituationsResponse>> getReviewSituations(
            Authentication authentication,
            @Parameter(description = "카테고리 (all, daily, purchase, medical, traffic, job, social, emergency)")
            @RequestParam(defaultValue = "all") String category,
            @Parameter(description = "정렬 기준 (latest, oldest)")
            @RequestParam(defaultValue = "latest") String sort,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long userId = (Long) authentication.getPrincipal();
        ReviewSituationsResponse result = reviewService.getReviewSituations(userId, category, sort, page, size);
        ApiResult<ReviewSituationsResponse> response = ApiResult.success(200, "상황극 복습 목록 조회 성공", result);
        return ResponseEntity.ok(response);
    }

    /**
     * 조음 키트 복습 목록 조회
     * 사용자의 학습 기록 중 조음 키트를 최신순 또는 오래된 순으로 조회합니다.
     */
    @Operation(summary = "조음 키트 복습 목록 조회", description = "사용자가 학습한 조음 키트를 최신 학습 기록 기준으로 조회합니다.")
    @GetMapping("/kits")
    public ResponseEntity<ApiResult<ReviewKitsResponse>> getReviewKits(
            Authentication authentication,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "latest") String sort,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long userId = (Long) authentication.getPrincipal();
        ReviewKitsResponse result = reviewService.getReviewKits(userId, categoryId, sort, page, size);
        ApiResult<ReviewKitsResponse> response = ApiResult.success(200, "조음 키트 복습 목록 조회 성공", result);
        return ResponseEntity.ok(response);
    }

    /**
     * 조음 키트 복습 녹음 듣기
     * 특정 키트의 모든 학습 녹음 기록을 조회합니다.
     */
    @Operation(summary = "조음 키트 복습 상세 조회", description = "특정 키트의 모든 학습 녹음 기록을 점수, 피드백과 함께 조회합니다.")
    @GetMapping("/kits/{kitId}")
    public ResponseEntity<ApiResult<ReviewKitRecordsResponse>> getKitRecords(
            Authentication authentication,
            @PathVariable Long kitId) {
        Long userId = (Long) authentication.getPrincipal();
        ReviewKitRecordsResponse result = reviewService.getKitRecords(userId, kitId);
        ApiResult<ReviewKitRecordsResponse> response = ApiResult.success(200, "조음 키트 상세 조회 성공", result);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 상황극 복습 상세 조회
     * 특정 상황극 학습 기록의 상세 내용(대화 내역, 평가, 오디오)을 조회합니다.
     */
    @Operation(summary = "상황극 복습 상세 조회", description = "특정 상황극 학습 기록의 상세 내용을 조회합니다. 오디오 파일은 1시간 유효한 presigned URL로 제공됩니다.")
    @GetMapping("/situations/{recordingId}")
    public ResponseEntity<ApiResult<ReviewSituationDetailResponse>> getReviewSituationDetail(
            Authentication authentication,
            @Parameter(description = "학습 기록 ID")
            @PathVariable Long recordingId) {
        Long userId = (Long) authentication.getPrincipal();
        ReviewSituationDetailResponse result = reviewService.getReviewSituationDetail(userId, recordingId);
        ApiResult<ReviewSituationDetailResponse> response = ApiResult.success(200, "상황극 복습 상세 조회 성공", result);
        return ResponseEntity.ok(response);
    }
}
