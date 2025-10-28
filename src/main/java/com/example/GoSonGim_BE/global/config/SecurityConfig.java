package com.example.GoSonGim_BE.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.example.GoSonGim_BE.global.constant.ApiVersion;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // REST API는 Stateless이므로 CSRF 불필요
            .csrf(csrf -> csrf.disable())
            // 세션 생성 정책 명시
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authz -> authz
                .requestMatchers(
                        ApiVersion.CURRENT + "/auth/**",
                        ApiVersion.CURRENT + "/files/**",
                        ApiVersion.CURRENT + "/kits/**"
                ).permitAll()
                .anyRequest().authenticated()
            );
        
        return http.build();
    }
}
