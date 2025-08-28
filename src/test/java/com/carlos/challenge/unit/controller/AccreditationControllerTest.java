package com.carlos.challenge.unit.controller;

import com.carlos.challenge.controller.AccreditationController;
import com.carlos.challenge.dto.AccreditationResponse;
import com.carlos.challenge.service.AccreditationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AccreditationController.class)
@Import(AccreditationControllerTest.TestSecurityConfig.class)
class AccreditationControllerTest {

    @TestConfiguration
    @EnableMethodSecurity
    static class TestSecurityConfig {
        @Bean
        SecurityFilterChain testFilterChain(HttpSecurity http) throws Exception {
            return http
                    .csrf(csrf -> csrf.disable())
                    .authorizeHttpRequests(auth -> auth
                            .requestMatchers(POST, "/api/acreditaciones").authenticated()
                            .requestMatchers(GET,  "/api/acreditaciones/**").authenticated()
                            .anyRequest().denyAll()
                    )
                    .httpBasic(b -> {})
                    .build();
        }
    }

    @Autowired MockMvc mvc;

    @MockitoBean AccreditationService service;

    @Test
    @WithMockUser(roles = {"USER"})
    void create_ok() throws Exception {
        AccreditationResponse dto = new AccreditationResponse(
                "abc123", new BigDecimal("1234.56"), 1, "Sucursal Centro", Instant.now()
        );
        when(service.create(any())).thenReturn(dto);

        mvc.perform(post("/api/acreditaciones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {"importe":1234.56,"idPuntoVenta":1}
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idPuntoVenta").value(1))
                .andExpect(jsonPath("$.nombrePuntoVenta").value("Sucursal Centro"));
    }

    @Test
    void create_unauth_401() throws Exception {
        mvc.perform(post("/api/acreditaciones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {"importe":10,"idPuntoVenta":1}
                        """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void create_400_validacion() throws Exception {
        mvc.perform(post("/api/acreditaciones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {"importe":null,"idPuntoVenta":null}
                        """))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void list_ok_sin_filtros() throws Exception {
        AccreditationResponse dto = new AccreditationResponse(
                "abc", BigDecimal.TEN, 1, "Sucursal Centro", Instant.now()
        );
        Page<AccreditationResponse> page = new PageImpl<>(List.of(dto), PageRequest.of(0, 20), 1);

        when(service.list(eq(Optional.empty()), eq(Optional.empty()), eq(Optional.empty()), any()))
                .thenReturn(page);

        mvc.perform(get("/api/acreditaciones?page=0&size=20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].idPuntoVenta").value(1));
    }

    @Test
    void list_sin_auth_401() throws Exception {
        mvc.perform(get("/api/acreditaciones?page=0&size=5"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void list_filtrando_por_pv() throws Exception {
        Page<AccreditationResponse> page = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);

        when(service.list(eq(Optional.of(1)), eq(Optional.empty()), eq(Optional.empty()), any()))
                .thenReturn(page);

        mvc.perform(get("/api/acreditaciones?idPuntoVenta=1&page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void list_filtrando_por_fecha() throws Exception {
        Instant from = Instant.parse("2025-08-26T00:00:00Z");
        Instant to   = Instant.parse("2025-08-27T00:00:00Z");
        Page<AccreditationResponse> page = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);

        when(service.list(eq(Optional.empty()), eq(Optional.of(from)), eq(Optional.of(to)), any()))
                .thenReturn(page);

        mvc.perform(get("/api/acreditaciones?from=2025-08-26T00:00:00Z&to=2025-08-27T00:00:00Z&page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void list_pagina_fuera_de_rango_devuelve_vacio() throws Exception {
        Page<AccreditationResponse> emptyPage = new PageImpl<>(List.of(), PageRequest.of(9999, 5), 0);

        when(service.list(any(), any(), any(), any()))
                .thenReturn(emptyPage);

        mvc.perform(get("/api/acreditaciones?page=9999&size=5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty());
    }
}
