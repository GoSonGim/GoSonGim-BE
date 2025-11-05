package com.example.GoSonGim_BE.domain.openai.exception;

import com.example.GoSonGim_BE.global.exception.BaseException;

/**
 * OpenAI 도메인 예외 클래스
 */
public class OpenAIExceptions {
    
    /**
     * OpenAI API 호출 실패 예외
     */
    public static class OpenAIServiceException extends BaseException {
        public OpenAIServiceException(String message) {
            super("OpenAI API 호출 중 오류가 발생했습니다: " + message, "OPENAI_SERVICE_ERROR");
        }
        
        public OpenAIServiceException(String message, Throwable cause) {
            super("OpenAI API 호출 중 오류가 발생했습니다: " + message, "OPENAI_SERVICE_ERROR");
        }
    }
    
    /**
     * OpenAI API 응답 파싱 실패 예외
     */
    public static class OpenAIResponseParseException extends BaseException {
        public OpenAIResponseParseException(String message) {
            super("OpenAI 응답 파싱 실패: " + message, "OPENAI_RESPONSE_PARSE_ERROR");
        }
    }
    
    /**
     * OpenAI API 응답이 비어있는 예외
     */
    public static class OpenAIEmptyResponseException extends BaseException {
        public OpenAIEmptyResponseException() {
            super("OpenAI API 응답이 비어있습니다.", "OPENAI_EMPTY_RESPONSE");
        }
    }
}

