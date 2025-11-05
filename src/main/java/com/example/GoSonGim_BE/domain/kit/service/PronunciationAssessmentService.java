package com.example.GoSonGim_BE.domain.kit.service;

import com.example.GoSonGim_BE.domain.kit.dto.response.PronunciationAssessmentResponse;
import com.example.GoSonGim_BE.domain.kit.dto.response.SpeechToTextResponse;

import java.io.InputStream;

public interface PronunciationAssessmentService {

    /**
     * Azure Speech SDK를 활용한 발음 평가 수행
     *
     * @param audioStream  평가할 음성 파일의 InputStream
     * @param targetWord   목표 문장 또는 단어
     * @return 평가 결과 (정확도, 유창성, 완성도, 운율 점수)
     */
    PronunciationAssessmentResponse assessPronunciation(InputStream audioStream, String targetWord);
    
    /**
     * 음성 인식(STT) 수행 (발음 평가 없이)
     *
     * @param audioStream 평가할 음성 파일의 InputStream
     * @return 인식된 텍스트와 신뢰도
     */
    SpeechToTextResponse transcribe(InputStream audioStream);
}