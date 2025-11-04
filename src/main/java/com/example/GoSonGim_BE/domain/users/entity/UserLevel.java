package com.example.GoSonGim_BE.domain.users.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

// 사용자 학습 레벨
@Getter
@RequiredArgsConstructor
public enum UserLevel {

    BEGINNER_1("초급 1단계"),
    BEGINNER_2("초급 2단계"),
    BEGINNER_3("초급 3단계"),

    INTERMEDIATE_1("중급 1단계"),
    INTERMEDIATE_2("중급 2단계"),
    INTERMEDIATE_3("중급 3단계"),

    ADVANCED_1("고급 1단계"),
    ADVANCED_2("고급 2단계"),
    ADVANCED_3("고급 3단계");

    private final String description;
}

