package com.buy01.media.infrastructure.web.exception;

import static org.springframework.http.HttpStatus.*;

import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebInputException;
import org.springframework.web.servlet.NoHandlerFoundException;

import com.buy01.media.infrastructure.web.exception.Errors.Faileduploadedfile;
import com.buy01.media.infrastructure.web.exception.Errors.NotFound;

import io.jsonwebtoken.ExpiredJwtException;
import lombok.extern.slf4j.Slf4j;

import java.security.SignatureException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Map<Class<? extends Throwable>, ProblemTemplate> TEMPLATES = new ProblemTemplate.Registry()
            .add(SignatureException.class, UNAUTHORIZED, "Invalid JWT Signature")
            .add(ExpiredJwtException.class, UNAUTHORIZED, "JWT Token Expired")
            .add(AccessDeniedException.class, FORBIDDEN, "Access Denied")
            .add(MissingServletRequestPartException.class, BAD_REQUEST, "Missing Argument")
            .add(HttpMessageNotReadableException.class, BAD_REQUEST, "JSON Not Valid")
            .add(IllegalArgumentException.class, BAD_REQUEST, "Bad Argument")
            .add(NotFoundException.class, NOT_FOUND, "Validation Error")
            .add(UsernameNotFoundException.class, NOT_FOUND, "User Not Found")
            .add(MissingServletRequestParameterException.class, BAD_REQUEST, "Missing Argument")
            .add(Faileduploadedfile.class, CONFLICT, "Failed to uploaded file")
            .add(NotFound.class, NOT_FOUND, "Not Found")
            .add(NoHandlerFoundException.class, NOT_FOUND, "Path Not Found")
            .add(ServerWebInputException.class, BAD_REQUEST, "No request body")
            .add(WebExchangeBindException.class, BAD_REQUEST, "Validation Failed",
                    ex -> "Fields: " + ((WebExchangeBindException) ex).getBindingResult().getFieldErrorCount())
            .build();

    private static final ProblemTemplate DEFAULT_TEMPLATE = new ProblemTemplate(
            INTERNAL_SERVER_ERROR,
            Throwable::getMessage,
            "Internal Server Error");

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ProblemDetailResponse> handleResponseStatusException(ResponseStatusException ex) {
        return ResponseEntity.status(ex.getStatus())
                .body(new ProblemDetailResponse(ex.getStatus().value(), "Application Error", ex.getReason()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetailResponse> handle(Exception ex) {
        ProblemTemplate template = TEMPLATES.get(ex.getClass());
        if (template == null) {
            log.warn("Exception not handled happen: ", ex);
            template = DEFAULT_TEMPLATE;
        }
        return ResponseEntity.status(template.status())
                .body(new ProblemDetailResponse(
                        template.status().value(),
                        template.title(),
                        template.detailGenerator().apply(ex)));
    }

    static final class ProblemDetailResponse {
        private final int status;
        private final String title;
        private final String detail;

        ProblemDetailResponse(int status, String title, String detail) {
            this.status = status;
            this.title = title;
            this.detail = detail;
        }

        public int getStatus() {
            return status;
        }

        public String getTitle() {
            return title;
        }

        public String getDetail() {
            return detail;
        }
    }
}

final class ProblemTemplate {

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