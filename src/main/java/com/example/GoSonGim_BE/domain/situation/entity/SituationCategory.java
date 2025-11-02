package com.example.GoSonGim_BE.domain.situation.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 상황극 카테고리
 */
@Getter
@RequiredArgsConstructor
public enum SituationCategory {

    ALL("전체"),
    DAILY("일상"),
    PURCHASE("구매"),
    MEDICAL("의료"),
    TRAFFIC("교통"),
    JOB("직업"),
    SOCIAL("사교"),
    EMERGENCY("비상");

    private final String description;
}
