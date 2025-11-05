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
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.example.GoSonGim_BE.domain.openai.exception.OpenAIExceptions;
import com.example.GoSonGim_BE.global.config.OpenAIProperties;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * OpenAI 서비스 구현체
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OpenAIServiceImpl implements OpenAIService {
    
    private final OpenAIProperties openAIProperties;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    private static final String CHAT_ENDPOINT = "/chat/completions";
    
    @Override
    public String generateFirstQuestion(String situationDescription, String situationName) {
        String systemPrompt = String.format(
            "상황극 첫 질문 생성. 설명: %s, 이름: %s. " +
            "친절한 인사와 질문만 반환. 질문만 출력.",
            situationDescription, situationName
        );
        
        String userMessage = "첫 질문 생성";
        
        return callOpenAI(systemPrompt, userMessage);
    }
    
    @Override
    public EvaluationResult evaluateAnswer(String situationDescription, String question, String answer, int turnIndex) {
        String systemPrompt = String.format(
            "조음 장애 학습자 말하기 평가. 상황: %s\n" +
            "평가: 단어 정확도(70점) - 발음 정확도 중시. 예: 사과O 사구X. 맥락(30점). " +
            "50점 이상 통과. 피드백은 한 문장만.\n" +
            "JSON: {\"isSuccess\": boolean, \"score\": 0-100, \"feedback\": \"한 문장\"}",
            situationDescription
        );
        
        String userMessage = String.format("질문: %s\n답변: %s", question, answer);
        
        String response = callOpenAI(systemPrompt, userMessage);
        
        try {
            Map<String, Object> evaluationMap = objectMapper.readValue(response, 
                objectMapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class));
            boolean isSuccess = Boolean.TRUE.equals(evaluationMap.get("isSuccess"));
            float score = ((Number) evaluationMap.getOrDefault("score", 0)).floatValue();
            String feedback = (String) evaluationMap.getOrDefault("feedback", "평가를 완료했습니다.");
            
            return new EvaluationResult(isSuccess, score, feedback);
        } catch (Exception e) {
            log.error("OpenAI 평가 결과 파싱 실패", e);
            throw new OpenAIExceptions.OpenAIResponseParseException(e.getMessage());
        }
    }
    
    @Override
    public String generateNextQuestion(String situationDescription, List<Map<String, Object>> conversationHistory, int turnIndex) {
        String systemPrompt = String.format(
            "상황극 질문 생성. 설명: %s. 상황에서 할 법한 한 문장 질문만 생성. 이전 대화를 참고하되 꼬리를 물지 말고 다양한 질문.",
            situationDescription
        );
        
        String history = conversationHistory.stream()
            .map(turn -> {
                String question = (String) turn.get("question");
                String answer = (String) turn.get("answer");
                return (question != null ? "Q: " + question + " " : "") +
                       (answer != null ? "A: " + answer + " " : "");
            })
            .filter(s -> !s.isEmpty())
            .reduce("", (a, b) -> a + b)
            .trim();
        
        String userMessage = String.format("대화: %s\n다음 질문:", history);
        
        return callOpenAI(systemPrompt, userMessage);
    }
    
    @Override
    public String generateFinalFeedback(String situationDescription, float averageScore, boolean lastEvaluationSuccess) {
        String systemPrompt = String.format(
            "조음 장애 학습자 최종 피드백 생성. 상황: %s\n" +
            "평균 점수: %.1f점, 마지막 평가: %s\n" +
            "격려하는 한 문장 피드백만 생성.",
            situationDescription, averageScore, lastEvaluationSuccess ? "성공" : "실패"
        );
        
        String userMessage = "최종 피드백:";
        
        return callOpenAI(systemPrompt, userMessage);
    }
    
    /**
     * OpenAI API 호출
     */
    private String callOpenAI(String systemPrompt, String userMessage) {
        try {
            String url = openAIProperties.getBaseUrl() + CHAT_ENDPOINT;
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(openAIProperties.getKey());
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", openAIProperties.getModel());
            
            List<Map<String, String>> messages = new ArrayList<>();
            Map<String, String> systemMessage = new HashMap<>();
            systemMessage.put("role", "system");
            systemMessage.put("content", systemPrompt);
            messages.add(systemMessage);
            
            Map<String, String> userMsg = new HashMap<>();
            userMsg.put("role", "user");
            userMsg.put("content", userMessage);
            messages.add(userMsg);
            
            requestBody.put("messages", messages);
            requestBody.put("temperature", 0.3);
            requestBody.put("max_tokens", 100);    // 더 짧게
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            
            ParameterizedTypeReference<Map<String, Object>> responseType = 
                new ParameterizedTypeReference<Map<String, Object>>() {};
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                url, HttpMethod.POST, request, responseType
            );
            
            Map<String, Object> responseBody = response.getBody();
            if (responseBody != null) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> choices = (List<Map<String, Object>>) responseBody.get("choices");
                if (choices != null && !choices.isEmpty()) {
                    Map<String, Object> firstChoice = choices.get(0);
                    @SuppressWarnings("unchecked")
                    Map<String, Object> message = (Map<String, Object>) firstChoice.get("message");
                    String content = (String) message.get("content");
                    return content != null ? content.trim() : "";
                }
            }
            
            log.warn("OpenAI API 응답에 content가 없습니다: {}", responseBody);
            throw new OpenAIExceptions.OpenAIEmptyResponseException();
            
        } catch (OpenAIExceptions.OpenAIEmptyResponseException | OpenAIExceptions.OpenAIResponseParseException e) {
            throw e;
        } catch (Exception e) {
            log.error("OpenAI API 호출 실패", e);
            throw new OpenAIExceptions.OpenAIServiceException(e.getMessage(), e);
        }
    }
}

