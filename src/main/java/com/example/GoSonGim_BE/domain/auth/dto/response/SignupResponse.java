package com.example.GoSonGim_BE.domain.auth.dto.response;

public record SignupResponse(
    TokenResponse tokens,
    UserResponse user
) {}
