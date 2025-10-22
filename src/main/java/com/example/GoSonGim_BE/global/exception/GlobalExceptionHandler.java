package com.example.GoSonGim_BE.global.exception;

import com.example.GoSonGim_BE.domain.auth.exception.AuthExceptions;
import com.example.GoSonGim_BE.domain.users.exception.UserExceptions;
import com.example.GoSonGim_BE.global.dto.ApiErrorResponse;
import com.example.GoSonGim_BE.global.util.ExceptionResponseUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 전역 예외 처리 핸들러
 * 
 * @author GoSonGim Team
 * @since 1.0.0
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    /**
     * 도메인 예외 통합 처리
     */
    @ExceptionHandler({
        // Auth 도메인 예외
        AuthExceptions.EmailAlreadyUsedException.class,
        AuthExceptions.InvalidEmailFormatException.class,
        AuthExceptions.InvalidCredentialsException.class,
        AuthExceptions.UserDeletedException.class,
        
        // User 도메인 예외
        UserExceptions.UserAlreadyDeletedException.class,
        UserExceptions.UserNotDeletedException.class
    })
    public ResponseEntity<ApiErrorResponse> handleDomainExceptions(BaseException e) {
        HttpStatus status = determineStatus(e);
        return ExceptionResponseUtil.createErrorResponse(e, status);
    }
    
    /**
     * Validation 예외 처리
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().get(0).getDefaultMessage();
        return ExceptionResponseUtil.createErrorResponse(message, "VALIDATION_ERROR", HttpStatus.BAD_REQUEST);
    }
    
    /**
     * 일반적인 RuntimeException 처리
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArgumentException(IllegalArgumentException e) {
        return ExceptionResponseUtil.createErrorResponse(
            e.getMessage(), 
            "ILLEGAL_ARGUMENT", 
            HttpStatus.BAD_REQUEST
        );
    }
    
    /**
     * 일반적인 IllegalStateException 처리
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalStateException(IllegalStateException e) {
        return ExceptionResponseUtil.createErrorResponse(
            e.getMessage(), 
            "ILLEGAL_STATE", 
            HttpStatus.BAD_REQUEST
        );
    }
    
    /**
     * 예외 타입에 따른 HTTP 상태 코드 결정
     */
    private HttpStatus determineStatus(BaseException e) {
        // Auth 도메인 예외
        if (e instanceof AuthExceptions.EmailAlreadyUsedException) {
            return HttpStatus.CONFLICT;
        }
        if (e instanceof AuthExceptions.InvalidCredentialsException) {
            return HttpStatus.UNAUTHORIZED;
        }
        if (e instanceof AuthExceptions.UserDeletedException) {
            return HttpStatus.GONE;  // 410 Gone - 리소천가 영구적으로 삭제됨
        }
        
        // User 도메인 예외
        if (e instanceof UserExceptions.UserAlreadyDeletedException) {
            return HttpStatus.CONFLICT;
        }
        if (e instanceof UserExceptions.UserNotDeletedException) {
            return HttpStatus.BAD_REQUEST;
        }
        
        return HttpStatus.BAD_REQUEST;
    }
}
