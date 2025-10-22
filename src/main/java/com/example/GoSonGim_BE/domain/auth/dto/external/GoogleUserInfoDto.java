package com.example.GoSonGim_BE.domain.auth.dto.external;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GoogleUserInfoDto(
    String id,
    String email,
    String name,
    String picture,
    
    @JsonProperty("verified_email")
    Boolean verifiedEmail
) {}
