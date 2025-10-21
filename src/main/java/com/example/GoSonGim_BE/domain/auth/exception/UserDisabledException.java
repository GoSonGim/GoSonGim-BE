package com.example.GoSonGim_BE.domain.auth.exception;

import com.example.GoSonGim_BE.global.exception.BaseException;

public class UserDisabledException extends BaseException {
    public UserDisabledException() {
        super("비활성화된 계정입니다.", "USER_DISABLED");
    }
}
