package com.example.GoSonGim_BE.domain.auth.service;

import com.example.GoSonGim_BE.domain.auth.dto.request.EmailValidationRequest;
import com.example.GoSonGim_BE.domain.auth.dto.request.GoogleLoginRequest;
import com.example.GoSonGim_BE.domain.auth.dto.request.LoginRequest;
import com.example.GoSonGim_BE.domain.auth.dto.request.SignupRequest;
import com.example.GoSonGim_BE.domain.auth.dto.response.EmailValidationResponse;
import com.example.GoSonGim_BE.domain.auth.dto.response.LoginResponse;
import com.example.GoSonGim_BE.domain.auth.dto.response.SignupResponse;

public interface AuthService {
    /**
     * 이메일 로그인
     */
    SignupResponse signup(SignupRequest request);
    LoginResponse login(LoginRequest request);
    EmailValidationResponse validateEmail(EmailValidationRequest request);

    /**
     * 구글 로그인
     */
    LoginResponse googleLogin(GoogleLoginRequest request);
}
