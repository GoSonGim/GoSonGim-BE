package com.example.GoSonGim_BE.domain.auth.dto.response;

public record TokenResponse(
    String accessToken,
    String refreshToken,
    String tokenType,
    int expiresIn,
    int refreshExpiresIn
) {}
