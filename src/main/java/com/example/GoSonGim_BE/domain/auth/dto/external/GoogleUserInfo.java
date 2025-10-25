package com.example.GoSonGim_BE.domain.auth.dto.external;

public record GoogleUserInfo(
    String providerId,
    String providerEmail,
    String name
) {}
