package com.example.GoSonGim_BE.global.dto;

import java.time.LocalDateTime;

public record ApiResponse<T>(
    boolean success,
    int status,
    String message,
    String timestamp,
    T result
) {
    public static <T> ApiResponse<T> success(int status, String message, T result) {
        return new ApiResponse<>(true, status, message, LocalDateTime.now().toString(), result);
    }
}