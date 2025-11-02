package com.example.GoSonGim_BE.domain.situation.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.GoSonGim_BE.domain.situation.dto.request.SituationCreateRequest;
import com.example.GoSonGim_BE.domain.situation.dto.response.SituationCreateResponse;
import com.example.GoSonGim_BE.domain.situation.dto.response.SituationListResponse;
import com.example.GoSonGim_BE.domain.situation.service.SituationService;
import com.example.GoSonGim_BE.global.constant.ApiVersion;
import com.example.GoSonGim_BE.global.dto.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(ApiVersion.CURRENT + "/situations")
@RequiredArgsConstructor
public class SituationController {

    private final SituationService situationService;

    /**
     * 카테고리별 상황극 목록 조회
     */
    @GetMapping
    public ResponseEntity<ApiResponse<SituationListResponse>> getSituationsByCategory(
            @RequestParam(value = "category", defaultValue = "all") String category) {
        SituationListResponse response = situationService.getSituationsByCategory(category);
        
        ApiResponse<SituationListResponse> apiResponse = ApiResponse.success(
            200, "상황극 목록을 조회했습니다.", response);
        
        return ResponseEntity.ok(apiResponse);
    }

    /**
     * 상황극 생성
     */
    @PostMapping
    public ResponseEntity<ApiResponse<SituationCreateResponse>> createSituation(@Valid @RequestBody SituationCreateRequest request) {
        SituationCreateResponse response = situationService.createSituation(request);

        ApiResponse<SituationCreateResponse> apiResponse = ApiResponse.success(
            201, "상황극을 생성했습니다.", response);

        return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
    }
}
