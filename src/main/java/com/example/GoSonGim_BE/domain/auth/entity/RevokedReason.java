package com.example.GoSonGim_BE.domain.auth.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RevokedReason {
    ROTATED("Refresh Token Rotation으로 폐기"),
    LOGOUT("사용자 로그아웃"),
    EXPIRED("만료"),
    SECURITY("보안 이슈");

    private final String description;
}
