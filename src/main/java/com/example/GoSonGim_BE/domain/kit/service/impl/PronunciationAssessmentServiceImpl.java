package com.example.GoSonGim_BE.domain.kit.service.impl;

import com.example.GoSonGim_BE.domain.kit.dto.response.PronunciationAssessmentResponse;
import com.example.GoSonGim_BE.domain.kit.service.PronunciationAssessmentService;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Base64;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import javax.sound.sampled.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Map;
import java.util.HashMap;

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
            // 1️⃣ 오디오 스트림을 바이트 배열로 변환
            byte[] audioData = readStreamToByteArray(audioStream);
            log.info("[Azure REST] Audio data size: {} bytes", audioData.length);
            
            // 2️⃣ WAV 헤더에서 오디오 형식 정보 추출
            AudioFormat audioFormat = parseWavHeader(audioData);
            log.info("[Azure REST] Detected audio format: {}Hz, {}bit, {} channels", 
                audioFormat.sampleRate, audioFormat.bitsPerSample, audioFormat.channels);
            
            // 3️⃣ Azure 호환 형식으로 변환 (16kHz)
            byte[] azureCompatibleData = convertToAzureCompatibleFormat(audioData, audioFormat);
            log.info("[Azure REST] Converted audio data size: {} bytes", azureCompatibleData.length);
            
            // 4️⃣ REST API로 발음 평가 요청
            return callAzureSpeechRestApi(azureCompatibleData, targetWord);

        } catch (Exception e) {
            log.error("[Azure REST] 발음 평가 중 오류 발생", e);
            throw new RuntimeException("Azure 발음 평가 실패: " + e.getMessage(), e);
        }
    }

    /**
     * Azure Speech Service REST API를 사용하여 발음 평가 수행
     */
    private PronunciationAssessmentResponse callAzureSpeechRestApi(byte[] audioData, String targetWord) throws Exception {
        // REST API 엔드포인트 구성
        String endpoint = String.format("https://%s.stt.speech.microsoft.com/speech/recognition/conversation/cognitiveservices/v1", 
            speechRegion);
        
        // 발음 평가 설정 JSON 생성
        Map<String, Object> pronunciationConfig = new HashMap<>();
        pronunciationConfig.put("ReferenceText", targetWord);
        pronunciationConfig.put("GradingSystem", "HundredMark");
        pronunciationConfig.put("Granularity", "Phoneme");
        pronunciationConfig.put("Dimension", "Comprehensive");
        pronunciationConfig.put("EnableProsodyAssessment", "True");
        
        ObjectMapper objectMapper = new ObjectMapper();
        String configJson = objectMapper.writeValueAsString(pronunciationConfig);
        String encodedConfig = Base64.getEncoder().encodeToString(configJson.getBytes());
        
        log.info("[Azure REST] 발음 평가 시작 - targetWord: {}", targetWord);
        log.debug("[Azure REST] Config JSON: {}", configJson);
        
        // HTTP 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.set("Ocp-Apim-Subscription-Key", speechKey);
        headers.set("Content-Type", "audio/wav");
        headers.set("Accept", "application/json");
        headers.set("Pronunciation-Assessment", encodedConfig);
        
        // 요청 파라미터 설정
        String url = endpoint + "?language=ko-KR&format=detailed";
        
        // HTTP 요청 생성
        HttpEntity<byte[]> request = new HttpEntity<>(audioData, headers);
        
        // REST API 호출
        RestTemplate restTemplate = new RestTemplate();
        log.info("[Azure REST] Calling Azure Speech API: {}", url);
        
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);
        
        log.info("[Azure REST] Response status: {}", response.getStatusCode());
        log.debug("[Azure REST] Response body: {}", response.getBody());
        
        // 응답 파싱 및 변환
        return parseAzureRestResponse(response.getBody(), targetWord);
    }
    
    /**
     * Azure REST API 응답을 PronunciationAssessmentResponse로 변환
     */
    private PronunciationAssessmentResponse parseAzureRestResponse(String responseBody, String targetWord) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> responseMap = objectMapper.readValue(responseBody, Map.class);
        
        // NBest 결과에서 첫 번째 결과 추출
        @SuppressWarnings("unchecked")
        java.util.List<Map<String, Object>> nBest = (java.util.List<Map<String, Object>>) responseMap.get("NBest");
        
        if (nBest == null || nBest.isEmpty()) {
            throw new RuntimeException("Azure REST API 응답에 NBest 결과가 없습니다");
        }
        
        Map<String, Object> bestResult = nBest.get(0);
        String recognizedText = (String) bestResult.get("Display");
        
        // 발음 평가 점수는 NBest[0]에 직접 포함되어 있음
        Double accuracyScore = getDoubleValue(bestResult, "AccuracyScore");
        Double fluencyScore = getDoubleValue(bestResult, "FluencyScore");
        Double completenessScore = getDoubleValue(bestResult, "CompletenessScore");
        Double prosodyScore = getDoubleValue(bestResult, "PronScore"); // PronScore가 전체 발음 점수
        
        PronunciationAssessmentResponse result = new PronunciationAssessmentResponse(
            recognizedText != null ? recognizedText : "",
            accuracyScore != null ? accuracyScore : 0.0,
            fluencyScore != null ? fluencyScore : 0.0,
            completenessScore != null ? completenessScore : 0.0,
            prosodyScore != null ? prosodyScore : 0.0
        );
        
        log.info("[Azure REST] 발음 평가 완료: {}", result);
        return result;
    }
    
    /**
     * Map에서 Double 값을 안전하게 추출하는 헬퍼 메서드
     */
    private Double getDoubleValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        try {
            return Double.parseDouble(value.toString());
        } catch (NumberFormatException e) {
            log.warn("[Azure REST] Invalid number format for {}: {}", key, value);
            return null;
        }
    }
    
    // WAV 파일 헤더 분석을 위한 AudioFormat 클래스
    private static class AudioFormat {
        public final int sampleRate;
        public final int bitsPerSample;
        public final int channels;
        
        public AudioFormat(int sampleRate, int bitsPerSample, int channels) {
            this.sampleRate = sampleRate;
            this.bitsPerSample = bitsPerSample;
            this.channels = channels;
        }
    }
    
    // InputStream을 바이트 배열로 변환
    private byte[] readStreamToByteArray(InputStream inputStream) throws Exception {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] data = new byte[8192];
        int bytesRead;
        
        while ((bytesRead = inputStream.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, bytesRead);
        }
        
        inputStream.close();
        return buffer.toByteArray();
    }
    
    // WAV 파일 헤더에서 오디오 형식 정보 추출
    private AudioFormat parseWavHeader(byte[] wavData) {
        try {
            // WAV 파일 시그니처 확인
            if (wavData.length < 44 || 
                !(wavData[0] == 'R' && wavData[1] == 'I' && wavData[2] == 'F' && wavData[3] == 'F')) {
                log.warn("[Azure] Invalid WAV header, using default format");
                return new AudioFormat(16000, 16, 1); // 기본값
            }
            
            // 채널 수 (오프셋 22-23)
            int channels = (wavData[23] & 0xFF) << 8 | (wavData[22] & 0xFF);
            
            // 샘플 레이트 (오프셋 24-27)
            int sampleRate = (wavData[27] & 0xFF) << 24 | 
                           (wavData[26] & 0xFF) << 16 | 
                           (wavData[25] & 0xFF) << 8 | 
                           (wavData[24] & 0xFF);
            
            // 비트 샘플 (오프셋 34-35)
            int bitsPerSample = (wavData[35] & 0xFF) << 8 | (wavData[34] & 0xFF);
            
            log.debug("[Azure] WAV Header parsed - SampleRate: {}, BitsPerSample: {}, Channels: {}", 
                sampleRate, bitsPerSample, channels);
            
            return new AudioFormat(sampleRate, bitsPerSample, channels);
            
        } catch (Exception e) {
            log.warn("[Azure] Failed to parse WAV header: {}, using default format", e.getMessage());
            return new AudioFormat(16000, 16, 1); // 기본값
        }
    }
    
    // 오디오를 Azure 호환 형식(16kHz, 16bit, mono)로 변환
    private byte[] convertToAzureCompatibleFormat(byte[] originalWavData, AudioFormat originalFormat) {
        try {
            // Azure가 지원하는 형식인지 확인 (8kHz 또는 16kHz)
            if ((originalFormat.sampleRate == 8000 || originalFormat.sampleRate == 16000) && 
                originalFormat.bitsPerSample == 16 && originalFormat.channels == 1) {
                log.info("[Azure] Audio already in compatible format, no conversion needed");
                return originalWavData;
            }
            
            log.info("[Azure] Converting audio from {}Hz to 8kHz for Azure compatibility", originalFormat.sampleRate);
            
            // WAV 헤더 제거 (데이터 부분만 추출)
            byte[] audioDataOnly = extractAudioData(originalWavData);
            
            // 다운샘플링 수행 (8kHz로 시도)
            byte[] convertedData = downsampleAudio(audioDataOnly, originalFormat, 8000);
            
            // 새로운 WAV 헤더 생성 (8kHz, 16bit, mono)
            byte[] newWavFile = createWavFile(convertedData, 8000, 16, 1);
            
            log.info("[Azure] Audio conversion completed: {} bytes -> {} bytes", originalWavData.length, newWavFile.length);
            return newWavFile;
            
        } catch (Exception e) {
            log.error("[Azure] Audio conversion failed: {}", e.getMessage(), e);
            return originalWavData; // 실패 시 원본 반환
        }
    }
    
    // WAV 파일에서 오디오 데이터만 추출 (헤더 제거)
    private byte[] extractAudioData(byte[] wavData) {
        if (wavData.length < 44) {
            return wavData;
        }
        
        // 데이터 청크 크기 확인 (오프셋 40-43)
        int dataSize = (wavData[43] & 0xFF) << 24 | 
                      (wavData[42] & 0xFF) << 16 | 
                      (wavData[41] & 0xFF) << 8 | 
                      (wavData[40] & 0xFF);
        
        // 헤더 제거하고 오디오 데이터만 반환
        byte[] audioData = new byte[wavData.length - 44];
        System.arraycopy(wavData, 44, audioData, 0, audioData.length);
        
        return audioData;
    }
    
    // 간단한 다운샘플링 (간격 샘플링 방식)
    private byte[] downsampleAudio(byte[] audioData, AudioFormat originalFormat, int targetSampleRate) {
        if (originalFormat.sampleRate <= targetSampleRate) {
            return audioData; // 이미 목표 샘플률 이하
        }
        
        int bytesPerSample = originalFormat.bitsPerSample / 8;
        int originalSamples = audioData.length / bytesPerSample;
        
        // 다운샘플링 비율 계산
        double ratio = (double) originalFormat.sampleRate / targetSampleRate;
        int targetSamples = (int) (originalSamples / ratio);
        
        byte[] downsampledData = new byte[targetSamples * bytesPerSample];
        
        for (int i = 0; i < targetSamples; i++) {
            int originalIndex = (int) (i * ratio) * bytesPerSample;
            if (originalIndex + bytesPerSample <= audioData.length) {
                System.arraycopy(audioData, originalIndex, downsampledData, i * bytesPerSample, bytesPerSample);
            }
        }
        
        return downsampledData;
    }
    
    // 새로운 WAV 파일 생성
    private byte[] createWavFile(byte[] audioData, int sampleRate, int bitsPerSample, int channels) {
        int byteRate = sampleRate * channels * bitsPerSample / 8;
        int blockAlign = channels * bitsPerSample / 8;
        
        ByteBuffer buffer = ByteBuffer.allocate(44 + audioData.length);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        
        // RIFF 헤더
        buffer.put("RIFF".getBytes());
        buffer.putInt(36 + audioData.length);
        buffer.put("WAVE".getBytes());
        
        // fmt 청크
        buffer.put("fmt ".getBytes());
        buffer.putInt(16); // PCM format size
        buffer.putShort((short) 1); // PCM format
        buffer.putShort((short) channels);
        buffer.putInt(sampleRate);
        buffer.putInt(byteRate);
        buffer.putShort((short) blockAlign);
        buffer.putShort((short) bitsPerSample);
        
        // data 청크
        buffer.put("data".getBytes());
        buffer.putInt(audioData.length);
        buffer.put(audioData);
        
        return buffer.array();
    }
    
    // WAV 파일에서 순수 PCM 데이터만 추출 (Azure SDK 전용)
    private byte[] extractPureAudioData(byte[] wavData) {
        if (wavData.length < 44) {
            log.warn("[Azure] WAV data too short, returning as-is");
            return wavData;
        }
        
        try {
            // "data" 청크 찾기
            int dataChunkStart = -1;
            for (int i = 12; i < wavData.length - 8; i++) {
                if (wavData[i] == 'd' && wavData[i+1] == 'a' && 
                    wavData[i+2] == 't' && wavData[i+3] == 'a') {
                    dataChunkStart = i;
                    break;
                }
            }
            
            if (dataChunkStart == -1) {
                log.warn("[Azure] 'data' chunk not found, using offset 44");
                dataChunkStart = 40; // 기본 헤더 크기에서 "data" 4바이트 뺀 값
            }
            
            // 데이터 크기 읽기 (data 청크 시작 + 4바이트 후 4바이트)
            int dataSize = (wavData[dataChunkStart + 7] & 0xFF) << 24 | 
                          (wavData[dataChunkStart + 6] & 0xFF) << 16 | 
                          (wavData[dataChunkStart + 5] & 0xFF) << 8 | 
                          (wavData[dataChunkStart + 4] & 0xFF);
            
            // 실제 오디오 데이터 시작점 (data 청크 + 8바이트)
            int audioDataStart = dataChunkStart + 8;
            
            // 사용 가능한 최대 데이터 크기 계산
            int availableDataSize = wavData.length - audioDataStart;
            int actualDataSize = Math.min(dataSize, availableDataSize);
            
            // 순수 PCM 데이터 추출
            byte[] pureData = new byte[actualDataSize];
            System.arraycopy(wavData, audioDataStart, pureData, 0, actualDataSize);
            
            log.info("[Azure] Extracted pure PCM data: {} bytes from WAV header data size: {}", 
                actualDataSize, dataSize);
            
            return pureData;
            
        } catch (Exception e) {
            log.warn("[Azure] Failed to extract pure audio data: {}, using default offset", e.getMessage());
            // 실패 시 기본 44바이트 헤더 제거
            byte[] fallbackData = new byte[wavData.length - 44];
            System.arraycopy(wavData, 44, fallbackData, 0, fallbackData.length);
            return fallbackData;
        }
    }
}