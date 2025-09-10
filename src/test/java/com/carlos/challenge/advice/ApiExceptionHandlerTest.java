package com.carlos.challenge.advice;

import com.carlos.challenge.config.TestProfiles;
import com.carlos.challenge.infrastructure.in.web.advice.ApiExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import jakarta.servlet.http.HttpServletRequest;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ActiveProfiles(TestProfiles.TEST)
class ApiExceptionHandlerTest {

    ApiExceptionHandler advice = new ApiExceptionHandler();

    @Test
    void notFound_translatesTo404() {
        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getRequestURI()).thenReturn("/x");
        ResponseEntity<?> r = advice.handleNotFound(new java.util.NoSuchElementException("nope"), req);
        assertThat(r.getStatusCode().value()).isEqualTo(404);
    }

    @Test
    void illegalArgument_translatesTo404() {
        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getRequestURI()).thenReturn("/x");
        ResponseEntity<?> r = advice.handleNotFound(new IllegalArgumentException("bad"), req);
        assertThat(r.getStatusCode().value()).isEqualTo(404);
    }
}
