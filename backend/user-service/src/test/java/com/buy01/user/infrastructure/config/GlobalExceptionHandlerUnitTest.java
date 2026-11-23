package com.buy01.user.infrastructure.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.nio.file.NoSuchFileException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.buy01.user.domain.exception.InvalidCredentialsException;
import com.buy01.user.domain.exception.UserAlreadyExistsException;
import com.buy01.user.domain.exception.UserNotFoundException;

import io.jsonwebtoken.JwtException;

class GlobalExceptionHandlerUnitTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();
    private final WebRequest request = org.mockito.Mockito.mock(WebRequest.class);

    public GlobalExceptionHandlerUnitTest() {
        org.mockito.Mockito.when(request.getDescription(false)).thenReturn("uri=/test");
    }

    private void assertStatus(HttpStatus expected, ResponseEntity<Map<String, Object>> response) {
        assertEquals(expected, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(expected.value(), response.getBody().get("status"));
        assertNotNull(response.getBody().get("title"));
        assertEquals("/test", response.getBody().get("instance"));
    }

    @Test
    void handleUserAlreadyExists() {
        assertStatus(HttpStatus.CONFLICT,
                handler.handleUserAlreadyExists(new UserAlreadyExistsException("exists"), request));
    }

    @Test
    void handleInvalidCredentials() {
        assertStatus(HttpStatus.UNAUTHORIZED,
                handler.handleInvalidCredentials(new InvalidCredentialsException("bad"), request));
    }

    @Test
    void handleUserNotFound() {
        assertStatus(HttpStatus.NOT_FOUND,
                handler.handleUserNotFound(new UserNotFoundException("not found"), request));
    }

    @Test
    void handleAccessDenied() {
        assertStatus(HttpStatus.FORBIDDEN,
                handler.handleAccessDenied(new AccessDeniedException("denied"), request));
    }

    @Test
    void handleBadCredentials() {
        assertStatus(HttpStatus.BAD_REQUEST,
                handler.handleBadCredentials(new BadCredentialsException("bad"), request));
    }

    @Test
    void handleUserDisabled() {
        assertStatus(HttpStatus.BAD_REQUEST,
                handler.handleUserDisabled(new DisabledException("disabled"), request));
    }

    @Test
    void handleAuthenticationException() {
        assertStatus(HttpStatus.UNAUTHORIZED,
                handler.handleAuthenticationException(new AuthenticationException("auth") {
                }, request));
    }

    @Test
    void handleValidationErrors() throws Exception {
        org.springframework.validation.BeanPropertyBindingResult bindingResult =
            new org.springframework.validation.BeanPropertyBindingResult(new Object(), "object");
        bindingResult.addError(new org.springframework.validation.FieldError("object", "field", "msg"));
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, bindingResult);
        assertStatus(HttpStatus.BAD_REQUEST, handler.handleValidationErrors(ex, request));
    }

    @Test
    void handleTypeMismatchUuid() {
        MethodArgumentTypeMismatchException ex = new MethodArgumentTypeMismatchException(
                "abc", java.util.UUID.class, "id", null, new IllegalArgumentException("bad"));
        assertStatus(HttpStatus.BAD_REQUEST, handler.handleTypeMismatch(ex, request));
    }

    @Test
    void handleTypeMismatchOther() {
        MethodArgumentTypeMismatchException ex = new MethodArgumentTypeMismatchException(
                "abc", Integer.class, "age", null, new IllegalArgumentException("bad"));
        assertStatus(HttpStatus.BAD_REQUEST, handler.handleTypeMismatch(ex, request));
    }

    @Test
    void handleConstraintViolation() {
        assertStatus(HttpStatus.BAD_REQUEST,
                handler.handleConstraintViolation(new javax.validation.ConstraintViolationException(new HashSet<>()), request));
    }

    @Test
    void handleInvalidJson() {
        assertStatus(HttpStatus.BAD_REQUEST,
                handler.handleInvalidJson(new org.springframework.http.converter.HttpMessageNotReadableException("bad"), request));
    }

    @Test
    void handleIllegalArgument() {
        assertStatus(HttpStatus.BAD_REQUEST,
                handler.handleIllegalArgument(new IllegalArgumentException("bad"), request));
    }

    @Test
    void handleMissingRequestParameter() {
        assertStatus(HttpStatus.BAD_REQUEST,
                handler.handleMissingRequestParameter(new MissingServletRequestParameterException("email", "String"), request));
    }

    @Test
    void handleMissingPathVariable() throws Exception {
        assertStatus(HttpStatus.BAD_REQUEST,
                handler.handleMissingPathVariable(
                        new MissingPathVariableException("id", org.springframework.core.MethodParameter.forExecutable(
                                this.getClass().getDeclaredMethod("dummy"), -1)), request));
    }

    void dummy() {
    }

    @Test
    void handleUnsupportedMediaType() {
        assertStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                handler.handleUnsupportedMediaType(
                        new org.springframework.web.HttpMediaTypeNotSupportedException("bad"), request));
    }

    @Test
    void handleMediaTypeNotAcceptable() {
        assertStatus(HttpStatus.NOT_ACCEPTABLE,
                handler.handleMediaTypeNotAcceptable(
                        new org.springframework.web.HttpMediaTypeNotAcceptableException(Collections.emptyList()), request));
    }

    @Test
    void handleMethodNotSupported() {
        assertStatus(HttpStatus.METHOD_NOT_ALLOWED,
                handler.handleMethodNotSupported(
                        new org.springframework.web.HttpRequestMethodNotSupportedException("PUT", Collections.singleton("GET")), request));
    }

    @Test
    void handleEndpointNotFound() {
        assertStatus(HttpStatus.NOT_FOUND,
                handler.handleEndpointNotFound(
                        new org.springframework.web.servlet.NoHandlerFoundException("GET", "/x", new HttpHeaders()), request));
    }

    @Test
    void handleDataIntegrityViolation() {
        assertStatus(HttpStatus.BAD_REQUEST,
                handler.handleDataIntegrityViolation(new DataIntegrityViolationException("dup"), request));
    }

    @Test
    void handleJwtException() {
        assertStatus(HttpStatus.BAD_REQUEST,
                handler.handleJwtException(new JwtException("bad"), request));
    }

    @Test
    void handleMultipartException() {
        assertStatus(HttpStatus.BAD_REQUEST,
                handler.handleMultipartException(new org.springframework.web.multipart.MultipartException("bad"), request));
    }

    @Test
    void handleFileSizeExceeded() {
        assertStatus(HttpStatus.PAYLOAD_TOO_LARGE,
                handler.handleFileSizeExceeded(new org.springframework.web.multipart.MaxUploadSizeExceededException(1000), request));
    }

    @Test
    void handleFileNotFound() {
        assertStatus(HttpStatus.NOT_FOUND,
                handler.handleFileNotFound(new NoSuchFileException("x"), request));
    }

    @Test
    void handleGenericException() {
        assertStatus(HttpStatus.INTERNAL_SERVER_ERROR,
                handler.handleGenericException(new RuntimeException("boom"), request));
    }
}
