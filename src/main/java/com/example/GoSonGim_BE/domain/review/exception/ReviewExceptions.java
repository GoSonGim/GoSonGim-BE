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
    
    /**
     * 상황극 학습 기록을 찾을 수 없는 예외
     */
    public static class SituationLogNotFoundException extends BaseException {
        public SituationLogNotFoundException(Long recordingId) {
            super("상황극 복습 기록을 찾을 수 없습니다: " + recordingId, "SITUATION_LOG_NOT_FOUND");
        }
    }
    
    /**
     * 상황극 학습 기록 접근 권한 없음 예외
     */
    public static class SituationLogAccessDeniedException extends BaseException {
        public SituationLogAccessDeniedException(Long recordingId) {
            super("해당 기록에 접근할 권한이 없습니다: " + recordingId, "SITUATION_LOG_ACCESS_DENIED");
        }
    }
    
    /**
     * 대화 내역 데이터가 유효하지 않은 예외
     */
    public static class InvalidConversationDataException extends BaseException {
        public InvalidConversationDataException(String message) {
            super(message, "INVALID_CONVERSATION_DATA");
        }
    }
    
    /**
     * 조음 키트 학습 로그를 찾을 수 없는 예외
     */
    public static class KitLogNotFoundException extends BaseException {
        public KitLogNotFoundException(Long kitStageLogId) {
            super("조음 키트 학습 기록을 찾을 수 없습니다: " + kitStageLogId, "KIT_LOG_NOT_FOUND");
        }
    }
    
    /**
     * 조음 키트 학습 로그 접근 권한 없음 예외
     */
    public static class KitLogAccessDeniedException extends BaseException {
        public KitLogAccessDeniedException(Long kitStageLogId) {
            super("해당 기록에 접근할 권한이 없습니다: " + kitStageLogId, "KIT_LOG_ACCESS_DENIED");
        }
    }
}

