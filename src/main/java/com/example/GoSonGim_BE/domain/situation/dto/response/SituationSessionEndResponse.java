package com.example.GoSonGim_BE.domain.situation.dto.response;

/**
 * 상황극 학습 세션 종료 응답 DTO
 */
public record SituationSessionEndResponse(
    Long situationLogId,
    SituationSessionReplyResponse.FinalSummary finalSummary
) {}

