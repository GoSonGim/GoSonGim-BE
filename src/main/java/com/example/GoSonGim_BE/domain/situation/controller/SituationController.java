package com.example.GoSonGim_BE.domain.situation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.GoSonGim_BE.domain.situation.dto.request.SituationCreateRequest;
import com.example.GoSonGim_BE.domain.situation.dto.response.SituationCreateResponse;
import com.example.GoSonGim_BE.domain.situation.dto.response.SituationDetailResponse;
import com.example.GoSonGim_BE.domain.situation.dto.response.SituationListResponse;
import com.example.GoSonGim_BE.domain.situation.service.SituationService;
import com.example.GoSonGim_BE.global.constant.ApiVersion;
import com.example.GoSonGim_BE.global.dto.ApiResult;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Situation API")
@RestController
@RequestMapping(ApiVersion.CURRENT + "/situations")
@RequiredArgsConstructor
public class SituationController {

    private final SituationService situationService;

    /**
     * 카테고리별 상황극 목록 조회
     */
    @Operation(summary = "상황극 목록 조회")
    @GetMapping
    public ResponseEntity<ApiResult<SituationListResponse>> getSituationsByCategory(
            @RequestParam(value = "category", defaultValue = "all") String category) {
        SituationListResponse response = situationService.getSituationsByCategory(category);
        
        ApiResult<SituationListResponse> apiResult = ApiResult.success(
            200, "상황극 목록을 조회했습니다.", response);
        
        return ResponseEntity.ok(apiResult);
    }

    /**
     * 상황극 상세 조회
     */
    @Operation(summary = "상황극 상세 조회")
    @GetMapping("/{situationId}")
    public ResponseEntity<ApiResult<SituationDetailResponse>> getSituationById(
            @PathVariable Long situationId) {
        SituationDetailResponse response = situationService.getSituationById(situationId);
        
        ApiResult<SituationDetailResponse> apiResult = ApiResult.success(
            200, "상황극을 조회했습니다.", response);
        
        return ResponseEntity.ok(apiResult);
    }

    /**
     * 상황극 생성
     */
    @Operation(summary = "상황극 생성")
    @PostMapping
    public ResponseEntity<ApiResult<SituationCreateResponse>> createSituation(@Valid @RequestBody SituationCreateRequest request) {
        SituationCreateResponse response = situationService.createSituation(request);

        ApiResult<SituationCreateResponse> apiResult = ApiResult.success(
            201, "상황극을 생성했습니다.", response);

        return ResponseEntity.status(HttpStatus.CREATED).body(apiResult);
    }
}
