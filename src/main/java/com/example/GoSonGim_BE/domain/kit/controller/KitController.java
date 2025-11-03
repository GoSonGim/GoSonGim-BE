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
import com.example.GoSonGim_BE.global.dto.ApiResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;



@Tag(name = "Kit API")
@RestController
@RequestMapping(ApiVersion.CURRENT + "/kits")
@RequiredArgsConstructor
public class KitController {
    
    private final KitService kitService;

    /**
     * 조음 키트 카테고리 목록 조회
     */
    @Operation(summary = "조음 키트 카테고리 목록 조회")
    @GetMapping("/category")
    public ResponseEntity<ApiResult<KitCategoriesResponse>> getKitCategories() {
        KitCategoriesResponse response = kitService.getKitCategories();
        ApiResult<KitCategoriesResponse> apiResult = ApiResult.success(200, "조음 키트 카테고리 목록 조회 성공", response);
        return ResponseEntity.ok(apiResult);
    }
    
    /**
     * 카테고리별 조음 키트 목록 조회
     */
    @Operation(summary = "카테고리별 조음 키트 목록 조회")
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<ApiResult<KitsResponse>> getKitsByCategoryId(@PathVariable Long categoryId) {
        KitsResponse response = kitService.getKitsByCategoryId(categoryId);
        ApiResult<KitsResponse> apiResult = ApiResult.success(200, "조음 키트 목록 조회 성공", response);
        return ResponseEntity.ok(apiResult);
    }

    /**
     * 조음 키트 스테이지 조회
     */
    @Operation(summary = "조음 키트 스테이지 조회")
    @GetMapping("/{kitId}/stages")
    public ResponseEntity<ApiResult<KitStagesResponse>> getKitStages(@PathVariable Long kitId) {
        KitStagesResponse response = kitService.getKitStages(kitId);
        ApiResult<KitStagesResponse> apiResult = ApiResult.success(200, "조음 키트 상세 조회 성공", response);
        return ResponseEntity.ok(apiResult);
    }
    
    /**
     * 조음 키트 단어 발음 평가
     * Azure Speech Service를 통해 발음을 평가하고 결과를 저장
     */
    @Operation(summary = "조음 키트 단어 발음 평가")
    @PostMapping("/stages/evaluate")
    public ResponseEntity<ApiResult<EvaluateResponse>> evaluatePronunciation(@Valid @RequestBody List<EvaluateRequest> evaluations) {
        Long userId = 1L;
        EvaluateResponse response = kitService.evaluatePronunciation(evaluations, userId);
        ApiResult<EvaluateResponse> apiResult = ApiResult.success(200, "발음 평가가 완료되었습니다.", response);
        return ResponseEntity.ok(apiResult);
    }
    
    /**
     * 조음 키트 단어 외 학습 기록 저장
     * 발음 평가 없이 직접 학습 기록을 저장
     */
    @Operation(summary = "조음 키트 학습 기록 저장")
    @PostMapping("/stages/log")
    public ResponseEntity<ApiResult<Void>> saveStudyLog(@Valid @RequestBody LogRequest request) {
        Long userId = 1L;
        kitService.saveStudyLog(request, userId);
        ApiResult<Void> apiResult = ApiResult.success(200, "학습 기록이 저장되었습니다.", null);
        return ResponseEntity.ok(apiResult);
    }
    
    /**
     * 조음 키트 진단 평가
     * WAV 파일과 목표 텍스트를 받아 발음을 평가하고 키트를 추천
     */
    @Operation(summary = "조음 키트 진단 평가")
    @PostMapping(value = "/diagnosis", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResult<DiagnosticResponse>> diagnosePronunciation(
            @RequestPart("audioFile") MultipartFile audioFile,
            @RequestParam("targetText") String targetText) {
        DiagnosticResponse response = kitService.diagnosePronunciation(audioFile, targetText);
        ApiResult<DiagnosticResponse> apiResult = ApiResult.success(200, "키트 진단이 성공적으로 처리되었습니다.", response);
        return ResponseEntity.ok(apiResult);
    }
}