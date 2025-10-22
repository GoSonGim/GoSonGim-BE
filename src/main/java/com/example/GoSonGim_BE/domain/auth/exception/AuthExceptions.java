package com.example.GoSonGim_BE.domain.auth.exception;

import com.example.GoSonGim_BE.global.exception.BaseException;

public class AuthExceptions {
    
    /**
     * 이메일 중복 예외
     */
    public static class EmailAlreadyUsedException extends BaseException {
        public EmailAlreadyUsedException(String email) {
            super("이미 등록된 이메일입니다: " + email, "EMAIL_ALREADY_USED");
        }
    }
    
    /**
     * 이메일 형식 오류 예외
     */
    public static class InvalidEmailFormatException extends BaseException {
        public InvalidEmailFormatException(String email) {
            super("이메일 형식이 올바르지 않습니다: " + email, "INVALID_EMAIL_FORMAT");
        }
    }
    
    /**
     * 잘못된 자격증명 예외
     */
    public static class InvalidCredentialsException extends BaseException {
        public InvalidCredentialsException() {
            super("이메일 또는 비밀번호가 올바르지 않습니다.", "INVALID_CREDENTIALS");
        }
    }
    
    /**
     * 탈퇴한 계정 예외
     */
    public static class UserDeletedException extends BaseException {
        public UserDeletedException() {
            super("탈퇴한 계정입니다.", "USER_DELETED");
        }
    }

    /**
     * 유효하지 않은 리디렉션 URI 예외
     */
    public static class InvalidRedirectUriException extends BaseException {
        public InvalidRedirectUriException() {
            super("유효하지 않은 리디렉션 URI입니다.", "INVALID_REDIRECT_URI");
        }
    }

    /**
     * OAuth 토큰 교환 실패 예외
    */
    public static class OAuthTokenInvalidException extends BaseException {
        public OAuthTokenInvalidException() {
            super("구글 인증 정보가 유효하지 않습니다.", "OAUTH_TOKEN_INVALID_EXCEPTION");
        }
    }

    /**
     * OAuth 사용자 정보 조회 실패 예외
     */
    public static class OAuthUserInfoNotFoundException extends BaseException {
        public OAuthUserInfoNotFoundException() {
            super("구글 사용자 정보를 찾을 수 없습니다.", "OAUTH_USER_INFO_NOT_FOUND_EXCEPTION");
        }
    }
}