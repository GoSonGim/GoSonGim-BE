package com.example.GoSonGim_BE.domain.review.service;

import com.example.GoSonGim_BE.domain.review.dto.response.ReviewDailyResponse;
import com.example.GoSonGim_BE.domain.review.dto.response.ReviewKitRecordsResponse;
import com.example.GoSonGim_BE.domain.review.dto.response.ReviewKitsResponse;
import com.example.GoSonGim_BE.domain.review.dto.response.ReviewMonthlyResponse;
import com.example.GoSonGim_BE.domain.review.dto.response.ReviewSituationDetailResponse;
import com.example.GoSonGim_BE.domain.review.dto.response.ReviewSituationsResponse;
import com.example.GoSonGim_BE.domain.review.dto.response.ReviewWordsResponse;

import java.time.LocalDate;
import java.time.YearMonth;

/**
 * 복습 서비스 인터페이스
 */
public interface ReviewService {
    
    /**
     * 사용자의 학습한 단어 중 랜덤으로 최대 5개 조회
     * 
     * @param userId 사용자 ID
     * @return 복습 단어 목록
     */
    ReviewWordsResponse getRandomReviewWords(Long userId);

    /**
     * 사용자가 학습한 상황극 복습 목록 조회
     *
     * @param userId 사용자 ID
     * @param category 카테고리 (all 포함)
     * @param sort 정렬 기준 (latest/oldest)
     * @param page 페이지 (1부터 시작)
     * @param size 페이지 크기
     * @return 상황극 복습 목록 응답
     */
    ReviewSituationsResponse getReviewSituations(Long userId, String category, String sort, int page, int size);

    /**
     * 사용자가 학습한 조음 키트 복습 목록 조회
     *
     * @param userId 사용자 ID
     * @param categoryId 카테고리 ID (null이면 전체)
     * @param sort 정렬 기준 (latest/oldest)
     * @param page 페이지 (1부터 시작)
     * @param size 페이지 크기
     * @return 조음 키트 복습 목록 응답
     */
    ReviewKitsResponse getReviewKits(Long userId, Long categoryId, String sort, int page, int size);

    /**
     * 특정 키트의 학습 녹음 기록 조회
     *
     * @param userId 사용자 ID
     * @param kitId 키트 ID
     * @return 녹음 기록 목록 응답
     */
    ReviewKitRecordsResponse getKitRecords(Long userId, Long kitId);
    
    /**
     * 특정 조음 키트 로그 기준으로 같은 학습 세션의 모든 로그 조회
     *
     * @param userId 사용자 ID
     * @param kitStageLogId 키트 스테이지 로그 ID
     * @return 같은 학습 세션의 모든 녹음 기록 응답
     */
    ReviewKitRecordsResponse getKitLogRecord(Long userId, Long kitStageLogId);
    
    /**
     * 상황극 복습 상세 조회
     *
     * @param userId 사용자 ID
     * @param recordingId 학습 기록 ID
     * @return 상황극 복습 상세 응답
     */
    ReviewSituationDetailResponse getReviewSituationDetail(Long userId, Long recordingId);
    
    /**
     * 월별 학습 기록 조회
     * 지정한 월에 학습이 있었던 날짜 목록을 반환합니다.
     *
     * @param userId 사용자 ID
     * @param month 조회할 월
     * @return 월별 학습 기록 응답
     */
    ReviewMonthlyResponse getMonthlyReview(Long userId, YearMonth month);
    
    /**
     * 일별 학습 기록 조회
     * 선택한 날짜에 학습한 조음 키트를 최신순으로 반환합니다.
     *
     * @param userId 사용자 ID
     * @param date 조회할 날짜
     * @return 일별 학습 기록 응답
     */
    ReviewDailyResponse getDailyReview(Long userId, LocalDate date);
}

