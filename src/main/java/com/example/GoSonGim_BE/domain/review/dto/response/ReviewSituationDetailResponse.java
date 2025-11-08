package com.example.GoSonGim_BE.domain.review.dto.response;

import java.util.List;

/**
 * 상황극 복습 상세 조회 응답
 */
public record ReviewSituationDetailResponse(
    Long recordingId,
    SituationInfo situation,
    EvaluationInfo evaluation,
    List<ConversationTurn> conversation
) {
    public record SituationInfo(
        Long id,
        String name
    ) {}
    
    public record EvaluationInfo(
        Integer score,
        String feedback
    ) {}
    
    public record ConversationTurn(
        String question,
        Answer answer
    ) {}
    
    public record Answer(
        String text,
        String audioUrl,
        Integer audioExpiresIn
    ) {}
}

