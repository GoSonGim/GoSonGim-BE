package com.example.GoSonGim_BE.domain.auth.service;

import java.util.Collections;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.GoSonGim_BE.domain.auth.dto.external.GoogleUserInfo;
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
import com.example.GoSonGim_BE.domain.auth.dto.response.UserResponse;
import com.example.GoSonGim_BE.domain.auth.entity.RefreshToken;
import com.example.GoSonGim_BE.domain.auth.entity.UserLocalCredential;
import com.example.GoSonGim_BE.domain.auth.entity.UserOAuthCredential;
import com.example.GoSonGim_BE.domain.auth.exception.AuthExceptions;
import com.example.GoSonGim_BE.domain.auth.repository.UserLocalCredentialRepository;
import com.example.GoSonGim_BE.domain.auth.repository.UserOAuthCredentialRepository;
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
    private final UserOAuthCredentialRepository userOAuthCredentialRepository;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final OAuthProperties oauthProperties;
    private final JwtProvider jwtProvider;

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

        // 6. JWT 토큰 생성
        TokenResponse tokens = generateTokenResponse(savedCredential.getUser().getId());

        // 7. 사용자 정보
        UserResponse userResponse = new UserResponse(
            savedCredential.getUser().getId(),
            savedCredential.getEmail()
        );

        // 8. 응답 생성
        return new SignupResponse(tokens, userResponse);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
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
        
        // 4. JWT 토큰 생성
        TokenResponse tokens = generateTokenResponse(credential.getUser().getId());
        
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

    /**
     * 구글 OAuth 로그인 처리
     * 
     * @param request 구글 로그인 요청 (code, redirectUri)
     * @return LoginResponse 로그인 응답 (사용자 정보)
     * @throws AuthExceptions.InvalidRedirectUriException 유효하지 않은 redirectUri
     * @throws AuthExceptions.OAuthTokenInvalidException 구글 인증 실패
     */
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
        User user = findOrCreateOAuthUser(googleUserInfo);

        // 4. JWT 토큰 생성
        TokenResponse tokens = generateTokenResponse(user.getId());

        // 5. 응답 생성
        UserResponse userResponse = new UserResponse(
            user.getId(),
            googleUserInfo.providerEmail()
        );

        return new LoginResponse(tokens, userResponse);
    }

    /**
     * 구글 Authorization Code를 ID Token으로 교환
     * 
     * @param code 프론트엔드에서 받은 구글 Authorization Code
     * @param redirectUri OAuth 리디렉션 URI (검증용)
     * @return String ID Token (사용자 정보가 포함된 JWT)
     * @throws AuthExceptions.OAuthTokenInvalidException 토큰 교환 실패 시
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
     * ID Token 검증 및 사용자 정보 추출
     * 
     * @param idToken 구글에서 발급받은 ID Token
     * @return GoogleUserInfo 구글 사용자 정보 (providerId, email, name)
     * @throws AuthExceptions.OAuthTokenInvalidException ID Token이 유효하지 않거나 검증 실패 시
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
            String providerId = payload.getSubject();
            String providerEmail = payload.getEmail();
            String name = (String) payload.get("name");
            
            return new GoogleUserInfo(providerId, providerEmail, name);
            
        } catch (Exception e) {
            throw new AuthExceptions.OAuthTokenInvalidException();
        }
    }
    
    /**
     * 구글 사용자 정보로 User 조회 또는 생성
     * 
     * - 기존 사용자: provider + providerId로 조회하여 반환
     * - 신규 사용자: User 생성 후 UserOAuthCredential 저장
     * 
     * @param googleUserInfo 구글 사용자 정보 (providerId, email, name)
     * @return User 조회되거나 생성된 사용자 엔티티
     */
    private User findOrCreateOAuthUser(GoogleUserInfo googleUserInfo) {
        // 1. provider + providerId로 사용자 조회 (고유 식별)
        Optional<UserOAuthCredential> existingOAuth = userOAuthCredentialRepository.findByProviderAndProviderId("GOOGLE", googleUserInfo.providerId());

        if (existingOAuth.isPresent()) {
            // 2-1. 기존 사용자가 있으면 반환
            return existingOAuth.get().getUser();
        }

        // 2-2 신규 사용자 생성
        User newUser = userService.createDefaultUser();

        // 3. OAuth 인증 정보 저장
        UserOAuthCredential userOAuthCredential = UserOAuthCredential.builder()
            .user(newUser)
            .provider("GOOGLE")
            .providerId(googleUserInfo.providerId())
            .providerEmail(googleUserInfo.providerEmail())
            .build();
        
        userOAuthCredentialRepository.save(userOAuthCredential);

        // 4. 신규 사용자 반환
        return newUser;
    }

    /**
     * JWT 토큰 생성
     * 
     * @param userId 사용자 ID
     * @return TokenResponse
     */
    private TokenResponse generateTokenResponse(Long userId) {
        String accessToken = jwtProvider.generateAccessToken(userId);
        String refreshToken = jwtProvider.generateRefreshToken(userId);
        
        return new TokenResponse(
            accessToken,
            refreshToken,
            "Bearer",
            3600,    // 밀리초 → 초
            1209600   // 밀리초 → 초
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TokenResponse refresh(RefreshTokenRequest request) {
        // 1. Refresh Token 검증 및 조회 (JwtProvider에 위임)
        RefreshToken refreshToken = jwtProvider.validateAndGetRefreshToken(request.refreshToken());
    
        // 2. 새로운 토큰 발급 (JwtProvider에 위임)
        String newAccessToken = jwtProvider.generateAccessToken(refreshToken.getUser().getId());
        String newRefreshToken = jwtProvider.rotateRefreshToken(refreshToken);
        
        // 3. 응답 생성
        return new TokenResponse(
            newAccessToken,
            newRefreshToken,
            "Bearer",
            3600,
            1209600
        );
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public LogoutResponse logout(LogoutRequest request) {
        // Refresh Token 폐기 처리
        jwtProvider.revokeRefreshTokenForLogout(request.refreshToken());
        
        // 응답 생성
        return new LogoutResponse(true, "Refresh Token이 정상적으로 폐기되었습니다.");
    }
}
