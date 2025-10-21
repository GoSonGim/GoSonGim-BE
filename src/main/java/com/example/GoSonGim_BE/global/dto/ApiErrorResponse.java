package com.example.GoSonGim_BE.global.dto;

import java.time.LocalDateTime;

public record ApiErrorResponse(
    boolean success,
    int status,
    String message,
    String timestamp,
    ErrorDetail error
) {
    public record ErrorDetail(
        String code
    ) {}
    
    public static ApiErrorResponse error(int status, String message, String errorCode) {
        return new ApiErrorResponse(false, status, message, LocalDateTime.now().toString(), new ErrorDetail(errorCode));
    }
}
