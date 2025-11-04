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

    public static class InvalidRefreshTokenException extends BaseException {
        public InvalidRefreshTokenException() {
            super("유효하지 않은 Refresh Token입니다.", "INVALID_REFRESH_TOKEN");
        }
    }
    
    public static class RefreshTokenNotFoundException extends BaseException {
        public RefreshTokenNotFoundException() {
            super("Refresh Token을 찾을 수 없습니다.", "REFRESH_TOKEN_NOT_FOUND");
        }
    }
    
    public static class RefreshTokenRevokedException extends BaseException {
        public RefreshTokenRevokedException() {
            super("폐기된 Refresh Token입니다.", "REFRESH_TOKEN_REVOKED");
        }
    }
    
    public static class RefreshTokenExpiredException extends BaseException {
        public RefreshTokenExpiredException() {
            super("만료된 Refresh Token입니다.", "REFRESH_TOKEN_EXPIRED");
        }
    }
}