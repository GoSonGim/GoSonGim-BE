package com.example.GoSonGim_BE.domain.situation.exception;

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
}

