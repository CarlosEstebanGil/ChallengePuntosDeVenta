package com.carlos.challenge.unit.controller;

import com.carlos.challenge.controller.PointOfSaleController;
import com.carlos.challenge.model.PointOfSale;
import com.carlos.challenge.service.PointCacheService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import java.util.List;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpMethod.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.web.server.ResponseStatusException;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@WebMvcTest(PointOfSaleController.class)
@Import(PointOfSaleControllerTest.TestSecurityConfig.class)
class PointOfSaleControllerTest {

    @TestConfiguration
    @EnableMethodSecurity
    static class TestSecurityConfig {
        @Bean
        SecurityFilterChain testFilterChain(HttpSecurity http) throws Exception {
            return http
                    .csrf(csrf -> csrf.disable())
                    .authorizeHttpRequests(auth -> auth
                            .requestMatchers(POST,   "/api/pos/**").hasRole("ADMIN")
                            .requestMatchers(PUT,    "/api/pos/**").hasRole("ADMIN")
                            .requestMatchers(DELETE, "/api/pos/**").hasRole("ADMIN")
                            .requestMatchers(GET,    "/api/pos/**").authenticated()
                            .anyRequest().denyAll()
                    )
                    .httpBasic(b -> {})
                    .build();
        }
    }

    @Autowired
    MockMvc mvc;

    @MockitoBean
    PointCacheService pointCache;

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void list_ok() throws Exception {
        when(pointCache.findAll()).thenReturn(List.of(
                new PointOfSale(1, "Sucursal Centro"),
                new PointOfSale(2, "Sucursal Norte")
        ));

        mvc.perform(get("/api/pos"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Sucursal Centro"));
    }

    @Test
    void list_sin_auth_401() throws Exception {
        mvc.perform(get("/api/pos"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void findById_ok() throws Exception {
        when(pointCache.findById(1)).thenReturn(new PointOfSale(1, "Sucursal Centro"));

        mvc.perform(get("/api/pos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Sucursal Centro"));
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void findById_404() throws Exception {
        when(pointCache.findById(anyInt()))
                .thenThrow(new ResponseStatusException(NOT_FOUND, "Not found"));

        mvc.perform(get("/api/pos/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void create_ok() throws Exception {
        when(pointCache.create(1, "Sucursal Centro"))
                .thenReturn(new PointOfSale(1, "Sucursal Centro"));

        mvc.perform(post("/api/pos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"id":1,"name":"Sucursal Centro"}
                                """))
                .andExpect(status().isCreated())        // o isOk() según tu controller
                .andExpect(header().string("Location", "/api/pos/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Sucursal Centro"));
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void create_con_user_403() throws Exception {

        mvc.perform(post("/api/pos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                             {"id":2,"name":"PV2"}
                         """))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void create_400_validacion() throws Exception {

        mvc.perform(post("/api/pos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
            {"id":null,"name":""}
            """))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void update_ok() throws Exception {
        when(pointCache.update(1, "Nuevo name"))
                .thenReturn(new PointOfSale(1, "Nuevo name"));

        mvc.perform(put("/api/pos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Nuevo name"}"""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Nuevo name"));
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void update_con_user_403() throws Exception {
        mvc.perform(put("/api/pos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"X"}"""))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void update_404() throws Exception {
        when(pointCache.update(anyInt(), any()))
                .thenThrow(new ResponseStatusException(NOT_FOUND, "Not found"));

        mvc.perform(put("/api/pos/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Pepito"}"""))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void delete_204() throws Exception {
        doNothing().when(pointCache).delete(1);

        mvc.perform(delete("/api/pos/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void delete_con_user_403() throws Exception {
        mvc.perform(delete("/api/pos/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void delete_404() throws Exception {
        doThrow(new ResponseStatusException(NOT_FOUND, "Not found"))
                .when(pointCache).delete(999);

        mvc.perform(delete("/api/pos/999"))
                .andExpect(status().isNotFound());
    }
}