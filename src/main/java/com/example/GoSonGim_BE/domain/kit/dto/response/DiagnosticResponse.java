package com.example.GoSonGim_BE.domain.kit.dto.response;

import java.util.List;

public record DiagnosticResponse(
    String recognizedText,
    Scores scores,
    double overallScore,
    List<RecommendedKit> recommendedKits
) {
    
    public record Scores(
        double accuracy,
        double fluency,
        double completeness,
        double prosody
    ) {}
    
    public record RecommendedKit(
        Long kitId,
        String kitName
    ) {}
}