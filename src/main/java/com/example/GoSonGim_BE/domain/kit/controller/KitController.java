package com.example.GoSonGim_BE.domain.kit.controller;

import com.example.GoSonGim_BE.domain.kit.dto.*;
import com.example.GoSonGim_BE.domain.kit.service.KitService;
import com.example.GoSonGim_BE.global.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class KitController {
    
    private final KitService kitService;
    
    /**
     * 조음 키트 조회
     */
    @GetMapping("/kits/{kitId}/stages")
    public ResponseEntity<ApiResponse<KitStagesResponse>> getKitStages(@RequestParam Long userId, @PathVariable Long kitId) {
        KitStagesResponse response = kitService.getKitStages(userId, kitId);
        ApiResponse<KitStagesResponse> apiResponse = ApiResponse.success(200, "조음 키트 조회 성공", response);
        return ResponseEntity.ok(apiResponse);
    }
    
    /**
     * 조음 키트 단어 발음 평가 및 학습 기록 저장
     */
    @PostMapping("/kits/stages/evaluate")
    public ResponseEntity<ApiResponse<EvaluateResponse>> evaluatePronunciation(@RequestParam Long userId, @Valid @RequestBody EvaluateRequest request) {
        EvaluateResponse response = kitService.evaluatePronunciation(request);
        ApiResponse<EvaluateResponse> apiResponse = ApiResponse.success(201, "발음 평가 및 학습 기록 저장 완료", response);
        return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
    }
    
    /**
     * 조음 키트 단어 외 학습 기록 저장
     */
    @PostMapping("/kits/stages/log")
    public ResponseEntity<ApiResponse<StageLogResponse>> saveStageLog(@RequestParam Long userId, @Valid @RequestBody StageLogRequest request) {
        StageLogResponse response = kitService.saveStageLog(request);
        ApiResponse<StageLogResponse> apiResponse = ApiResponse.success(201, "학습 기록 저장 완료", response);
        return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
    }
    
    /**
     * 조음 키트 진단 평가
     */
    @PostMapping("/kit/diagnosis")
    public ResponseEntity<ApiResponse<DiagnosisResponse>> performDiagnosis(@RequestParam Long userId, @Valid @RequestBody DiagnosisRequest request) {
        DiagnosisResponse response = kitService.performDiagnosis(request);
        ApiResponse<DiagnosisResponse> apiResponse = ApiResponse.success(201, "진단 평가 완료", response);
        return ResponseEntity.ok(apiResponse);
    }
}