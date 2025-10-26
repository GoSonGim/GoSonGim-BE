package com.example.GoSonGim_BE.domain.auth.dto.request;

import jakarta.validation.constraints.NotBlank;

public record GoogleLoginRequest(    
    @NotBlank(message = "구글 인증 코드는 필수입니다.")
    String code,

    @NotBlank(message = "리디렉션 URI는 필수입니다.")
    String redirectUri
) {}
