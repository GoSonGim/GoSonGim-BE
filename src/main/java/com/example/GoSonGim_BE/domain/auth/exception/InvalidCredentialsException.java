package com.example.GoSonGim_BE.domain.auth.exception;

import com.example.GoSonGim_BE.global.exception.BaseException;

public class InvalidCredentialsException extends BaseException {
    public InvalidCredentialsException() {
        super("이메일 또는 비밀번호가 올바르지 않습니다.", "INVALID_CREDENTIALS");
    }
}
