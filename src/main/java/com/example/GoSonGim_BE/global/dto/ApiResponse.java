package com.example.GoSonGim_BE.global.dto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public record ApiResponse<T>(
    boolean success,
    int status,
    String message,
    String timestamp,
    T result
) {
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    public static <T> ApiResponse<T> success(int status, String message, T result) {
        return new ApiResponse<>(
            true, 
            status, 
            message, 
            LocalDateTime.now().format(TIMESTAMP_FORMATTER), 
            result
        );
    }
}