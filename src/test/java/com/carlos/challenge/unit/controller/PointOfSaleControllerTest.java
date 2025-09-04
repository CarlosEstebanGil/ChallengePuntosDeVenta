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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import java.util.List;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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
                            .requestMatchers(POST,   "/api/pointsofsale/**").hasRole("ADMIN")
                            .requestMatchers(PUT,    "/api/pointsofsale/**").hasRole("ADMIN")
                            .requestMatchers(DELETE, "/api/pointsofsale/**").hasRole("ADMIN")
                            .requestMatchers(GET,    "/api/pointsofsale/**").authenticated()
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
                new PointOfSale("pos-1", "Sucursal Centro", 1),
                new PointOfSale("pos-2", "Sucursal Norte", 2)
        ));

        mvc.perform(get("/api/pointsofsale"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value("pos-1"))
                .andExpect(jsonPath("$[0].name").value("Sucursal Centro"))
                .andExpect(jsonPath("$[0].code").value(1)) // <-- elimina esta línea si no existe 'code'
                .andExpect(jsonPath("$[1].id").value("pos-2"))
                .andExpect(jsonPath("$[1].name").value("Sucursal Norte"))
                .andExpect(jsonPath("$[1].code").value(2)); // <-- elimina esta línea si no existe 'code'
    }

    @Test
    void list_sin_auth_401() throws Exception {
        mvc.perform(get("/api/pointsofsale"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void findById_ok() throws Exception {
        when(pointCache.findById("pos-1"))
                .thenReturn(new PointOfSale("pos-1", "Sucursal Centro", 1));

        mvc.perform(get("/api/pointsofsale/{id}", "pos-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("pos-1"))
                .andExpect(jsonPath("$.name").value("Sucursal Centro"))
                .andExpect(jsonPath("$.code").value(1)); // <-- elimina si no existe 'code'
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void findById_404() throws Exception {
        when(pointCache.findById(anyString()))
                .thenThrow(new ResponseStatusException(NOT_FOUND, "Not found"));

        mvc.perform(get("/api/pointsofsale/{id}", "no-such-id"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void create_ok_sin_code_auto() throws Exception {
        when(pointCache.create("Sucursal Centro"))
                .thenReturn(new PointOfSale("pos-1", "Sucursal Centro", 101));

        mvc.perform(post("/api/pointsofsale")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {"name":"Sucursal Centro"}
                    """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/pointsofsale/pos-1"))
                .andExpect(jsonPath("$.id").value("pos-1"))
                .andExpect(jsonPath("$.name").value("Sucursal Centro"))
                .andExpect(jsonPath("$.code").value(101)); // <-- elimina si no existe 'code'
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void create_ok_con_code_explicito() throws Exception {
        when(pointCache.create("Sucursal Norte", 202))
                .thenReturn(new PointOfSale("pos-2", "Sucursal Norte", 202));

        mvc.perform(post("/api/pointsofsale")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {"name":"Sucursal Norte","code":202}
                    """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/pointsofsale/pos-2"))
                .andExpect(jsonPath("$.id").value("pos-2"))
                .andExpect(jsonPath("$.name").value("Sucursal Norte"))
                .andExpect(jsonPath("$.code").value(202)); // <-- elimina si no existe 'code'
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void create_con_user_403() throws Exception {
        mvc.perform(post("/api/pointsofsale")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {"name":"PV2"}
                    """))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void create_400_validacion() throws Exception {
        mvc.perform(post("/api/pointsofsale")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {"name":""}
                    """))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void update_ok() throws Exception {
        when(pointCache.update("pos-1", "Nuevo name"))
                .thenReturn(new PointOfSale("pos-1", "Nuevo name", 1));

        mvc.perform(put("/api/pointsofsale/{id}", "pos-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {"name":"Nuevo name"}
                    """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("pos-1"))
                .andExpect(jsonPath("$.name").value("Nuevo name"))
                .andExpect(jsonPath("$.code").value(1)); // <-- elimina si no existe 'code'
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void update_con_user_403() throws Exception {
        mvc.perform(put("/api/pointsofsale/{id}", "pos-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {"name":"X"}
                    """))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void update_404() throws Exception {
        when(pointCache.update(anyString(), any()))
                .thenThrow(new ResponseStatusException(NOT_FOUND, "Not found"));

        mvc.perform(put("/api/pointsofsale/{id}", "no-such-id")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {"name":"Pepito"}
                    """))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void delete_204() throws Exception {
        doNothing().when(pointCache).delete("pos-1");

        mvc.perform(delete("/api/pointsofsale/{id}", "pos-1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void delete_con_user_403() throws Exception {
        mvc.perform(delete("/api/pointsofsale/{id}", "pos-1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void delete_404() throws Exception {
        doThrow(new ResponseStatusException(NOT_FOUND, "Not found"))
                .when(pointCache).delete("no-such-id");

        mvc.perform(delete("/api/pointsofsale/{id}", "no-such-id"))
                .andExpect(status().isNotFound());
    }
}
