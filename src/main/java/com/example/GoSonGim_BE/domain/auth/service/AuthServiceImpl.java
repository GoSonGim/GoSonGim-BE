package com.example.GoSonGim_BE.domain.auth.service;

import java.util.Collections;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.GoSonGim_BE.domain.auth.dto.external.GoogleUserInfo;
import com.example.GoSonGim_BE.domain.auth.dto.request.EmailValidationRequest;
import com.example.GoSonGim_BE.domain.auth.dto.request.GoogleLoginRequest;
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
import com.example.GoSonGim_BE.global.config.OAuthProperties;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;

import lombok.RequiredArgsConstructor;
    
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    
    private final UserLocalCredentialRepository userLocalCredentialRepository;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final OAuthProperties oauthProperties;

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
        // 1. 이메일로 사용자 조회 (Fetch Join으로 User도 함께 조회)
        UserLocalCredential credential = userLocalCredentialRepository.findByEmailWithUser(request.email())
                .orElseThrow(() -> new AuthExceptions.InvalidCredentialsException());
        
        // 2. 탈퇴한 계정 체크
        if (credential.getUser().isDeleted()) {
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginResponse googleLogin(GoogleLoginRequest request) {
        // 1. redirectUri 검증
        if (!oauthProperties.getRedirectUri().equals(request.redirectUri())) {
            throw new AuthExceptions.InvalidRedirectUriException();
        }

        // 2. 구글 Authorization Code를 ID Token으로 교환
        String idToken = exchangeCodeForIdToken(request.code(), request.redirectUri());

        // 3. ID Token에서 사용자 정보 추출 및 User 조회/생성
        GoogleUserInfo googleUserInfo = extractUserInfoFromIdToken(idToken);
        User user = findOrCreateOAuthUser(googleUserInfo.email(), googleUserInfo.name());

        // TODO: 4. JWT 토큰 생성 (추후 구현)

        // 5. 응답 생성
        UserResponse userResponse = new UserResponse(
            user.getId(),
            googleUserInfo.email()
        );

        return new LoginResponse(null, userResponse);
    }

    /**
     * 구글 Authorization Code를 ID Token으로 교환
     */
    private String exchangeCodeForIdToken(String code, String redirectUri) {
        try {
            // Google API Client Library를 사용하여 토큰 교환
            GoogleTokenResponse googleResponse = new GoogleAuthorizationCodeTokenRequest(
                new NetHttpTransport(),
                new GsonFactory(),
                oauthProperties.getTokenUri(),
                oauthProperties.getClientId(),
                oauthProperties.getClientSecret(),
                code,
                redirectUri
            ).execute();
            
            // ID Token만 반환
            return googleResponse.getIdToken();
            
        } catch (Exception e) {
            throw new AuthExceptions.OAuthTokenInvalidException();
        }
    }

    /**
     * ID Token에서 사용자 정보 추출
     */
    private GoogleUserInfo extractUserInfoFromIdToken(String idToken) {
        try {
            // ID Token 검증 및 파싱
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                new NetHttpTransport(), 
                new GsonFactory()
            )
            .setAudience(Collections.singletonList(oauthProperties.getClientId()))
            .build();
            
            GoogleIdToken googleIdToken = verifier.verify(idToken);
            if (googleIdToken == null) {
                throw new AuthExceptions.OAuthTokenInvalidException();
            }
            
            GoogleIdToken.Payload payload = googleIdToken.getPayload();
            String email = payload.getEmail();
            String name = (String) payload.get("name");
            
            return new GoogleUserInfo(email, name);
            
        } catch (Exception e) {
            throw new AuthExceptions.OAuthTokenInvalidException();
        }
    }
    
    /**
     * 이메일로 사용자 조회 또는 생성
     */
    private User findOrCreateOAuthUser(String email, String name) {
        // TODO: UserOAuthCredential 엔티티 생성 후 구현
        // 임시로 기본 사용자 생성
        return userService.createDefaultUser();
    }
}
