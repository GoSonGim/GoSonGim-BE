package com.example.GoSonGim_BE.domain.kit.dto.response;

public record PronunciationAssessmentResponse(
    String recognizedText,
    double accuracy,
    double fluency,
    double completeness,
    double prosody
) {
}