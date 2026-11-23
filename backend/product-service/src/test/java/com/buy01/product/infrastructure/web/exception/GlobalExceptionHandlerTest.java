package com.buy01.product.infrastructure.web.exception;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.security.SignatureException;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.MethodNotAllowedException;
import org.springframework.web.server.ServerWebInputException;

import com.buy01.product.infrastructure.web.exception.Errors.MediaServiceException;

import io.jsonwebtoken.ExpiredJwtException;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handlesSignatureException() {
        assertProblem(new SignatureException("bad signature"), HttpStatus.UNAUTHORIZED, "Invalid JWT Signature");
    }

    @Test
    void handlesExpiredJwtException() {
        assertProblem(new ExpiredJwtException(null, null, "expired"), HttpStatus.UNAUTHORIZED, "JWT Token Expired");
    }

    @Test
    void handlesAccessDeniedException() {
        assertProblem(new AccessDeniedException("denied"), HttpStatus.FORBIDDEN, "Access Denied");
    }

    @Test
    void handlesHttpMessageNotReadableException() {
        assertProblem(new HttpMessageNotReadableException("bad json"), HttpStatus.BAD_REQUEST, "JSON Not Valid");
    }

    @Test
    void handlesIllegalArgumentException() {
        assertProblem(new IllegalArgumentException("bad argument"), HttpStatus.BAD_REQUEST, "Bad Argument");
    }

    @Test
    void handlesNotFoundException() {
        assertProblem(new NotFoundException(), HttpStatus.NOT_FOUND, "Validation Error");
    }

    @Test
    void handlesUsernameNotFoundException() {
        assertProblem(new UsernameNotFoundException("missing user"), HttpStatus.NOT_FOUND, "User Not Found");
    }

    @Test
    void handlesMethodNotAllowedException() {
        assertProblem(new MethodNotAllowedException("POST", List.of(org.springframework.http.HttpMethod.GET)),
                HttpStatus.METHOD_NOT_ALLOWED, "Method Not Allowed");
    }

    @Test
    void handlesServerWebInputException() {
        assertProblem(new ServerWebInputException("no body"), HttpStatus.BAD_REQUEST, "No request body");
    }

    @Test
    void handlesWebExchangeBindExceptionWithFieldErrors() {
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError(
                "request", "name", "", false, new String[] { "NotBlank" }, new Object[] {}, "must not be blank");
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));
        WebExchangeBindException exception = new WebExchangeBindException(mock(MethodParameter.class), bindingResult);

        ResponseEntity<Map<String, Object>> response = handler.handle(exception).block();

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).containsEntry("status", HttpStatus.BAD_REQUEST.value());
        assertThat(response.getBody()).containsEntry("title", "Validation Failed");
        assertThat(response.getBody()).containsEntry("detail", "name: must not be blank");
    }

    @Test
    void handlesWebExchangeBindExceptionWithoutDefaultMessage() {
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError(
                "request", "price", 0, true, new String[] { "Min" }, new Object[] { 1 }, null);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));
        WebExchangeBindException exception = new WebExchangeBindException(mock(MethodParameter.class), bindingResult);

        ResponseEntity<Map<String, Object>> response = handler.handle(exception).block();

        assertThat(response).isNotNull();
        assertThat((String) response.getBody().get("detail")).startsWith("price: ").contains("Field error in object");
    }

    @Test
    void handlesUnknownExceptionWithDefaultTemplate() {
        ResponseEntity<Map<String, Object>> response = handler.handle(new RuntimeException("unexpected")).block();

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).containsEntry("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        assertThat(response.getBody()).containsEntry("title", "Internal Server Error");
        assertThat(response.getBody()).containsEntry("detail", "unexpected");
    }

    @Test
    void handlesMediaServiceClientError() {
        ResponseEntity<Map<String, Object>> response = handler.handleMediaServiceException(
                new MediaServiceException(404, "not found", "body")).block();

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).containsEntry("status", HttpStatus.NOT_FOUND.value());
        assertThat(response.getBody()).containsEntry("title", "Media Service Client Error");
        assertThat(response.getBody()).containsEntry("detail", "not found → body");
        assertThat(response.getBody())
                .containsEntry("externalService", "media")
                .containsEntry("externalStatus", 404)
                .containsEntry("externalDetail", "body");
    }

    @Test
    void handlesMediaServiceServerError() {
        ResponseEntity<Map<String, Object>> response = handler.handleMediaServiceException(
                new MediaServiceException(503, "unavailable", "busy")).block();

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(response.getBody()).containsEntry("title", "Media Service Server Error");
    }

    @Test
    void handlesMediaServiceNonErrorStatus() {
        ResponseEntity<Map<String, Object>> response = handler.handleMediaServiceException(
                new MediaServiceException(200, "ok", null)).block();

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsEntry("status", HttpStatus.OK.value());
        assertThat(response.getBody()).containsEntry("title", "Media Service Unavailable");
        assertThat(response.getBody()).containsEntry("detail", "ok");
        assertThat(response.getBody()).doesNotContainKey("externalDetail");
    }

    @Test
    void handlesMediaServiceInvalidStatusAsInternalServerError() {
        ResponseEntity<Map<String, Object>> response = handler.handleMediaServiceException(
                new MediaServiceException(999, "invalid", "body")).block();

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).containsEntry("title", "Media Service Server Error");
    }

    @Test
    void handlesMediaServiceZeroStatusAsInternalServerError() {
        ResponseEntity<Map<String, Object>> response = handler.handleMediaServiceException(
                new MediaServiceException(0, "network", null)).block();

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).containsEntry("title", "Media Service Server Error");
    }

    @Test
    void handlesMediaServiceBlankResponseBody() {
        ResponseEntity<Map<String, Object>> response = handler.handleMediaServiceException(
                new MediaServiceException(400, "bad", "  ")).block();

        assertThat(response).isNotNull();
        assertThat(response.getBody()).containsEntry("detail", "bad");
    }

    private void assertProblem(Exception exception, HttpStatus status, String title) {
        ResponseEntity<Map<String, Object>> response = handler.handle(exception).block();

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(status);
        assertThat(response.getBody()).containsEntry("status", status.value());
        assertThat(response.getBody()).containsEntry("title", title);
    }
}
