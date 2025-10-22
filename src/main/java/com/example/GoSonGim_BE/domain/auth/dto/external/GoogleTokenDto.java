package com.example.GoSonGim_BE.domain.auth.dto.external;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GoogleTokenDto(
    @JsonProperty("access_token")
    String accessToken,
    
    @JsonProperty("expires_in")
    Long expiresIn,
    
    String scope,
    
    @JsonProperty("token_type")
    String tokenType,
    
    @JsonProperty("id_token")
    String idToken
) {}
