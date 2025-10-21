package com.example.GoSonGim_BE.domain.auth.service;

import com.example.GoSonGim_BE.domain.auth.dto.request.LoginRequest;
import com.example.GoSonGim_BE.domain.auth.dto.request.SignupRequest;
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
}
