package com.carlos.challenge.unit.controller;

import com.carlos.challenge.controller.CostController;
import com.carlos.challenge.dto.MinPathResponse;
import com.carlos.challenge.dto.NeighborResponse;
import com.carlos.challenge.service.GraphService;
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
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpMethod.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.web.server.ResponseStatusException;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@WebMvcTest(CostController.class)
@Import(CostControllerTest.TestSecurityConfig.class)
class CostControllerTest {

    @TestConfiguration
    @EnableMethodSecurity
    static class TestSecurityConfig {
        @Bean
        SecurityFilterChain testFilterChain(HttpSecurity http) throws Exception {
            return http
                    .csrf(csrf -> csrf.disable())
                    .authorizeHttpRequests(auth -> auth
                            .requestMatchers(POST,   "/api/costs").hasRole("ADMIN")
                            .requestMatchers(DELETE, "/api/costs").hasRole("ADMIN")
                            .requestMatchers(GET,    "/api/costs/**").authenticated()
                            .anyRequest().denyAll()
                    )
                    .httpBasic(b -> {})
                    .build();
        }
    }

    @Autowired
    MockMvc mvc;

    @MockitoBean
    GraphService graph;

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void upsert_ok_admin_204() throws Exception {
        doNothing().when(graph).upsertEdge(1, 2, 5);

        mvc.perform(post("/api/costs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "fromId": 1,
                          "toId":   2,
                          "cost":  5
                        }
                        """))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void upsert_user_403() throws Exception {
        mvc.perform(post("/api/costs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "fromId": 1,
                          "toId":   2,
                          "cost":  5
                        }
                        """))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void upsert_400_validacion() throws Exception {

        mvc.perform(post("/api/costs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "fromId": null,
                          "toId":   null,
                          "cost":  -1
                        }
                        """))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void delete_ok_admin_204() throws Exception {
        doNothing().when(graph).removeEdge(1, 2);

        mvc.perform(delete("/api/costs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "fromId": 1,
                          "toId":   2,
                          "cost":  0
                        }
                        """))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void delete_user_403() throws Exception {
        mvc.perform(delete("/api/costs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "fromId": 1,
                          "toId":   2,
                          "cost":  0
                        }
                        """))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void neighbors_ok() throws Exception {
        when(graph.neighborsOf(1)).thenReturn(List.of(
                new NeighborResponse(2, "Sucursal Norte", 5),
                new NeighborResponse(3, "Sucursal Este", 7)
        ));

        mvc.perform(get("/api/costs/neighbors/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(2))
                .andExpect(jsonPath("$[0].name").value("Sucursal Norte"))
                .andExpect(jsonPath("$[0].cost").value(5));
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void neighbors_404_si_pv_inexistente() throws Exception {
        when(graph.neighborsOf(anyInt()))
                .thenThrow(new ResponseStatusException(NOT_FOUND, "No existe el punto"));
        mvc.perform(get("/api/costs/neighbors/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void neighbors_unauth_401() throws Exception {
        mvc.perform(get("/api/costs/neighbors/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void minPath_ok() throws Exception {
        MinPathResponse resp = new MinPathResponse(
                12,
                List.of(1, 3, 4),
                List.of("Sucursal Centro", "Sucursal Este", "Sucursal Sur")
        );
        when(graph.shortestPath(1, 4)).thenReturn(resp);

        mvc.perform(get("/api/costs/min-path?from=1&to=4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCost").value(12))
                .andExpect(jsonPath("$.routeIds[0]").value(1))
                .andExpect(jsonPath("$.routeNames[2]").value("Sucursal Sur"));
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void minPath_404_si_no_hay_camino() throws Exception {
        when(graph.neighborsOf(anyInt()))
                .thenThrow(new ResponseStatusException(NOT_FOUND, "No existe el punto"));
        mvc.perform(get("/api/costs/min-path?from=1&to=99"))
                .andExpect(status().isNotFound());
    }
}