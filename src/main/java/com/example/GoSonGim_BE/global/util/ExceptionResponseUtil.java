package com.example.GoSonGim_BE.global.util;

import com.example.GoSonGim_BE.global.dto.ApiErrorResponse;
import com.example.GoSonGim_BE.global.exception.BaseException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class ExceptionResponseUtil {
    
    /**
     * BaseException 처리
     */
    public static ResponseEntity<ApiErrorResponse> createErrorResponse(BaseException e, HttpStatus status) {
        ApiErrorResponse response = ApiErrorResponse.error(
            status.value(),
            e.getMessage(),
            e.getErrorCode()
        );
        return ResponseEntity.status(status).body(response);
    }
    
    /**
     * 예외 처리 (메시지, 에러코드 직접 지정)
     */
    public static ResponseEntity<ApiErrorResponse> createErrorResponse(
            String message, String errorCode, HttpStatus status) {
        ApiErrorResponse response = ApiErrorResponse.error(
            status.value(),
            message,
            errorCode
        );
        return ResponseEntity.status(status).body(response);
    }
}