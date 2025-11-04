package com.example.GoSonGim_BE.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

@Component
@ConfigurationProperties(prefix = "spring.security.oauth2.client.registration.google")
@Data
public class OAuthProperties {
    private String clientId;
    private String clientSecret;
    private String redirectUri;
    
    @Value("${spring.security.oauth2.client.provider.google.token-uri}")
    private String tokenUri;
}
