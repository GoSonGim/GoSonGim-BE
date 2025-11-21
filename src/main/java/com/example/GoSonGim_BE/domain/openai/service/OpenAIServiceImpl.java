package com.example.GoSonGim_BE.domain.openai.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.example.GoSonGim_BE.domain.openai.exception.OpenAIExceptions;
import com.example.GoSonGim_BE.domain.openai.prompt.SituationPromptTemplates;
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
    
    private final OpenAIClient openAIClient;
    private final ObjectMapper objectMapper;
    
    @Override
    public String generateFirstQuestion(String situationDescription, String situationName) {
        String systemPrompt = SituationPromptTemplates.buildFirstQuestionPrompt(situationDescription);
        String userMessage = SituationPromptTemplates.UserMessages.FIRST_QUESTION;
        
        String response = openAIClient.call(systemPrompt, userMessage);
        return removeQuotes(response);
    }
    
    @Override
    public EvaluationResult evaluateAnswer(String situationDescription, String question, String answer, int turnIndex) {
        String systemPrompt = SituationPromptTemplates.buildEvaluationPrompt(situationDescription, turnIndex);
        String userMessage = SituationPromptTemplates.UserMessages.evaluation(question, answer);
        
        String response = openAIClient.call(systemPrompt, userMessage);
        
        try {
            Map<String, Object> evaluationMap = objectMapper.readValue(response, 
                objectMapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class));
            boolean isSuccess = Boolean.TRUE.equals(evaluationMap.get("isSuccess"));
            float score = ((Number) evaluationMap.getOrDefault("score", 0)).floatValue();
            String feedback = (String) evaluationMap.getOrDefault("feedback", "평가를 완료했습니다.");
            
            return new EvaluationResult(isSuccess, score, feedback);
        } catch (Exception e) {
            log.error("OpenAI 평가 결과 파싱 실패. 응답: {}", response, e);
            throw new OpenAIExceptions.OpenAIResponseParseException(e.getMessage());
        }
    }
    
    @Override
    public String generateNextQuestion(String situationDescription, List<Map<String, Object>> conversationHistory, int turnIndex) {
        String systemPrompt = SituationPromptTemplates.buildNextQuestionPrompt(situationDescription, turnIndex);
        String history = formatConversationHistory(conversationHistory);
        String userMessage = SituationPromptTemplates.UserMessages.nextQuestion(history);
        
        String response = openAIClient.call(systemPrompt, userMessage);
        return removeQuotes(response);
    }
    
    @Override
    public String generateFinalFeedback(String situationDescription, float averageScore, boolean lastEvaluationSuccess) {
        String systemPrompt = SituationPromptTemplates.buildFinalFeedbackPrompt(
            situationDescription, averageScore, lastEvaluationSuccess);
        String userMessage = SituationPromptTemplates.UserMessages.FINAL_FEEDBACK;
        
        String response = openAIClient.call(systemPrompt, userMessage);
        return removeQuotes(response);
    }
    
    /**
     * 대화 내역을 문자열로 포맷팅
     */
    private String formatConversationHistory(List<Map<String, Object>> conversationHistory) {
        return conversationHistory.stream()
            .map(turn -> {
                String question = (String) turn.get("question");
                String answer = (String) turn.get("answer");
                return (question != null ? "Q: " + question + " " : "") +
                       (answer != null ? "A: " + answer + " " : "");
            })
            .filter(s -> !s.isEmpty())
            .reduce("", (a, b) -> a + b)
            .trim();
    }
    
    /**
     * 응답에서 앞뒤 따옴표 제거
     */
    private String removeQuotes(String response) {
        if (response == null || response.isBlank()) {
            return "";
        }
        
        String cleaned = response.trim();
        
        // 앞뒤 따옴표 제거 (큰따옴표, 작은따옴표)
        while ((cleaned.startsWith("\"") && cleaned.endsWith("\"")) ||
               (cleaned.startsWith("'") && cleaned.endsWith("'"))) {
            cleaned = cleaned.substring(1, cleaned.length() - 1).trim();
        }
        
        return cleaned;
    }
}

