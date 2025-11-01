package com.example.GoSonGim_BE.domain.kit.controller;

import com.example.GoSonGim_BE.domain.kit.dto.request.EvaluateRequest;
import com.example.GoSonGim_BE.domain.kit.dto.request.LogRequest;
import com.example.GoSonGim_BE.domain.kit.dto.response.DiagnosticResponse;
import com.example.GoSonGim_BE.domain.kit.dto.response.EvaluateResponse;
import com.example.GoSonGim_BE.domain.kit.dto.response.KitCategoriesResponse;
import com.example.GoSonGim_BE.domain.kit.dto.response.KitStagesResponse;
import com.example.GoSonGim_BE.domain.kit.dto.response.KitsResponse;
import com.example.GoSonGim_BE.domain.kit.service.KitService;
import com.example.GoSonGim_BE.global.constant.ApiVersion;
import com.example.GoSonGim_BE.global.dto.ApiResponse;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;



@RestController
@RequestMapping(ApiVersion.CURRENT + "/kits")
@RequiredArgsConstructor
public class KitController {
    
    private final KitService kitService;

    /**
     * 조음 키트 카테고리 목록 조회
     */
    @GetMapping("/category")
    public ResponseEntity<ApiResponse<KitCategoriesResponse>> getKitCategories() {
        KitCategoriesResponse response = kitService.getKitCategories();
        ApiResponse<KitCategoriesResponse> apiResponse = ApiResponse.success(200, "조음 키트 카테고리 목록 조회 성공", response);
        return ResponseEntity.ok(apiResponse);
    }
    
    /**
     * 카테고리별 조음 키트 목록 조회
     */
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<ApiResponse<KitsResponse>> getKitsByCategoryId(@PathVariable Long categoryId) {
        KitsResponse response = kitService.getKitsByCategoryId(categoryId);
        ApiResponse<KitsResponse> apiResponse = ApiResponse.success(200, "조음 키트 목록 조회 성공", response);
        return ResponseEntity.ok(apiResponse);
    }

    /**
     * 조음 키트 스테이지 조회
     */
    @GetMapping("/{kitId}/stages")
    public ResponseEntity<ApiResponse<KitStagesResponse>> getKitStages(@PathVariable Long kitId) {
        KitStagesResponse response = kitService.getKitStages(kitId);
        ApiResponse<KitStagesResponse> apiResponse = ApiResponse.success(200, "조음 키트 상세 조회 성공", response);
        return ResponseEntity.ok(apiResponse);
    }
    
    /**
     * 조음 키트 단어 발음 평가
     * Azure Speech Service를 통해 발음을 평가하고 결과를 저장
     */
    @PostMapping("/stages/evaluate")
    public ResponseEntity<ApiResponse<EvaluateResponse>> evaluatePronunciation(@Valid @RequestBody List<EvaluateRequest> evaluations) {
        Long userId = 1L;
        EvaluateResponse response = kitService.evaluatePronunciation(evaluations, userId);
        ApiResponse<EvaluateResponse> apiResponse = ApiResponse.success(200, "발음 평가가 완료되었습니다.", response);
        return ResponseEntity.ok(apiResponse);
    }
    
    /**
     * 조음 키트 단어 외 학습 기록 저장
     * 발음 평가 없이 직접 학습 기록을 저장
     */
    @PostMapping("/stages/log")
    public ResponseEntity<ApiResponse<Void>> saveStudyLog(@Valid @RequestBody LogRequest request) {
        Long userId = 1L;
        kitService.saveStudyLog(request, userId);
        ApiResponse<Void> apiResponse = ApiResponse.success(200, "학습 기록이 저장되었습니다.", null);
        return ResponseEntity.ok(apiResponse);
    }
    
    /**
     * 조음 키트 진단 평가
     * WAV 파일과 목표 텍스트를 받아 발음을 평가하고 키트를 추천
     */
    @PostMapping(value = "/diagnosis", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<DiagnosticResponse>> diagnosePronunciation(
            @RequestPart("audioFile") MultipartFile audioFile,
            @RequestParam("targetText") String targetText) {
        DiagnosticResponse response = kitService.diagnosePronunciation(audioFile, targetText);
        ApiResponse<DiagnosticResponse> apiResponse = ApiResponse.success(200, "키트 진단이 성공적으로 처리되었습니다.", response);
        return ResponseEntity.ok(apiResponse);
    }
}