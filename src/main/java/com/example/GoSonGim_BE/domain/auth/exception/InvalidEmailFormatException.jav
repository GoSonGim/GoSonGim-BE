package com.example.GoSonGim_BE.domain.auth.exception;

import com.example.GoSonGim_BE.global.exception.BaseException;

public class InvalidEmailFormatException extends BaseException {
    public InvalidEmailFormatException(String email) {
        super("이메일 형식이 올바르지 않습니다: " + email, "INVALID_EMAIL_FORMAT");
    }
}
