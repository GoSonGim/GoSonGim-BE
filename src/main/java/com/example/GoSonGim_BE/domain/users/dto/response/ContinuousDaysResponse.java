package com.example.GoSonGim_BE.domain.users.dto.response;

/**
 * 연속 학습일 조회 응답
 */
public record ContinuousDaysResponse(
    int streakDays,         // 연속 학습일
    boolean learnedToday    // 오늘 학습 여부
) {
    public static ContinuousDaysResponse of(int streakDays, boolean learnedToday) {
        return new ContinuousDaysResponse(streakDays, learnedToday);
    }
}
