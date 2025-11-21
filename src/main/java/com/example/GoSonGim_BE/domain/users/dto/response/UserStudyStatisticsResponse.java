package com.example.GoSonGim_BE.domain.users.dto.response;

import java.util.List;

public record UserStudyStatisticsResponse(
    Stats stats,
    Graph graph
) {
    public record Stats(
        Long wordCount,      // 학습한 단어 수 (중복 제거)
        Long situationCount, // 학습한 상황 수
        Long kitCount       // 학습한 키트 수 (모든 단계 완료)
    ) {}
    
    public record Graph(
        KitGraph kit,
        SituationGraph situation
    ) {}
    
    public record KitGraph(
        Long totalSuccessCount,     // 총 발화 성공 횟수
        List<Integer> recentDayCounts  // 최근 5일간 일별 학습 개수
    ) {}
    
    public record SituationGraph(
        Long totalSuccessCount,     // 총 발화 성공 횟수
        List<Integer> recentDayCounts  // 최근 5일간 일별 학습 개수
    ) {}
    
    public static UserStudyStatisticsResponse of(
            Long wordCount,
            Long situationCount,
            Long kitCount,
            Long kitTotalSuccessCount,
            List<Integer> kitRecentDayCounts,
            Long situationTotalSuccessCount,
            List<Integer> situationRecentDayCounts) {
        return new UserStudyStatisticsResponse(
            new Stats(wordCount, situationCount, kitCount),
            new Graph(
                new KitGraph(kitTotalSuccessCount, kitRecentDayCounts),
                new SituationGraph(situationTotalSuccessCount, situationRecentDayCounts)
            )
        );
    }
}