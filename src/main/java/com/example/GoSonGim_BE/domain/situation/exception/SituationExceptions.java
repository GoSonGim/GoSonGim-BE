package com.example.GoSonGim_BE.domain.situation.exception;

import com.example.GoSonGim_BE.domain.situation.entity.SituationSession;
import com.example.GoSonGim_BE.global.exception.BaseException;

/**
 * 상황극 도메인 예외 클래스
 */
public class SituationExceptions {
    
    /**
     * 상황극 조회 실패 예외
     */
    public static class SituationNotFoundException extends BaseException {
        public SituationNotFoundException(Long situationId) {
            super("상황극을 찾을 수 없습니다: " + situationId, "SITUATION_NOT_FOUND");
        }
    }
    
    /**
     * 세션 조회 실패 예외
     */
    public static class SessionNotFoundException extends BaseException {
        public SessionNotFoundException(String sessionId) {
            super("세션을 찾을 수 없습니다: " + sessionId, "SESSION_NOT_FOUND");
        }
    }
    
    /**
     * 세션 접근 거부 예외
     */
    public static class SessionAccessDeniedException extends BaseException {
        public SessionAccessDeniedException(String sessionId) {
            super("세션에 접근할 권한이 없습니다: " + sessionId, "SESSION_ACCESS_DENIED");
        }
    }
    
    /**
     * 세션이 활성 상태가 아닌 예외
     */
    public static class SessionNotActiveException extends BaseException {
        public SessionNotActiveException(String sessionId, SituationSession.SessionStatus status) {
            super("세션이 활성 상태가 아닙니다: " + sessionId + " (상태: " + status + ")", "SESSION_NOT_ACTIVE");
        }
    }
    
    /**
     * 세션 데이터가 유효하지 않은 예외
     */
    public static class SessionInvalidException extends BaseException {
        public SessionInvalidException(String message) {
            super(message, "SESSION_INVALID");
        }
    }
    
    /**
     * 음성 인식(STT) 실패 예외
     */
    public static class SpeechToTextException extends BaseException {
        public SpeechToTextException(String message) {
            super(message, "SPEECH_TO_TEXT_FAILED");
        }
    }
}

