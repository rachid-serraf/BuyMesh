package com.buy01.gateway.exception;

import java.nio.charset.StandardCharsets;

import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

@Component
public class GlobalErrorHandler implements ErrorWebExceptionHandler {

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        HttpStatus status = determineHttpStatus(ex);
        String title = determineTitle(ex);
        String detail = ex.getMessage() != null ? ex.getMessage() : "An unexpected error occurred";

        String path = exchange.getRequest().getPath().value();

        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String errorMessage = serializeError(status, title, detail, path);
        byte[] bytes = errorMessage.getBytes(StandardCharsets.UTF_8);
        DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);

        return exchange.getResponse().writeWith(Mono.just(buffer));
    }

    private HttpStatus determineHttpStatus(Throwable ex) {
        if (ex instanceof org.springframework.security.access.AccessDeniedException) {
            return HttpStatus.FORBIDDEN;
        }
        if (ex instanceof org.springframework.security.core.AuthenticationException) {
            return HttpStatus.UNAUTHORIZED;
        }
        if (ex instanceof IllegalArgumentException) {
            return HttpStatus.BAD_REQUEST;
        }

        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    private String determineTitle(Throwable ex) {
        if (ex instanceof org.springframework.security.access.AccessDeniedException) {
            return "Forbidden";
        }
        if (ex instanceof org.springframework.security.core.AuthenticationException) {
            return "Unauthorized";
        }
        if (ex instanceof IllegalArgumentException) {
            return "Bad Request";
        }

        return "Internal Server Error";
    }

    private String serializeError(HttpStatus status, String title, String detail, String instance) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"detail\":\"").append(escapeJson(detail)).append("\",");
        sb.append("\"instance\":\"").append(escapeJson(instance != null ? instance : "")).append("\",");
        sb.append("\"status\":").append(status.value()).append(",");
        sb.append("\"title\":\"").append(escapeJson(title)).append("\"");
        sb.append("}");
        return sb.toString();
    }

    private String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
