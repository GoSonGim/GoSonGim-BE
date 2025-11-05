package com.example.GoSonGim_BE.domain.kit.dto.response;

/**
 * 음성 인식(STT) 결과 응답 DTO
 */
public record SpeechToTextResponse(
    String recognizedText,
    double confidence
) {}

