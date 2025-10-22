package com.example.GoSonGim_BE.domain.users.exception;

import com.example.GoSonGim_BE.global.exception.BaseException;

/**
 * 사용자 도메인 예외 클래스
 */
public class UserExceptions {
    
    /**
     * 이미 삭제된 사용자 예외
     */
    public static class UserAlreadyDeletedException extends BaseException {
        public UserAlreadyDeletedException() {
            super("이미 삭제된 사용자입니다.", "USER_ALREADY_DELETED");
        }
    }
    
    /**
     * 삭제되지 않은 사용자 예외
     */
    public static class UserNotDeletedException extends BaseException {
        public UserNotDeletedException() {
            super("삭제되지 않은 사용자입니다.", "USER_NOT_DELETED");
        }
    }
}
