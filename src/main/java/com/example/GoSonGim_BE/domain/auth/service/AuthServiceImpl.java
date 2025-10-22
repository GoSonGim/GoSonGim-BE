package com.example.GoSonGim_BE.domain.auth.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.GoSonGim_BE.domain.auth.dto.request.EmailValidationRequest;
import com.example.GoSonGim_BE.domain.auth.dto.request.LoginRequest;
import com.example.GoSonGim_BE.domain.auth.dto.request.SignupRequest;
import com.example.GoSonGim_BE.domain.auth.dto.response.EmailValidationResponse;
import com.example.GoSonGim_BE.domain.auth.dto.response.LoginResponse;
import com.example.GoSonGim_BE.domain.auth.dto.response.SignupResponse;
import com.example.GoSonGim_BE.domain.auth.dto.response.TokenResponse;
import com.example.GoSonGim_BE.domain.auth.dto.response.UserResponse;
import com.example.GoSonGim_BE.domain.auth.entity.UserLocalCredential;
import com.example.GoSonGim_BE.domain.auth.exception.AuthExceptions;
import com.example.GoSonGim_BE.domain.auth.repository.UserLocalCredentialRepository;
import com.example.GoSonGim_BE.domain.users.entity.User;
import com.example.GoSonGim_BE.domain.users.service.UserService;

import lombok.RequiredArgsConstructor;
    
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    
    private final UserLocalCredentialRepository userLocalCredentialRepository;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SignupResponse signup(SignupRequest request) {
        // 1. 이메일 중복 확인
        if (userLocalCredentialRepository.existsByEmail(request.email())) {
            throw new AuthExceptions.EmailAlreadyUsedException(request.email());
        }

        // 2. User 생성 (기본값으로 생성)
        User user = userService.createDefaultUser();

        // 3. 비밀번호 해싱
        String hashedPassword = passwordEncoder.encode(request.password());

        // 4. UserLocalCredential 생성
        UserLocalCredential credential = UserLocalCredential.builder()
                .user(user)
                .email(request.email())
                .emailVerified(false)
                .passwordHash(hashedPassword)
                .build();

        // 5. 저장
        UserLocalCredential savedCredential = userLocalCredentialRepository.save(credential);

        // TODO: JWT 토큰 생성
        // 6. JWT 토큰 생성 (일단 더미 값)
        TokenResponse tokens = new TokenResponse(
            "dummy_access_token",  // 나중에 실제 JWT로 교체
            "dummy_refresh_token", // 나중에 실제 JWT로 교체
            "Bearer",
            3600,
            1209600
        );

        // 7. 사용자 정보
        UserResponse userResponse = new UserResponse(
            savedCredential.getUser().getId(),
            savedCredential.getEmail()
        );

        // 8. 응답 생성
        return new SignupResponse(tokens, userResponse);
    }
    
    @Override
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        // 1. 이메일로 사용자 조회
        UserLocalCredential credential = userLocalCredentialRepository.findByEmail(request.email())
                .orElseThrow(() -> new AuthExceptions.InvalidCredentialsException());
        
        // 2. 탈퇴한 계정 체크
        if (credential.getUser().getIsDeleted()) {
            throw new AuthExceptions.UserDeletedException();
        }
        
        // 3. 비밀번호 검증
        if (!passwordEncoder.matches(request.password(), credential.getPasswordHash())) {
            throw new AuthExceptions.InvalidCredentialsException();
        }
        
        // TODO: JWT 토큰 생성
        // 4. JWT 토큰 생성 (일단 더미 값)
        TokenResponse tokens = new TokenResponse(
            "dummy_access_token",  // 나중에 실제 JWT로 교체
            "dummy_refresh_token", // 나중에 실제 JWT로 교체
            "Bearer",
            3600,
            1209600
        );
        
        // 5. 사용자 정보
        UserResponse userResponse = new UserResponse(
            credential.getUser().getId(),
            credential.getEmail()
        );
        
        // 6. 응답 생성
        return new LoginResponse(tokens, userResponse);
    }
    
    @Override
    @Transactional(readOnly = true)
    public EmailValidationResponse validateEmail(EmailValidationRequest request) {
        // 1. 이메일 중복 확인
        boolean isAvailable = !userLocalCredentialRepository.existsByEmail(request.email());
        
        // 2. 응답 생성
        return new EmailValidationResponse(request.email(), isAvailable);
    }
}
