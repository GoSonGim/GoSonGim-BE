package com.example.GoSonGim_BE.domain.openai.service;

import java.util.List;
import java.util.Map;

/**
 * OpenAI 서비스 인터페이스
 */
public interface OpenAIService {
    
    /**
     * 첫 번째 질문 생성
     * @param situationDescription 상황극 설명
     * @param situationName 상황극명
     * @return 생성된 첫 질문
     */
    String generateFirstQuestion(String situationDescription, String situationName);
    
    /**
     * 답변 평가
     * @param situationDescription 상황극 설명
     * @param question 질문
     * @param answer 사용자 답변
     * @param turnIndex 현재 턴 인덱스
     * @return 평가 결과 (isSuccess, score, feedback)
     */
    EvaluationResult evaluateAnswer(String situationDescription, String question, String answer, int turnIndex);
    
    /**
     * 다음 질문 생성
     * @param situationDescription 상황극 설명
     * @param conversationHistory 대화 내역
     * @param turnIndex 다음 턴 인덱스
     * @return 생성된 다음 질문
     */
    String generateNextQuestion(String situationDescription, List<Map<String, Object>> conversationHistory, int turnIndex);
    
    /**
     * 최종 피드백 생성
     * @param situationDescription 상황극 설명
     * @param averageScore 평균 점수
     * @param lastEvaluationSuccess 마지막 평가 성공 여부
     * @return 생성된 최종 피드백 (한 문장)
     */
    String generateFinalFeedback(String situationDescription, float averageScore, boolean lastEvaluationSuccess);
    
    /**
     * 평가 결과
     */
    record EvaluationResult(
        boolean isSuccess,
        float score,
        String feedback
    ) {}
}

