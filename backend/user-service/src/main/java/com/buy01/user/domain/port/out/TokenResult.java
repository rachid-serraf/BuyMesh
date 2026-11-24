package com.buy01.user.domain.port.out;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TokenResult {

    private final String token;
    private final Instant expiresAt;
}
