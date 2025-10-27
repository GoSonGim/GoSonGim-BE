package com.example.GoSonGim_BE.domain.auth.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.GoSonGim_BE.domain.auth.dto.request.EmailValidationRequest;
import com.example.GoSonGim_BE.domain.auth.dto.request.GoogleLoginRequest;
import com.example.GoSonGim_BE.domain.auth.dto.request.LoginRequest;
import com.example.GoSonGim_BE.domain.auth.dto.request.RefreshTokenRequest;
import com.example.GoSonGim_BE.domain.auth.dto.request.SignupRequest;
import com.example.GoSonGim_BE.domain.auth.dto.response.EmailValidationResponse;
import com.example.GoSonGim_BE.domain.auth.dto.response.LoginResponse;
import com.example.GoSonGim_BE.domain.auth.dto.response.SignupResponse;
import com.example.GoSonGim_BE.domain.auth.dto.response.TokenResponse;
import com.example.GoSonGim_BE.domain.auth.service.AuthService;
import com.example.GoSonGim_BE.global.constant.ApiVersion;
import com.example.GoSonGim_BE.global.dto.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(ApiVersion.CURRENT + "/auth")
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
    
    /**
     * 이메일 중복 확인
     */
    @GetMapping("/email/validate")
    public ResponseEntity<ApiResponse<EmailValidationResponse>> validateEmail(@Valid EmailValidationRequest request) {
        EmailValidationResponse response = authService.validateEmail(request);
        
        String message = response.available() ? "사용 가능한 이메일입니다." : "이미 사용 중인 이메일입니다.";
        ApiResponse<EmailValidationResponse> apiResponse = ApiResponse.success(200, message, response);
        
        return ResponseEntity.ok(apiResponse);
    }
    
    /**
     * 구글 OAuth 로그인
     */
    @PostMapping("/google/login")
    public ResponseEntity<ApiResponse<LoginResponse>> googleLogin(@Valid @RequestBody GoogleLoginRequest request) {
        LoginResponse response = authService.googleLogin(request);
        ApiResponse<LoginResponse> apiResponse = ApiResponse.success(200, "구글 로그인을 성공했습니다.", response);
        return ResponseEntity.ok(apiResponse);
    }

    /**
     * Refresh Token으로 Access Token 재발급
     */
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<TokenResponse>> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        TokenResponse response = authService.refresh(request);
        ApiResponse<TokenResponse> apiResponse = ApiResponse.success(200, "토큰 재발급을 성공했습니다.", response);
        return ResponseEntity.ok(apiResponse);
    }
}
