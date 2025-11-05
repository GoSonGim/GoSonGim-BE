package com.example.GoSonGim_BE.domain.situation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.GoSonGim_BE.domain.situation.dto.request.SituationCreateRequest;
import com.example.GoSonGim_BE.domain.situation.dto.request.SituationSessionEndRequest;
import com.example.GoSonGim_BE.domain.situation.dto.request.SituationSessionReplyRequest;
import com.example.GoSonGim_BE.domain.situation.dto.request.SituationSessionStartRequest;
import com.example.GoSonGim_BE.domain.situation.dto.response.SituationCreateResponse;
import com.example.GoSonGim_BE.domain.situation.dto.response.SituationDetailResponse;
import com.example.GoSonGim_BE.domain.situation.dto.response.SituationListResponse;
import com.example.GoSonGim_BE.domain.situation.dto.response.SituationSessionEndResponse;
import com.example.GoSonGim_BE.domain.situation.dto.response.SituationSessionReplyResponse;
import com.example.GoSonGim_BE.domain.situation.dto.response.SituationSessionStartResponse;
import com.example.GoSonGim_BE.domain.situation.dto.response.SituationSpeechToTextResponse;
import com.example.GoSonGim_BE.domain.situation.service.SituationService;
import com.example.GoSonGim_BE.global.constant.ApiVersion;
import com.example.GoSonGim_BE.global.dto.ApiResult;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
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
    public ResponseEntity<ApiResult<SituationCreateResponse>> createSituation(
            @Valid @RequestBody SituationCreateRequest request) {
        SituationCreateResponse response = situationService.createSituation(request);
        ApiResult<SituationCreateResponse> apiResult = ApiResult.success(201, "상황극을 생성했습니다.", response);
        return ResponseEntity.status(HttpStatus.CREATED).body(apiResult);
    }
    
    @Operation(summary = "대화 시작")
    @PostMapping("/session/start")
    public ResponseEntity<ApiResult<SituationSessionStartResponse>> startSession(
            Authentication authentication,
            @Valid @RequestBody SituationSessionStartRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        SituationSessionStartResponse response = situationService.startSession(userId, request);
        ApiResult<SituationSessionStartResponse> apiResult = ApiResult.success(200, "상황극 학습 세션이 시작되었습니다.", response);
        return ResponseEntity.ok(apiResult);
    }
    
    @Operation(summary = "답변 평가와 다음 질문 생성")
    @PostMapping("/session/reply")
    public ResponseEntity<ApiResult<SituationSessionReplyResponse>> reply(
            Authentication authentication,
            @Valid @RequestBody SituationSessionReplyRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        SituationSessionReplyResponse response = situationService.reply(userId, request);
        ApiResult<SituationSessionReplyResponse> apiResult = ApiResult.success(200, "요청이 성공적으로 처리되었습니다.", response);
        return ResponseEntity.ok(apiResult);
    }
    
    @Operation(summary = "대화 종료")
    @PostMapping("/session/complete")
    public ResponseEntity<ApiResult<SituationSessionEndResponse>> endSession(
            Authentication authentication,
            @Valid @RequestBody SituationSessionEndRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        SituationSessionEndResponse response = situationService.endSession(userId, request);
        ApiResult<SituationSessionEndResponse> apiResult = ApiResult.success(200, "상황극 학습 세션이 종료되었습니다.", response);
        return ResponseEntity.ok(apiResult);
    }
    
    @Operation(summary = "사용자 답변 STT 변환")
    @PostMapping(value = "/stt", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResult<SituationSpeechToTextResponse>> speechToText(
            Authentication authentication,
            @RequestPart("audioFile") MultipartFile file) {
        SituationSpeechToTextResponse response = situationService.transcribeAudio(file);
        ApiResult<SituationSpeechToTextResponse> apiResult = ApiResult.success(200, "음성 인식 성공", response);
        return ResponseEntity.ok(apiResult);
    }
}
