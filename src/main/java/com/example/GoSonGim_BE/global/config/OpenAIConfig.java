package com.example.GoSonGim_BE.global.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * OpenAI 설정
 */
@Configuration
public class OpenAIConfig {
    
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(5));   // 연결 타임아웃 단축
        factory.setReadTimeout(Duration.ofSeconds(15));     // 읽기 타임아웃 단축
        
        return builder
            .requestFactory(() -> factory)
            .build();
    }
}

