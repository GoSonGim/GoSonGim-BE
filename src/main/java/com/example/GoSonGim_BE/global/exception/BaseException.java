package com.example.GoSonGim_BE.global.exception;

public class BaseException extends RuntimeException {
    private final String errorCode;

    protected BaseException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
}
