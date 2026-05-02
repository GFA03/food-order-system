package com.omnieats.identity_service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    private String secret;
    private long expirationMs;
    private long rememberMeExpirationMs;

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public long getExpirationMs() {
        return expirationMs;
    }

    public void setExpirationMs(long expirationMs) {
        this.expirationMs = expirationMs;
    }

    public long getRememberMeExpirationMs() {
        return rememberMeExpirationMs;
    }

    public void setRememberMeExpirationMs(long rememberMeExpirationMs) {
        this.rememberMeExpirationMs = rememberMeExpirationMs;
    }
}
