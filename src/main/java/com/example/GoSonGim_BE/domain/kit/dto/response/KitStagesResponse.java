package com.example.GoSonGim_BE.domain.kit.dto.response;

import java.util.List;

public record KitStagesResponse(
    Long kitId,
    String kitName,
    String kitCategory,
    int totalStages,
    List<Stage> stages
) {
    public record Stage(
        Long stageId,
        String stageName
    ) {}
}
