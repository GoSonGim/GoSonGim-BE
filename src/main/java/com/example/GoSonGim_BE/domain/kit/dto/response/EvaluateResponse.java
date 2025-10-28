package com.example.GoSonGim_BE.domain.kit.dto.response;

public record EvaluateResponse(
    Long kitStageId,
    String targetWord,
    PronunciationScore pronunciation,
    double evaluationScore,
    boolean isSuccess
) {
    
    public record PronunciationScore(
        double accuracy,
        double fluency,
        double completeness,
        double prosody
    ) {}
}