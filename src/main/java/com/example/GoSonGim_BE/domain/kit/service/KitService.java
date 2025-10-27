package com.example.GoSonGim_BE.domain.kit.service;

import com.example.GoSonGim_BE.domain.kit.dto.*;

public interface KitService {
    /**
     * 조음 키트 조회
     */
    KitStagesResponse getKitStages(Long userId, Long kitId);
    
    /**
     * 조음 키트 단어 발음 평가 및 학습 기록 저장
     */
    EvaluateResponse evaluatePronunciation(EvaluateRequest request);
    
    /**
     * 조음 키트 단어 외 학습 기록 저장
     */
    StageLogResponse saveStageLog(StageLogRequest request);
    
    /**
     * 조음 키트 진단 평가
     */
    DiagnosisResponse performDiagnosis(DiagnosisRequest request);
}