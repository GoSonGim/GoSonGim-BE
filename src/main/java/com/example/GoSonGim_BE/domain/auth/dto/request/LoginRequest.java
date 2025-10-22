package com.example.GoSonGim_BE.domain.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
    @Email(message = "이메일 형식이 올바르지 않습니다.")
    @NotBlank(message = "이메일을 입력해주세요.")
    String email,
    
    @NotBlank(message = "비밀번호를 입력해주세요.")
    String password
) {}
