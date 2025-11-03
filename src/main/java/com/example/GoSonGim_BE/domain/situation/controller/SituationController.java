package com.example.GoSonGim_BE.domain.situation.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.GoSonGim_BE.domain.situation.dto.request.SituationCreateRequest;
import com.example.GoSonGim_BE.domain.situation.dto.request.SituationSessionReplyRequest;
import com.example.GoSonGim_BE.domain.situation.dto.request.SituationSessionStartRequest;
import com.example.GoSonGim_BE.domain.situation.dto.response.SituationCreateResponse;
import com.example.GoSonGim_BE.domain.situation.dto.response.SituationDetailResponse;
import com.example.GoSonGim_BE.domain.situation.dto.response.SituationListResponse;
import com.example.GoSonGim_BE.domain.situation.dto.response.SituationSessionReplyResponse;
import com.example.GoSonGim_BE.domain.situation.dto.response.SituationSessionStartResponse;
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

    @GetMapping
    public ResponseEntity<ApiResponse<SituationListResponse>> getSituationsByCategory(
            @RequestParam(value = "category", defaultValue = "all") String category) {
        SituationListResponse response = situationService.getSituationsByCategory(category);
        return ResponseEntity.ok(ApiResponse.success(200, "상황극 목록을 조회했습니다.", response));
    }

    @GetMapping("/{situationId}")
    public ResponseEntity<ApiResponse<SituationDetailResponse>> getSituationById(
            @PathVariable Long situationId) {
        SituationDetailResponse response = situationService.getSituationById(situationId);
        return ResponseEntity.ok(ApiResponse.success(200, "상황극을 조회했습니다.", response));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<SituationCreateResponse>> createSituation(
            @Valid @RequestBody SituationCreateRequest request) {
        SituationCreateResponse response = situationService.createSituation(request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(201, "상황극을 생성했습니다.", response));
    }
    
    @PostMapping("/session/start")
    public ResponseEntity<ApiResponse<SituationSessionStartResponse>> startSession(
            Authentication authentication,
            @Valid @RequestBody SituationSessionStartRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        SituationSessionStartResponse response = situationService.startSession(userId, request);
        return ResponseEntity.ok(ApiResponse.success(200, "상황극 학습 세션이 시작되었습니다.", response));
    }
    
    @PostMapping("/session/reply")
    public ResponseEntity<ApiResponse<SituationSessionReplyResponse>> reply(
            Authentication authentication,
            @Valid @RequestBody SituationSessionReplyRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        SituationSessionReplyResponse response = situationService.reply(userId, request);
        return ResponseEntity.ok(ApiResponse.success(200, "요청이 성공적으로 처리되었습니다.", response));
    }
}
