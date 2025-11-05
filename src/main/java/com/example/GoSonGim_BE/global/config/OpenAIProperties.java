package com.example.GoSonGim_BE.global.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

/**
 * OpenAI API 설정 Properties
 */
@Component
@ConfigurationProperties(prefix = "openai.api")
@Data
public class OpenAIProperties {
    
    /**
     * OpenAI API Key
     */
    private String key;
    
    /**
     * OpenAI API Base URL
     */
    private String baseUrl = "https://api.openai.com/v1";
    
    /**
     * 사용할 모델명 (기본값: gpt-4o-mini)
     */
    private String model = "gpt-4o-mini";
    
    /**
     * API 호출 타임아웃 (밀리초, 기본값: 30000)
     */
    private int timeout = 30000;
}

