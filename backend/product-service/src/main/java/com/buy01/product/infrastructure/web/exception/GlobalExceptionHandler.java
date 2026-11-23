package com.buy01.product.infrastructure.web.exception;

import static org.springframework.http.HttpStatus.*;

import java.security.SignatureException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.core.codec.DecodingException;
import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.MethodNotAllowedException;
import org.springframework.web.server.ServerWebInputException;

import com.buy01.product.infrastructure.web.exception.Errors.MediaServiceException;
import com.buy01.product.infrastructure.web.exception.Errors.StockConflictException;

import io.jsonwebtoken.ExpiredJwtException;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    private static final Map<Class<? extends Throwable>, ProblemTemplate> TEMPLATES = new ProblemTemplate.Registry()
            .add(SignatureException.class, UNAUTHORIZED, "Invalid JWT Signature")
            .add(ExpiredJwtException.class, UNAUTHORIZED, "JWT Token Expired")
            .add(AccessDeniedException.class, FORBIDDEN, "Access Denied")
            .add(HttpMessageNotReadableException.class, BAD_REQUEST, "JSON Not Valid")
            .add(IllegalArgumentException.class, BAD_REQUEST, "Bad Argument")
            .add(NotFoundException.class, NOT_FOUND, "Validation Error")
            .add(UsernameNotFoundException.class, NOT_FOUND, "User Not Found")
            .add(MethodNotAllowedException.class, METHOD_NOT_ALLOWED, "Method Not Allowed")
            .add(StockConflictException.class, CONFLICT, "Stock Conflict")
            .add(ServerWebInputException.class, BAD_REQUEST, "No request body")
            .add(WebExchangeBindException.class, BAD_REQUEST, "Validation Failed",
                    ex -> ((WebExchangeBindException) ex).getBindingResult().getFieldErrors()
                            .stream()
                            .map(fe -> fe.getField() + ": "
                                    + (fe.getDefaultMessage() != null ? fe.getDefaultMessage() : fe.toString()))
                            .collect(Collectors.joining("; ")))
            .build();

    private static final ProblemTemplate DEFAULT_TEMPLATE = new ProblemTemplate(
            INTERNAL_SERVER_ERROR,
            Throwable::getMessage,
            "Internal Server Error");

    @ExceptionHandler(MediaServiceException.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleMediaServiceException(MediaServiceException ex) {
        HttpStatus status = (ex.getStatusCode() >= 100 && ex.getStatusCode() < 600)
                ? HttpStatus.valueOf(ex.getStatusCode())
                : INTERNAL_SERVER_ERROR;

        String title;
        if (status.is4xxClientError()) {
            title = "Media Service Client Error";
        } else if (status.is5xxServerError()) {
            title = "Media Service Server Error";
        } else {
            title = "Media Service Unavailable";
        }

        String detail = ex.getMessage();
        if (ex.getResponseBody() != null && !ex.getResponseBody().isBlank()) {
            detail += " → " + ex.getResponseBody().trim();
        }

        Map<String, Object> properties = new HashMap<>();
        properties.put("externalService", "media");
        properties.put("externalStatus", ex.getStatusCode());
        if (ex.getResponseBody() != null) {
            properties.put("externalDetail", ex.getResponseBody());
        }

        if (status.is5xxServerError() || ex.getStatusCode() == 0) {
            log.error("Media service failure: status={}, detail={}", ex.getStatusCode(), detail, ex);
        } else {
            log.warn("Media service client issue: status={}, detail={}", ex.getStatusCode(), detail);
        }

        return Mono.just(ResponseEntity.status(status).body(problem(status, title, detail, properties)));
    }

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<Map<String, Object>>> handle(Exception ex) {
        ProblemTemplate template = TEMPLATES.get(ex.getClass());
        if (template == null) {
            log.warn("Exception not handled: {}", ex.getClass().getSimpleName(), ex);
            template = DEFAULT_TEMPLATE;
        }
        return Mono.just(ResponseEntity.status(template.status())
                .body(problem(template.status(), template.title(),
                        template.detailGenerator().apply(ex), null)));
    }

    private static Map<String, Object> problem(HttpStatus status, String title, String detail,
            Map<String, Object> extra) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("type", "about:blank");
        body.put("title", title);
        body.put("status", status.value());
        body.put("detail", detail);
        if (extra != null) {
            body.putAll(extra);
        }
        return body;
    }
}

class ProblemTemplate {
    private final HttpStatus status;
    private final DetailGenerator detailGenerator;
    private final String title;

    ProblemTemplate(HttpStatus status, DetailGenerator detailGenerator, String title) {
        this.status = status;
        this.detailGenerator = detailGenerator;
        this.title = title;
    }

    HttpStatus status() {
        return status;
    }

    DetailGenerator detailGenerator() {
        return detailGenerator;
    }

    String title() {
        return title;
    }

    public static class Registry {
        private final Map<Class<? extends Throwable>, ProblemTemplate> map = new HashMap<>();

        public Registry add(Class<? extends Throwable> type, HttpStatus status, String title) {
            map.put(type, new ProblemTemplate(status, Throwable::getMessage, title));
            return this;
        }

        public Registry add(Class<? extends Throwable> type, HttpStatus status, String title,
                DetailGenerator gen) {
            map.put(type, new ProblemTemplate(status, gen, title));
            return this;
        }

        public Map<Class<? extends Throwable>, ProblemTemplate> build() {
            return Map.copyOf(map);
        }
    }
}

@FunctionalInterface
interface DetailGenerator {
    String apply(Throwable ex);
}
