package com.example.GoSonGim_BE.domain.review.controller;

import com.example.GoSonGim_BE.domain.review.dto.response.ReviewDailyResponse;
import com.example.GoSonGim_BE.domain.review.dto.response.ReviewKitRecordsResponse;
import com.example.GoSonGim_BE.domain.review.dto.response.ReviewKitsResponse;
import com.example.GoSonGim_BE.domain.review.dto.response.ReviewMonthlyResponse;
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
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.YearMonth;

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
     * 조음 키트 학습 로그 상세 조회
     * 특정 학습 기록 ID를 기준으로 같은 학습 세션의 로그를 조회합니다.
     */
    @Operation(summary = "조음 키트 학습 로그 상세 조회", description = "특정 학습 기록의 상세 내용을 조회합니다. 일별 학습 조회에서 받은 recordingId로 조회하며, 같은 학습 세션의 모든 로그를 반환합니다.")
    @GetMapping("/kits/logs/{kitStageLogId}")
    public ResponseEntity<ApiResult<ReviewKitRecordsResponse>> getKitLogRecord(
            Authentication authentication,
            @Parameter(description = "키트 스테이지 로그 ID")
            @PathVariable Long kitStageLogId) {
        Long userId = (Long) authentication.getPrincipal();
        ReviewKitRecordsResponse result = reviewService.getKitLogRecord(userId, kitStageLogId);
        ApiResult<ReviewKitRecordsResponse> response = ApiResult.success(200, "조음 키트 로그 상세 조회 성공", result);
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
    
    /**
     * 월별 학습 기록 조회
     * 지정한 월에 학습이 있었던 날짜 목록을 조회합니다.
     */
    @Operation(summary = "월별 학습 기록 조회", description = "지정한 월에 학습이 있었던 날짜 목록을 조회합니다.")
    @GetMapping("/monthly")
    public ResponseEntity<ApiResult<ReviewMonthlyResponse>> getMonthlyReview(
            Authentication authentication,
            @Parameter(description = "조회할 월 (yyyy-MM 형식)", required = true)
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth month) {
        Long userId = (Long) authentication.getPrincipal();
        ReviewMonthlyResponse result = reviewService.getMonthlyReview(userId, month);
        ApiResult<ReviewMonthlyResponse> response = ApiResult.success(200, "월별 학습 기록 조회 성공", result);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 일별 학습 기록 조회
     * 선택한 날짜에 학습한 조음 키트를 최신순으로 조회합니다.
     */
    @Operation(summary = "일별 학습 기록 조회", description = "선택한 날짜에 학습한 조음 키트를 최신순으로 조회합니다. 같은 키트를 여러 번 학습했을 경우 중복해서 반환합니다.")
    @GetMapping("/daily")
    public ResponseEntity<ApiResult<ReviewDailyResponse>> getDailyReview(
            Authentication authentication,
            @Parameter(description = "조회할 날짜 (yyyy-MM-dd 형식)", required = true)
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        Long userId = (Long) authentication.getPrincipal();
        ReviewDailyResponse result = reviewService.getDailyReview(userId, date);
        ApiResult<ReviewDailyResponse> response = ApiResult.success(200, "일별 학습 기록 조회 성공", result);
        return ResponseEntity.ok(response);
    }
}
