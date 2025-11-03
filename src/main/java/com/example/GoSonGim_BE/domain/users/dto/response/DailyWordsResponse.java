package com.example.GoSonGim_BE.domain.users.dto.response;

import java.util.List;

public record DailyWordsResponse(
    List<DailyWordItem> items,
    PageInfo pageInfo
) {
    public record DailyWordItem(
        String date,        // "YYYY.M.D" 형식
        Integer wordCount,  // 해당 날짜에 학습한 단어 총 개수
        List<String> words  // 학습한 단어 목록
    ) {}
    
    public record PageInfo(
        Integer page,
        Integer size,
        Boolean hasNext
    ) {}
    
    public static DailyWordsResponse of(List<DailyWordItem> items, int page, int size, boolean hasNext) {
        return new DailyWordsResponse(
            items,
            new PageInfo(page, size, hasNext)
        );
    }
}