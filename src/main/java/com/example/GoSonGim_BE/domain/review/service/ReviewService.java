package com.example.GoSonGim_BE.domain.review.service;

import com.example.GoSonGim_BE.domain.review.dto.response.ReviewWordsResponse;

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
}

