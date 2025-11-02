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

    /**
     * String 값을 enum으로 변환
     * @param value 소문자 카테고리명 (예: "all", "daily")
     * @return SituationCategory enum
     * @throws IllegalArgumentException 유효하지 않은 값인 경우
     */
    public static SituationCategory from(String value) {
        if (value == null || value.isBlank()) {
            return ALL;
        }
        
        try {
            return valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("유효하지 않은 카테고리입니다: " + value);
        }
    }

    /**
     * 전체 카테고리인지 확인
     */
    public boolean isAll() {
        return this == ALL;
    }
}
