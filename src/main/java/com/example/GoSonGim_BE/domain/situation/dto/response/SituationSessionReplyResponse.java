package com.example.GoSonGim_BE.domain.situation.dto.response;

/**
 * 상황극 학습 세션 답변 및 평가 응답 DTO
 */
public record SituationSessionReplyResponse(
    Evaluation evaluation,
    String nextQuestion,
    Integer turnIndex,
    Boolean isSessionEnd,
    FinalSummary finalSummary
) {
    /**
     * 평가 결과
     */
    public record Evaluation(
        Boolean isSuccess,
        String feedback,
        Float score
    ) {}
    
    /**
     * 최종 요약 (세션 종료 시에만 제공)
     */
    public record FinalSummary(
        Float averageScore,
        String finalFeedback
    ) {}
}

