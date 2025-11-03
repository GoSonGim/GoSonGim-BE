package com.example.GoSonGim_BE.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.example.GoSonGim_BE.global.constant.ApiVersion;
import com.example.GoSonGim_BE.global.security.jwt.JwtAuthenticationEntryPoint;
import com.example.GoSonGim_BE.global.security.jwt.JwtAuthenticationFilter;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    private static final String[] PUBLIC_ENDPOINTS = new String[] {
            ApiVersion.CURRENT + "/auth/**",
            ApiVersion.CURRENT + "/files/**",
            ApiVersion.CURRENT + "/health",
            ApiVersion.CURRENT + "/ready",
            ApiVersion.CURRENT + "/live",
            "/error",  // 에러 핸들러 경로는 인증 불필요
            "/error/**"
    };

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 1) 공개 엔드포인트 전용 체인: JWT 필터 없이 전부 허용
    @Bean
    @Order(1)
    public SecurityFilterChain publicFilterChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher(PUBLIC_ENDPOINTS)
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authz -> authz.anyRequest().permitAll());
        return http.build();
    }

    // 2) 기본 체인: 나머지 모든 요청은 JWT 인증 필요
    @Bean
    @Order(2)
    public SecurityFilterChain appFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .authorizeHttpRequests(authz -> authz.anyRequest().authenticated())
            .exceptionHandling(ex -> ex.authenticationEntryPoint(jwtAuthenticationEntryPoint));
        return http.build();
    }
}
