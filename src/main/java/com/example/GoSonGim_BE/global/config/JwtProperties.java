package com.example.GoSonGim_BE.global.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Component
@ConfigurationProperties(prefix = "jwt")
@Getter
@Setter
public class JwtProperties {
    private String secret;
    private AccessToken accessToken = new AccessToken();
    private RefreshToken refreshToken = new RefreshToken();

    @Getter
    @Setter
    public static class AccessToken {
        private Long expiration; // 24시간 (밀리초)
    }

    @Getter
    @Setter
    public static class RefreshToken {
        private Long expiration; // 14일 (밀리초)
    }
}
