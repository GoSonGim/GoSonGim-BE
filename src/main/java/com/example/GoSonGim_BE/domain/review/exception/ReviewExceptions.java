package com.example.GoSonGim_BE.domain.review.exception;

import com.example.GoSonGim_BE.global.exception.BaseException;

/**
 * 복습 도메인 예외 클래스
 */
public class ReviewExceptions {
    
    /**
     * 학습 기록이 존재하지 않는 예외
     */
    public static class NoLearningHistoryException extends BaseException {
        public NoLearningHistoryException() {
            super("학습 기록이 존재하지 않습니다.", "NO_LEARNING_HISTORY");
        }
    }
}

