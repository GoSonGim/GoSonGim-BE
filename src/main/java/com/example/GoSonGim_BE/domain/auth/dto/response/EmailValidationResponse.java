package com.example.GoSonGim_BE.domain.auth.dto.response;

public record EmailValidationResponse(
    String email,
    boolean available
) {}
