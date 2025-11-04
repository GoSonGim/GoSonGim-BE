package com.example.GoSonGim_BE.domain.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
import com.example.GoSonGim_BE.domain.auth.dto.request.LogoutRequest;
import com.example.GoSonGim_BE.domain.auth.dto.request.RefreshTokenRequest;
import com.example.GoSonGim_BE.domain.auth.dto.request.SignupRequest;
import com.example.GoSonGim_BE.domain.auth.dto.response.EmailValidationResponse;
import com.example.GoSonGim_BE.domain.auth.dto.response.LoginResponse;
import com.example.GoSonGim_BE.domain.auth.dto.response.LogoutResponse;
import com.example.GoSonGim_BE.domain.auth.dto.response.SignupResponse;
import com.example.GoSonGim_BE.domain.auth.dto.response.TokenResponse;
import com.example.GoSonGim_BE.domain.auth.service.AuthService;
import com.example.GoSonGim_BE.global.constant.ApiVersion;
import com.example.GoSonGim_BE.global.dto.ApiResult;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Auth API")
@RestController
@RequestMapping(ApiVersion.CURRENT + "/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    /**
     * 이메일 회원가입
     */
    @Operation(summary = "이메일 회원가입")
    @PostMapping("/email/signup")
    public ResponseEntity<ApiResult<SignupResponse>> signup(@Valid @RequestBody SignupRequest request) {
        SignupResponse response = authService.signup(request);
        ApiResult<SignupResponse> apiResult = ApiResult.success(201, "회원가입이 완료되었습니다.", response);
        return ResponseEntity.status(HttpStatus.CREATED).body(apiResult);
    }
    
    /**
     * 이메일 로그인
     */
    @Operation(summary = "이메일 로그인")
    @PostMapping("/email/login")
    public ResponseEntity<ApiResult<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        ApiResult<LoginResponse> apiResult = ApiResult.success(200, "로그인을 성공했습니다.", response);
        return ResponseEntity.ok(apiResult);
    }
    
    /**
     * 이메일 중복 확인
     */
    @Operation(summary = "이메일 중복 확인")
    @GetMapping("/email/validate")
    public ResponseEntity<ApiResult<EmailValidationResponse>> validateEmail(@Valid EmailValidationRequest request) {
        EmailValidationResponse response = authService.validateEmail(request);
        
        String message = response.available() ? "사용 가능한 이메일입니다." : "이미 사용 중인 이메일입니다.";
        ApiResult<EmailValidationResponse> apiResult = ApiResult.success(200, message, response);
        
        return ResponseEntity.ok(apiResult);
    }
    
    /**
     * 구글 OAuth 로그인
     */
    @Operation(summary = "구글 OAuth 로그인")
    @PostMapping("/google/login")
    public ResponseEntity<ApiResult<LoginResponse>> googleLogin(@Valid @RequestBody GoogleLoginRequest request) {
        LoginResponse response = authService.googleLogin(request);
        ApiResult<LoginResponse> apiResult = ApiResult.success(200, "구글 로그인을 성공했습니다.", response);
        return ResponseEntity.ok(apiResult);
    }

    /**
     * Refresh Token으로 Access Token 재발급
     */
    @Operation(summary = "토큰 재발급")
    @PostMapping("/refresh")
    public ResponseEntity<ApiResult<TokenResponse>> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        TokenResponse response = authService.refresh(request);
        ApiResult<TokenResponse> apiResult = ApiResult.success(200, "토큰 재발급을 성공했습니다.", response);
        return ResponseEntity.ok(apiResult);
    }
    
    /**
     * 로그아웃
     */
    @Operation(summary = "로그아웃")
    @PostMapping("/logout")
    public ResponseEntity<ApiResult<LogoutResponse>> logout(@Valid @RequestBody LogoutRequest request) {
        LogoutResponse response = authService.logout(request);
        ApiResult<LogoutResponse> apiResult = ApiResult.success(200, "로그아웃이 완료되었습니다.", response);
        return ResponseEntity.ok(apiResult);
    }
}
