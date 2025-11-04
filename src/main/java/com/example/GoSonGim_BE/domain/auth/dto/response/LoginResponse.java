package com.example.GoSonGim_BE.domain.auth.dto.response;

public record LoginResponse(
    TokenResponse tokens,
    UserResponse user
) {}
