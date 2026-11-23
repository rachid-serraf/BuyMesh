package com.buy01.user.infrastructure.adapters.in.web.dto.response;

public class LoginResponse {

    private final String token;
    private final String expiresAt;

    public LoginResponse(String token, String expiresAt) {
        this.token = token;
        this.expiresAt = expiresAt;
    }

    public String getToken() {
        return token;
    }

    public String getExpiresAt() {
        return expiresAt;
    }

    public String token() {
        return token;
    }

    public String expiresAt() {
        return expiresAt;
    }
}
