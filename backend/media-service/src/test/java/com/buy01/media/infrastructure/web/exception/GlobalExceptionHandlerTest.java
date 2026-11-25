package com.buy01.media.infrastructure.web.exception;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ResponseStatusException;

import com.buy01.media.infrastructure.web.exception.GlobalExceptionHandler.ProblemDetailResponse;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handlesResponseStatusException() {
        ResponseEntity<ProblemDetailResponse> response =
                handler.handleResponseStatusException(new ResponseStatusException(BAD_REQUEST, "bad"));

        assertThat(response.getStatusCode()).isEqualTo(BAD_REQUEST);
        assertThat(response.getBody().getStatus()).isEqualTo(400);
        assertThat(response.getBody().getTitle()).isEqualTo("Application Error");
        assertThat(response.getBody().getDetail()).isEqualTo("bad");
    }

    @Test
    void handlesKnownMappedException() {
        ResponseEntity<ProblemDetailResponse> response = handler.handle(new Errors.NotFound("missing"));

        assertThat(response.getStatusCode()).isEqualTo(NOT_FOUND);
        assertThat(response.getBody().getStatus()).isEqualTo(404);
        assertThat(response.getBody().getTitle()).isEqualTo("Not Found");
        assertThat(response.getBody().getDetail()).isEqualTo("missing");
    }

    @Test
    void handlesValidationExceptionWithFieldCount() {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Form(), "target");
        bindingResult.rejectValue("name", "required");
        WebExchangeBindException ex = new WebExchangeBindException(null, bindingResult);

        ResponseEntity<ProblemDetailResponse> response = handler.handle(ex);

        assertThat(response.getStatusCode()).isEqualTo(BAD_REQUEST);
        assertThat(response.getBody().getTitle()).isEqualTo("Validation Failed");
        assertThat(response.getBody().getDetail()).isEqualTo("Fields: 1");
    }

    @Test
    void handlesUnknownExceptionWithDefaultTemplate() {
        ResponseEntity<ProblemDetailResponse> response = handler.handle(new RuntimeException("boom"));

        assertThat(response.getStatusCode()).isEqualTo(INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().getStatus()).isEqualTo(500);
        assertThat(response.getBody().getTitle()).isEqualTo("Internal Server Error");
        assertThat(response.getBody().getDetail()).isEqualTo("boom");
    }

    @Test
    void problemTemplateExposesItsValues() {
        ProblemTemplate template = new ProblemTemplate(BAD_REQUEST, Throwable::getMessage, "Title");

        assertThat(template.status()).isEqualTo(BAD_REQUEST);
        assertThat(template.title()).isEqualTo("Title");
        assertThat(template.detailGenerator().apply(new RuntimeException("detail"))).isEqualTo("detail");
    }

    static class Form {
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    @Test
    void registrySupportsBothAddOverloadsAndBuild() {
        ProblemTemplate.Registry registry = new ProblemTemplate.Registry()
                .add(IllegalArgumentException.class, BAD_REQUEST, "Simple")
                .add(IllegalStateException.class, BAD_REQUEST, "Custom", ex -> "custom:" + ex.getMessage());

        Map<Class<? extends Throwable>, ProblemTemplate> map = registry.build();

        assertThat(map).hasSize(2);
        assertThat(map.get(IllegalArgumentException.class).detailGenerator()
                .apply(new IllegalArgumentException("x"))).isEqualTo("x");
        assertThat(map.get(IllegalStateException.class).detailGenerator()
                .apply(new IllegalStateException("y"))).isEqualTo("custom:y");
    }
}
