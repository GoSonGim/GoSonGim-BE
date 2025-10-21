package com.example.GoSonGim_BE.domain.auth.exception;

import com.example.GoSonGim_BE.global.exception.BaseException;

public class EmailAlreadyUsedException extends BaseException {
    public EmailAlreadyUsedException(String email) {
        super("이미 등록된 이메일입니다: " + email, "EMAIL_ALREADY_USED");
    }
}
