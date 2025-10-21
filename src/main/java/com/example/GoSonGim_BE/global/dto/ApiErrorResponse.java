package com.example.GoSonGim_BE.global.dto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public record ApiErrorResponse(
    boolean success,
    int status,
    String message,
    String timestamp,
    ErrorDetail error
) {
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    public record ErrorDetail(
        String code
    ) {}
    
    public static ApiErrorResponse error(int status, String message, String errorCode) {
        return new ApiErrorResponse(
            false, 
            status, 
            message, 
            LocalDateTime.now().format(TIMESTAMP_FORMATTER), 
            new ErrorDetail(errorCode)
        );
    }
}
