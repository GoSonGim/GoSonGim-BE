package com.example.GoSonGim_BE.global.exception;

import com.example.GoSonGim_BE.domain.auth.exception.AuthExceptions;
import com.example.GoSonGim_BE.domain.openai.exception.OpenAIExceptions;
import com.example.GoSonGim_BE.domain.review.exception.ReviewExceptions;
import com.example.GoSonGim_BE.domain.situation.exception.SituationExceptions;
import com.example.GoSonGim_BE.domain.users.exception.UserExceptions;
import com.example.GoSonGim_BE.global.dto.ApiErrorResponse;
import com.example.GoSonGim_BE.global.util.ExceptionResponseUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 전역 예외 처리 핸들러
 * 
 * - BaseException 기반의 커스텀 예외를 일관된 형식으로 처리
 * - Validation 예외 처리
 * - 예상치 못한 예외에 대한 Fallback 처리
 */
@Slf4j
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
        
        // JWT 도메인 예외
        AuthExceptions.InvalidRefreshTokenException.class,
        AuthExceptions.RefreshTokenExpiredException.class,
        AuthExceptions.RefreshTokenRevokedException.class,
        AuthExceptions.RefreshTokenNotFoundException.class,
        
        // OAuth 도메인 예외
        AuthExceptions.InvalidRedirectUriException.class,
        AuthExceptions.OAuthTokenInvalidException.class,
        
        // User 도메인 예외
        UserExceptions.UserNotFoundException.class,
        UserExceptions.UserAlreadyDeletedException.class,
        UserExceptions.UserNotDeletedException.class,
        
        // Situation 도메인 예외
        SituationExceptions.SituationNotFoundException.class,
        SituationExceptions.SessionNotFoundException.class,
        SituationExceptions.SessionAccessDeniedException.class,
        SituationExceptions.SessionNotActiveException.class,
        SituationExceptions.SessionInvalidException.class,
        SituationExceptions.SpeechToTextException.class,
        
        // Review 도메인 예외
        ReviewExceptions.NoLearningHistoryException.class,
        ReviewExceptions.InvalidQueryParameterException.class,
        ReviewExceptions.SituationLogNotFoundException.class,
        ReviewExceptions.SituationLogAccessDeniedException.class,
        ReviewExceptions.InvalidConversationDataException.class,
        
        // OpenAI 도메인 예외
        OpenAIExceptions.OpenAIServiceException.class,
        OpenAIExceptions.OpenAIResponseParseException.class,
        OpenAIExceptions.OpenAIEmptyResponseException.class
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
     * 모든 예외에 대한 Fallback 처리
     * 예상치 못한 예외가 발생했을 때 적절한 응답 반환
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleException(Exception e) {
        log.error("Unexpected exception occurred", e);
        
        // DB 관련 예외
        if (e.getClass().getName().contains("SQLException") || 
            e.getClass().getName().contains("DataAccess")) {
            return ExceptionResponseUtil.createErrorResponse(
                "데이터베이스 오류가 발생했습니다.",
                "DATABASE_ERROR",
                HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
        
        // 기타 예외
        return ExceptionResponseUtil.createErrorResponse(
            e.getMessage() != null ? e.getMessage() : "서버 오류가 발생했습니다.",
            "INTERNAL_SERVER_ERROR",
            HttpStatus.INTERNAL_SERVER_ERROR
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
            return HttpStatus.GONE;  // 410 Gone - 리소스가 영구적으로 삭제됨
        }
        
        // JWT 도메인 예외
        if (e instanceof AuthExceptions.InvalidRefreshTokenException ||
            e instanceof AuthExceptions.RefreshTokenExpiredException ||
            e instanceof AuthExceptions.RefreshTokenRevokedException ||
            e instanceof AuthExceptions.RefreshTokenNotFoundException) {
            return HttpStatus.UNAUTHORIZED;
        }
        
        // OAuth 도메인 예외
        if (e instanceof AuthExceptions.InvalidRedirectUriException ||
            e instanceof AuthExceptions.OAuthTokenInvalidException) {
            return HttpStatus.BAD_REQUEST;
        }
        
        // User 도메인 예외
        if (e instanceof UserExceptions.UserNotFoundException) {
            return HttpStatus.NOT_FOUND;
        }
        if (e instanceof UserExceptions.UserAlreadyDeletedException) {
            return HttpStatus.CONFLICT;
        }
        if (e instanceof UserExceptions.UserNotDeletedException) {
            return HttpStatus.BAD_REQUEST;
        }
        
        // Situation 도메인 예외
        if (e instanceof SituationExceptions.SituationNotFoundException ||
            e instanceof SituationExceptions.SessionNotFoundException) {
            return HttpStatus.NOT_FOUND;
        }
        if (e instanceof SituationExceptions.SessionAccessDeniedException) {
            return HttpStatus.FORBIDDEN;
        }
        if (e instanceof SituationExceptions.SessionNotActiveException ||
            e instanceof SituationExceptions.SessionInvalidException) {
            return HttpStatus.BAD_REQUEST;
        }
        if (e instanceof SituationExceptions.SpeechToTextException) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
        
        // Review 도메인 예외
        if (e instanceof ReviewExceptions.NoLearningHistoryException ||
            e instanceof ReviewExceptions.SituationLogNotFoundException) {
            return HttpStatus.NOT_FOUND;
        }
        if (e instanceof ReviewExceptions.InvalidQueryParameterException ||
            e instanceof ReviewExceptions.InvalidConversationDataException) {
            return HttpStatus.BAD_REQUEST;
        }
        if (e instanceof ReviewExceptions.SituationLogAccessDeniedException) {
            return HttpStatus.FORBIDDEN;
        }
        
        // OpenAI 도메인 예외
        if (e instanceof OpenAIExceptions.OpenAIServiceException ||
            e instanceof OpenAIExceptions.OpenAIResponseParseException ||
            e instanceof OpenAIExceptions.OpenAIEmptyResponseException) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
        
        return HttpStatus.BAD_REQUEST;
    }
}
