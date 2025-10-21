package com.example.GoSonGim_BE.domain.auth.service;

import com.example.GoSonGim_BE.domain.auth.dto.request.EmailValidationRequest;
import com.example.GoSonGim_BE.domain.auth.dto.request.LoginRequest;
import com.example.GoSonGim_BE.domain.auth.dto.request.SignupRequest;
import com.example.GoSonGim_BE.domain.auth.dto.response.EmailValidationResponse;
import com.example.GoSonGim_BE.domain.auth.dto.response.LoginResponse;
import com.example.GoSonGim_BE.domain.auth.dto.response.SignupResponse;

public interface AuthService {
        /**
     * 회원가입
     */
    SignupResponse signup(SignupRequest request);
    
    /**
     * 로그인
     */
    LoginResponse login(LoginRequest request);
    
    /**
     * 이메일 중복 확인
     */
    EmailValidationResponse validateEmail(EmailValidationRequest request);
}
