package com.example.GoSonGim_BE.domain.situation.dto.response;

/**
 * 상황극 음성 인식(STT) 응답 DTO
 */
public record SituationSpeechToTextResponse(
    String recognizedText,
    double confidence
) {}

