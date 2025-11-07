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
    
    /**
     * 잘못된 쿼리 파라미터 예외
     */
    public static class InvalidQueryParameterException extends BaseException {
        public InvalidQueryParameterException(String parameter) {
            super("유효하지 않은 파라미터입니다: " + parameter, "INVALID_REVIEW_PARAMETER");
        }
    }

    /**
     * 키트를 찾을 수 없는 예외
     */
    public static class KitNotFoundException extends BaseException {
        public KitNotFoundException() {
            super("키트를 찾을 수 없습니다.", "KIT_NOT_FOUND");
        }
    }

    /**
     * 카테고리를 찾을 수 없는 예외
     */
    public static class CategoryNotFoundException extends BaseException {
        public CategoryNotFoundException() {
            super("카테고리를 찾을 수 없습니다.", "CATEGORY_NOT_FOUND");
        }
    }
}

