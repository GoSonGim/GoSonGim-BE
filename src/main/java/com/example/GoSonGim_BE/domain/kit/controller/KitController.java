package com.example.GoSonGim_BE.domain.kit.controller;

import com.example.GoSonGim_BE.domain.kit.dto.request.EvaluateRequest;
import com.example.GoSonGim_BE.domain.kit.dto.response.EvaluateResponse;
import com.example.GoSonGim_BE.domain.kit.dto.response.KitCategoriesResponse;
import com.example.GoSonGim_BE.domain.kit.dto.response.KitStagesResponse;
import com.example.GoSonGim_BE.domain.kit.dto.response.KitsResponse;
import com.example.GoSonGim_BE.domain.kit.service.KitService;
import com.example.GoSonGim_BE.global.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;



@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class KitController {
    
    private final KitService kitService;

    /**
     * 조음 키트 카테고리 목록 조회
     */
    @GetMapping("/kits/category")
    public ResponseEntity<ApiResponse<KitCategoriesResponse>> getKitCategories() {
        KitCategoriesResponse response = kitService.getKitCategories();
        ApiResponse<KitCategoriesResponse> apiResponse = ApiResponse.success(200, "조음 키트 카테고리 목록 조회 성공", response);
        return ResponseEntity.ok(apiResponse);
    }
    
    /**
     * 카테고리별 조음 키트 목록 조회
     */
    @GetMapping("/kits/category/{categoryId}")
    public ResponseEntity<ApiResponse<KitsResponse>> getKitsByCategoryId(@PathVariable Long categoryId) {
        KitsResponse response = kitService.getKitsByCategoryId(categoryId);
        ApiResponse<KitsResponse> apiResponse = ApiResponse.success(200, "조음 키트 목록 조회 성공", response);
        return ResponseEntity.ok(apiResponse);
    }

    /**
     * 조음 키트 스테이지 조회
     */
    @GetMapping("/kits/{kitId}/stages")
    public ResponseEntity<ApiResponse<KitStagesResponse>> getKitStages(@PathVariable Long kitId) {
        KitStagesResponse response = kitService.getKitStages(kitId);
        ApiResponse<KitStagesResponse> apiResponse = ApiResponse.success(200, "조음 키트 상세 조회 성공", response);
        return ResponseEntity.ok(apiResponse);
    }
    
    /**
     * 조음 키트 단어 발음 평가
     * Azure Speech Service를 통해 발음을 평가하고 결과를 저장
     */
    @PostMapping("/kits/stages/evaluate")
    public ResponseEntity<ApiResponse<EvaluateResponse>> evaluatePronunciation(@Valid @RequestBody EvaluateRequest request) {
        Long userId = 1L;
        EvaluateResponse response = kitService.evaluatePronunciation(request, userId);
        ApiResponse<EvaluateResponse> apiResponse = ApiResponse.success(200, "발음 평가가 완료되었습니다.", response);
        return ResponseEntity.ok(apiResponse);
    }
}