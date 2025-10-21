package com.example.GoSonGim_BE.domain.auth.exception;

import com.example.GoSonGim_BE.global.dto.ApiErrorResponse;
import com.example.GoSonGim_BE.global.exception.BaseException;
import com.example.GoSonGim_BE.global.util.ExceptionResponseUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class AuthExceptionHandler {
    
    /**
     * 인증 관련 모든 예외 통합 처리
     */
    @ExceptionHandler({
        AuthExceptions.EmailAlreadyUsedException.class,
        AuthExceptions.InvalidEmailFormatException.class,
        AuthExceptions.InvalidCredentialsException.class,
        AuthExceptions.UserDisabledException.class
    })
    public ResponseEntity<ApiErrorResponse> handleAuthExceptions(BaseException e) {
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
     * 예외 타입에 따른 HTTP 상태 코드 결정
     */
    private HttpStatus determineStatus(BaseException e) {
        if (e instanceof AuthExceptions.EmailAlreadyUsedException) {
            return HttpStatus.CONFLICT;
        }
        if (e instanceof AuthExceptions.InvalidCredentialsException) {
            return HttpStatus.UNAUTHORIZED;
        }
        if (e instanceof AuthExceptions.UserDisabledException) {
            return HttpStatus.LOCKED;
        }
        return HttpStatus.BAD_REQUEST;
    }
}