package com.example.GoSonGim_BE.domain.situation.service;

import com.example.GoSonGim_BE.domain.situation.dto.request.SituationCreateRequest;
import com.example.GoSonGim_BE.domain.situation.dto.response.SituationCreateResponse;

public interface SituationService {
    /*
     * 상황극 생성
     */
    SituationCreateResponse createSituation(SituationCreateRequest request);
}
