package com.example.GoSonGim_BE.domain.review.dto.response;

import java.util.List;

/**
 * 월별 학습 기록 조회 응답
 */
public record ReviewMonthlyResponse(
    String month,
    List<Integer> days
) {}

