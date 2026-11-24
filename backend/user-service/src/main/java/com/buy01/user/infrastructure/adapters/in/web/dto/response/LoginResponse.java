package com.buy01.user.infrastructure.adapters.in.web.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginResponse {

    private final String token;
    private final String expiresAt;
}
