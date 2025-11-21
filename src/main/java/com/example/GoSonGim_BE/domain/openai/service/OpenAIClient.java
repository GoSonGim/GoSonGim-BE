package com.example.GoSonGim_BE.domain.openai.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.example.GoSonGim_BE.domain.openai.exception.OpenAIExceptions;
import com.example.GoSonGim_BE.global.config.OpenAIProperties;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * OpenAI API 클라이언트
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OpenAIClient {
    
    private final OpenAIProperties openAIProperties;
    private final RestTemplate restTemplate;
    
    private static final String CHAT_ENDPOINT = "/chat/completions";
    private static final double TEMPERATURE = 0.5;
    private static final int MAX_TOKENS = 200;
    private static final double TOP_P = 0.9;
    
    /**
     * OpenAI API 호출
     */
    public String call(String systemPrompt, String userMessage) {
        try {
            String url = openAIProperties.getBaseUrl() + CHAT_ENDPOINT;
            
            HttpHeaders headers = createHeaders();
            Map<String, Object> requestBody = createRequestBody(systemPrompt, userMessage);
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            
            ParameterizedTypeReference<Map<String, Object>> responseType = 
                new ParameterizedTypeReference<Map<String, Object>>() {};
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                url, HttpMethod.POST, request, responseType
            );
            
            return extractContent(response.getBody());
            
        } catch (OpenAIExceptions.OpenAIEmptyResponseException e) {
            throw e;
        } catch (Exception e) {
            log.error("OpenAI API 호출 실패", e);
            throw new OpenAIExceptions.OpenAIServiceException(e.getMessage(), e);
        }
    }
    
    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openAIProperties.getKey());
        return headers;
    }
    
    private Map<String, Object> createRequestBody(String systemPrompt, String userMessage) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", openAIProperties.getModel());
        requestBody.put("messages", createMessages(systemPrompt, userMessage));
        requestBody.put("temperature", TEMPERATURE);
        requestBody.put("max_tokens", MAX_TOKENS);
        requestBody.put("top_p", TOP_P);
        return requestBody;
    }
    
    private List<Map<String, String>> createMessages(String systemPrompt, String userMessage) {
        List<Map<String, String>> messages = new ArrayList<>();
        
        Map<String, String> systemMsg = new HashMap<>();
        systemMsg.put("role", "system");
        systemMsg.put("content", systemPrompt);
        messages.add(systemMsg);
        
        Map<String, String> userMsg = new HashMap<>();
        userMsg.put("role", "user");
        userMsg.put("content", userMessage);
        messages.add(userMsg);
        
        return messages;
    }
    
    private String extractContent(Map<String, Object> responseBody) {
        if (responseBody == null) {
            log.warn("OpenAI API 응답이 null입니다");
            throw new OpenAIExceptions.OpenAIEmptyResponseException();
        }
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> choices = (List<Map<String, Object>>) responseBody.get("choices");
        
        if (choices == null || choices.isEmpty()) {
            log.warn("OpenAI API 응답에 choices가 없습니다: {}", responseBody);
            throw new OpenAIExceptions.OpenAIEmptyResponseException();
        }
        
        Map<String, Object> firstChoice = choices.get(0);
        @SuppressWarnings("unchecked")
        Map<String, Object> message = (Map<String, Object>) firstChoice.get("message");
        String content = (String) message.get("content");
        
        return content != null ? content.trim() : "";
    }
}

