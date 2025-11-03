package com.example.GoSonGim_BE.domain.situation.service;

import com.example.GoSonGim_BE.domain.situation.dto.request.SituationCreateRequest;
import com.example.GoSonGim_BE.domain.situation.dto.request.SituationSessionReplyRequest;
import com.example.GoSonGim_BE.domain.situation.dto.request.SituationSessionStartRequest;
import com.example.GoSonGim_BE.domain.situation.dto.response.SituationCreateResponse;
import com.example.GoSonGim_BE.domain.situation.dto.response.SituationDetailResponse;
import com.example.GoSonGim_BE.domain.situation.dto.response.SituationListResponse;
import com.example.GoSonGim_BE.domain.situation.dto.response.SituationSessionReplyResponse;
import com.example.GoSonGim_BE.domain.situation.dto.response.SituationSessionStartResponse;

public interface SituationService {
    /**
     * 상황극 생성
     */
    SituationCreateResponse createSituation(SituationCreateRequest request);
    
    /**
     * 카테고리별 상황극 목록 조회
     * @param category 카테고리 (all, daily, purchase, medical, traffic, job, social, emergency)
     * @return 상황극 목록
     */
    SituationListResponse getSituationsByCategory(String category);
    
    /**
     * 상황극 상세 조회
     * @param situationId 상황극 ID
     * @return 상황극 상세 정보
     */
    SituationDetailResponse getSituationById(Long situationId);
    
    /**
     * 상황극 학습 세션 시작
     * @param userId 사용자 ID
     * @param request 세션 시작 요청
     * @return 세션 ID, 첫 질문
     */
    SituationSessionStartResponse startSession(Long userId, SituationSessionStartRequest request);
    
    /**
     * 상황극 학습 세션 답변 평가 및 다음 질문 생성
     * @param userId 사용자 ID
     * @param request 답변 평가 요청 (sessionId, question, answer)
     * @return 평가 결과, 다음 질문, 턴 인덱스, 종료 여부, 최종 요약
     */
    SituationSessionReplyResponse reply(Long userId, SituationSessionReplyRequest request);
}
