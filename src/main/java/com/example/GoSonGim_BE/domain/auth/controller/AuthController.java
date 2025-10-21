package com.example.GoSonGim_BE.domain.auth.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.GoSonGim_BE.domain.auth.dto.request.LoginRequest;
import com.example.GoSonGim_BE.domain.auth.dto.request.SignupRequest;
import com.example.GoSonGim_BE.domain.auth.dto.response.LoginResponse;
import com.example.GoSonGim_BE.domain.auth.dto.response.SignupResponse;
import com.example.GoSonGim_BE.domain.auth.service.AuthService;
import com.example.GoSonGim_BE.global.dto.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/${api.version}/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    /**
     * 이메일 회원가입
     */
    @PostMapping("/email/signup")
    public ResponseEntity<ApiResponse<SignupResponse>> signup(@Valid @RequestBody SignupRequest request) {
        SignupResponse response = authService.signup(request);
        ApiResponse<SignupResponse> apiResponse = ApiResponse.success(201, "회원가입이 완료되었습니다.", response);
        return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
    }
    
    /**
     * 이메일 로그인
     */
    @PostMapping("/email/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        ApiResponse<LoginResponse> apiResponse = ApiResponse.success(200, "로그인을 성공했습니다.", response);
        return ResponseEntity.ok(apiResponse);
    }
}
