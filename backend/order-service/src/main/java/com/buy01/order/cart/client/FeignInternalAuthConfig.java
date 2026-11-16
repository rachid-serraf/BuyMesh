package com.buy01.order.cart.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import feign.RequestInterceptor;

@Configuration
public class FeignInternalAuthConfig {

    @Bean
    public RequestInterceptor internalApiKeyInterceptor(
            @Value("${internal.api-key:local-dev-internal-key}") String apiKey) {
        return template -> {
            if (template.path().startsWith("/internal/")) {
                template.header("X-Internal-Api-Key", apiKey);
            }
        };
    }
}
