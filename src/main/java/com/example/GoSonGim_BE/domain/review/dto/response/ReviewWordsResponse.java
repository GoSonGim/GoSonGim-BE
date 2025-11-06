package com.example.GoSonGim_BE.domain.review.dto.response;

import java.util.List;

/**
 * 복습 단어 조회 응답 DTO
 */
public record ReviewWordsResponse(
    List<String> words,
    int count
) {
    public ReviewWordsResponse(List<String> words) {
        this(words, words.size());
    }
}

