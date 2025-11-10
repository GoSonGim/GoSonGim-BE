package com.example.GoSonGim_BE.domain.review.dto.response;

import java.time.LocalDateTime;

/**
 * 일별 학습 기록 아이템 응답
 */
public record ReviewDailyItemResponse(
    String type, // "KIT" 또는 "SITUATION"
    Long id, // kitId 또는 situationId
    String name, // kitName 또는 situationName
    Long recordingId, // kitId (KIT인 경우) 또는 situationLog.id (SITUATION인 경우)
    LocalDateTime createdAt
) {
    public static ReviewDailyItemResponse fromKit(com.example.GoSonGim_BE.domain.kit.entity.KitStageLog log) {
        return new ReviewDailyItemResponse(
            "KIT",
            log.getKitStage().getKit().getId(),
            log.getKitStage().getKit().getKitName(),
            log.getKitStage().getKit().getId(), // recordingId에 kitId 사용
            log.getCreatedAt()
        );
    }
    
    public static ReviewDailyItemResponse fromSituation(com.example.GoSonGim_BE.domain.situation.entity.SituationLog log) {
        return new ReviewDailyItemResponse(
            "SITUATION",
            log.getSituation().getId(),
            log.getSituation().getSituationName(),
            log.getId(), // recordingId에 situationLog.id 사용
            log.getCreatedAt()
        );
    }
}

