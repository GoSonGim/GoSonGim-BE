package com.example.GoSonGim_BE.domain.kit.service.impl;

import com.example.GoSonGim_BE.domain.kit.dto.response.PronunciationAssessmentResponse;
import com.example.GoSonGim_BE.domain.kit.service.PronunciationAssessmentService;
import com.microsoft.cognitiveservices.speech.*;
import com.microsoft.cognitiveservices.speech.audio.AudioConfig;
import com.microsoft.cognitiveservices.speech.audio.AudioStreamFormat;
import com.microsoft.cognitiveservices.speech.audio.PullAudioInputStream;
import com.microsoft.cognitiveservices.speech.audio.PullAudioInputStreamCallback;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.InputStream;

@Slf4j
@Service
public class PronunciationAssessmentServiceImpl implements PronunciationAssessmentService {

    @Value("${azure.speech.key}")
    private String speechKey;

    @Value("${azure.speech.region}")
    private String speechRegion;

    @Override
    public PronunciationAssessmentResponse assessPronunciation(InputStream audioStream, String targetWord) {
        try {
            // 1️⃣ Azure Speech SDK 설정
            SpeechConfig config = SpeechConfig.fromSubscription(speechKey, speechRegion);
            config.setSpeechRecognitionLanguage("ko-KR");
            config.setOutputFormat(OutputFormat.Detailed);
            config.setProperty(PropertyId.SpeechServiceResponse_RequestDetailedResultTrueFalse, "true");

            // InputStream → PullAudioInputStream 변환
            PullAudioInputStreamCallback callback = new PullAudioInputStreamCallback() {
                @Override
                public int read(byte[] dataBuffer) {
                    try {
                        return audioStream.read(dataBuffer);
                    } catch (Exception e) {
                        return -1; // 오류 또는 EOF
                    }
                }

                @Override
                public void close() {
                    try {
                        audioStream.close();
                    } catch (Exception ignored) {
                    }
                }
            };

// 2️⃣ PCM 형식 지정 (WAV 기본값: 16kHz, 16bit, mono)
            AudioStreamFormat format = AudioStreamFormat.getWaveFormatPCM(16000, (short) 16, (short) 1);

// 3️⃣ AudioConfig 생성
            AudioConfig audioConfig = AudioConfig.fromStreamInput(PullAudioInputStream.createPullStream(callback, format));

            // 3️⃣ 발음 평가 기준 정의
            PronunciationAssessmentConfig pronConfig = new PronunciationAssessmentConfig(
                    targetWord,
                    PronunciationAssessmentGradingSystem.HundredMark,
                    PronunciationAssessmentGranularity.Phoneme,
                    true
            );
            pronConfig.enableProsodyAssessment();

            // 4️⃣ 평가 실행
            SpeechRecognizer recognizer = new SpeechRecognizer(config, audioConfig);
            pronConfig.applyTo(recognizer);

            log.info("[Azure] 발음 평가 시작 - targetWord: {}", targetWord);
            SpeechRecognitionResult result = recognizer.recognizeOnceAsync().get();

            if (result.getReason() == ResultReason.RecognizedSpeech) {
                PronunciationAssessmentResult score = PronunciationAssessmentResult.fromResult(result);

                PronunciationAssessmentResponse response = new PronunciationAssessmentResponse(
                        result.getText(),
                        score.getAccuracyScore(),
                        score.getFluencyScore(),
                        score.getCompletenessScore(),
                        score.getProsodyScore()
                );

                log.info("[Azure] 발음 평가 완료: {}", response);
                recognizer.close();
                audioConfig.close();
                config.close();
                return response;
            } else {
                log.warn("[Azure] 발음 인식 실패: {}", result.getReason());
                recognizer.close();
                audioConfig.close();
                config.close();
                throw new RuntimeException("Azure 발음 인식 실패: " + result.getReason());
            }

        } catch (Exception e) {
            log.error("[Azure] 발음 평가 중 오류 발생", e);
            throw new RuntimeException("Azure 발음 평가 실패: " + e.getMessage(), e);
        }
    }
}