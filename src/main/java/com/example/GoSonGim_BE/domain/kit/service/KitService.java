package com.example.GoSonGim_BE.domain.kit.service;

import com.example.GoSonGim_BE.domain.kit.dto.request.EvaluateRequest;
import com.example.GoSonGim_BE.domain.kit.dto.response.EvaluateResponse;
import com.example.GoSonGim_BE.domain.kit.dto.response.KitCategoriesResponse;
import com.example.GoSonGim_BE.domain.kit.dto.response.KitStagesResponse;
import com.example.GoSonGim_BE.domain.kit.dto.response.KitsResponse;


public interface KitService {

    // 조음 키트 카테고리 목록 조회
    KitCategoriesResponse getKitCategories();
    
    // 카테고리별 조음 키트 목록 조회
    KitsResponse getKitsByCategoryId(Long categoryId);

    // 조음 키트 스테이지 조회
    KitStagesResponse getKitStages(Long kitId);
    
    // 조음 키트 단어 발음 평가
    EvaluateResponse evaluatePronunciation(EvaluateRequest request, Long userId);
}