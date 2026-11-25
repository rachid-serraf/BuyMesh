package com.buy01.user.infrastructure.config;

import java.nio.file.NoSuchFileException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.servlet.NoHandlerFoundException;

import com.buy01.user.domain.exception.InvalidCredentialsException;
import com.buy01.user.domain.exception.UserAlreadyExistsException;
import com.buy01.user.domain.exception.UserNotFoundException;

import io.jsonwebtoken.JwtException;
import javax.validation.ConstraintViolationException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    public static final String BAD_REQUEST = "Bad Request";
    public static final String NOT_FOUND = "Not Found";

    private ResponseEntity<Map<String, Object>> problem(HttpStatus status, String title, String detail, WebRequest request) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("type", "about:blank");
        body.put("title", title);
        body.put("status", status.value());
        body.put("detail", detail);
        String description = request.getDescription(false);
        if (description != null && description.startsWith("uri=")) {
            body.put("instance", description.substring("uri=".length()));
        }
        return ResponseEntity.status(status).body(body);
    }

    // =========================
    // Business Exceptions
    // =========================
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<Map<String, Object>> handleUserAlreadyExists(UserAlreadyExistsException ex, WebRequest request) {
        return problem(HttpStatus.CONFLICT, "Conflict", ex.getMessage(), request);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidCredentials(InvalidCredentialsException ex, WebRequest request) {
        return problem(HttpStatus.UNAUTHORIZED, "Unauthorized", ex.getMessage(), request);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleUserNotFound(UserNotFoundException ex, WebRequest request) {
        return problem(HttpStatus.NOT_FOUND, NOT_FOUND, ex.getMessage(), request);
    }

    // =========================
    // Security Exceptions
    // =========================
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDenied(AccessDeniedException ex, WebRequest request) {
        return problem(HttpStatus.FORBIDDEN, "Forbidden", "You do not have permission to access this resource.", request);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleBadCredentials(BadCredentialsException ex, WebRequest request) {
        return problem(HttpStatus.BAD_REQUEST, BAD_REQUEST, "Invalid email or password provided.", request);
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<Map<String, Object>> handleUserDisabled(DisabledException ex, WebRequest request) {
        return problem(HttpStatus.BAD_REQUEST, BAD_REQUEST, "This account is currently disabled.", request);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Map<String, Object>> handleAuthenticationException(AuthenticationException ex, WebRequest request) {
        return problem(HttpStatus.UNAUTHORIZED, "Unauthorized", "Authentication failed: " + ex.getMessage(), request);
    }

    // =========================
    // Validation & Parsing
    // =========================
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(MethodArgumentNotValidException ex, WebRequest request) {
        String errors = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return problem(HttpStatus.BAD_REQUEST, BAD_REQUEST, "Validation failed: " + errors, request);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> handleTypeMismatch(MethodArgumentTypeMismatchException ex, WebRequest request) {
        String detail;
        if (ex.getRequiredType() == UUID.class) {
            detail = "Invalid UUID format: " + ex.getValue();
        } else {
            detail = "Invalid parameter: " + ex.getName();
        }
        return problem(HttpStatus.BAD_REQUEST, BAD_REQUEST, detail, request);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleConstraintViolation(ConstraintViolationException ex, WebRequest request) {
        return problem(HttpStatus.BAD_REQUEST, BAD_REQUEST, "Constraint validation failed.", request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidJson(HttpMessageNotReadableException ex, WebRequest request) {
        return problem(HttpStatus.BAD_REQUEST, BAD_REQUEST, "Invalid JSON format: " + ex.getMessage(), request);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex, WebRequest request) {
        return problem(HttpStatus.BAD_REQUEST, BAD_REQUEST, ex.getMessage(), request);
    }

    // =========================
    // Request & Media Type
    // =========================
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Map<String, Object>> handleMissingRequestParameter(MissingServletRequestParameterException ex, WebRequest request) {
        return problem(HttpStatus.BAD_REQUEST, BAD_REQUEST, "Missing parameter: " + ex.getParameterName(), request);
    }

    @ExceptionHandler(MissingPathVariableException.class)
    public ResponseEntity<Map<String, Object>> handleMissingPathVariable(MissingPathVariableException ex, WebRequest request) {
        return problem(HttpStatus.BAD_REQUEST, BAD_REQUEST, "Missing path variable.", request);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<Map<String, Object>> handleUnsupportedMediaType(HttpMediaTypeNotSupportedException ex, WebRequest request) {
        return problem(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Unsupported Media Type", "Media type not supported.", request);
    }

    @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
    public ResponseEntity<Map<String, Object>> handleMediaTypeNotAcceptable(HttpMediaTypeNotAcceptableException ex, WebRequest request) {
        return problem(HttpStatus.NOT_ACCEPTABLE, "Not Acceptable", "Media type not acceptable.", request);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Map<String, Object>> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex, WebRequest request) {
        return problem(HttpStatus.METHOD_NOT_ALLOWED, "Method Not Allowed", "Method not allowed: " + ex.getMethod(), request);
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<Map<String, Object>> handleEndpointNotFound(NoHandlerFoundException ex, WebRequest request) {
        return problem(HttpStatus.NOT_FOUND, NOT_FOUND, "Endpoint not found.", request);
    }

    // =========================
    // Database & JWT
    // =========================
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDataIntegrityViolation(DataIntegrityViolationException ex, WebRequest request) {
        return problem(HttpStatus.BAD_REQUEST, BAD_REQUEST, "Data integrity violation.", request);
    }

    @ExceptionHandler(JwtException.class)
    public ResponseEntity<Map<String, Object>> handleJwtException(JwtException ex, WebRequest request) {
        return problem(HttpStatus.BAD_REQUEST, BAD_REQUEST, "JWT processing error.", request);
    }

    // =========================
    // Multipart & Files
    // =========================
    @ExceptionHandler(MultipartException.class)
    public ResponseEntity<Map<String, Object>> handleMultipartException(MultipartException ex, WebRequest request) {
        return problem(HttpStatus.BAD_REQUEST, BAD_REQUEST, "Multipart request error.", request);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Map<String, Object>> handleFileSizeExceeded(MaxUploadSizeExceededException ex, WebRequest request) {
        return problem(HttpStatus.PAYLOAD_TOO_LARGE, "Payload Too Large", "File too large.", request);
    }

    @ExceptionHandler(NoSuchFileException.class)
    public ResponseEntity<Map<String, Object>> handleFileNotFound(NoSuchFileException ex, WebRequest request) {
        return problem(HttpStatus.NOT_FOUND, NOT_FOUND, "File not found.", request);
    }

    // =========================
    // Fallback
    // =========================
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex, WebRequest request) {
        return problem(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", "An unexpected error occurred.", request);
    }
}
