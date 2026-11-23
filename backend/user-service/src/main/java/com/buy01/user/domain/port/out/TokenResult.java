package com.buy01.user.domain.port.out;

import java.time.Instant;

public class TokenResult {

    private final String token;
    private final Instant expiresAt;

    public TokenResult(String token, Instant expiresAt) {
        this.token = token;
        this.expiresAt = expiresAt;
    }

    public String getToken() {
        return token;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public String token() {
        return token;
    }

    public Instant expiresAt() {
        return expiresAt;
    }
}
