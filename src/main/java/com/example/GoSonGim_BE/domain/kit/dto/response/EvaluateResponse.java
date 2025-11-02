package com.example.GoSonGim_BE.domain.kit.dto.response;

import java.util.List;

public record EvaluateResponse(
    List<IndividualResult> individualResults,
    OverallResult overallResult
) {
    
    public record IndividualResult(
        Long kitStageId,
        String targetWord,
        String recognizedText,
        PronunciationScore pronunciation,
        double evaluationScore,
        boolean isSuccess
    ) {}
    
    public record OverallResult(
        double overallScore,
        String overallFeedback
    ) {}
    
    public record PronunciationScore(
        double accuracy,
        double fluency,
        double completeness,
        double prosody
    ) {}
}