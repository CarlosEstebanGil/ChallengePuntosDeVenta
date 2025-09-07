package com.carlos.challenge.unit.controller;

import com.carlos.challenge.controller.CostController;
import com.carlos.challenge.dto.EdgeRequest;
import com.carlos.challenge.dto.MinPathsResponse;
import com.carlos.challenge.dto.NeighborResponse;
import com.carlos.challenge.dto.PathDetail;
import com.carlos.challenge.service.GraphService;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
                            .requestMatchers(POST,   "/api/graph/costs").hasRole("ADMIN")
                            .requestMatchers(DELETE, "/api/graph/costs").hasRole("ADMIN")
                            .requestMatchers(GET,    "/api/graph/costs/**").authenticated()
                            .anyRequest().denyAll()
                    )
                    .httpBasic(b -> {})
                    .build();
        }
    }

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper objectMapper;

    @MockitoBean GraphService graph;

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void upsert_ok_admin_204() throws Exception {
        String from = "11111111-1111-1111-1111-111111111111";
        String to   = "22222222-2222-2222-2222-222222222222";
        int cost    = 5;

        doNothing().when(graph).upsertEdge(from, to, cost);

        String body = objectMapper.writeValueAsString(new EdgeRequest(from, to, cost));

        mvc.perform(post("/api/graph/costs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void upsert_user_403() throws Exception {
        String body = objectMapper.writeValueAsString(
                new EdgeRequest("11111111-1111-1111-1111-111111111111",
                        "22222222-2222-2222-2222-222222222222",
                        5)
        );

        mvc.perform(post("/api/graph/costs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void upsert_400_validacion() throws Exception {

        String body = objectMapper.writeValueAsString(
                new EdgeRequest("11111111-1111-1111-1111-111111111111",
                        "22222222-2222-2222-2222-222222222222",
                        -1)
        );

        mvc.perform(post("/api/graph/costs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }


    @Test
    @WithMockUser(roles = {"ADMIN"})
    void delete_ok_admin_204() throws Exception {
        String from = "11111111-1111-1111-1111-111111111111";
        String to   = "22222222-2222-2222-2222-222222222222";

        doNothing().when(graph).removeEdge(from, to);

        String body = objectMapper.writeValueAsString(new EdgeRequest(from, to, 0));

        mvc.perform(delete("/api/graph/costs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void delete_user_403() throws Exception {
        String body = objectMapper.writeValueAsString(
                new EdgeRequest("11111111-1111-1111-1111-111111111111",
                        "22222222-2222-2222-2222-222222222222",
                        0)
        );

        mvc.perform(delete("/api/graph/costs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());
    }


    @Test
    @WithMockUser(roles = {"USER"})
    void neighbors_ok() throws Exception {
        String from = "11111111-1111-1111-1111-111111111111";
        when(graph.neighborsOf(from)).thenReturn(List.of(
                new NeighborResponse("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa", "Sucursal Norte", 5),
                new NeighborResponse("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb", "Sucursal Este", 7)
        ));

        mvc.perform(get("/api/graph/costs/neighbors/{fromId}", from))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"))
                .andExpect(jsonPath("$[0].name").value("Sucursal Norte"))
                .andExpect(jsonPath("$[0].cost").value(5))
                .andExpect(jsonPath("$[1].id").value("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb"))
                .andExpect(jsonPath("$[1].name").value("Sucursal Este"))
                .andExpect(jsonPath("$[1].cost").value(7));
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void neighbors_404_si_pv_inexistente() throws Exception {
        when(graph.neighborsOf(anyString()))
                .thenThrow(new ResponseStatusException(NOT_FOUND, "No existe el punto"));

        mvc.perform(get("/api/graph/costs/neighbors/{fromId}", "eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee"))
                .andExpect(status().isNotFound());
    }

    @Test
    void neighbors_unauth_401() throws Exception {
        mvc.perform(get("/api/graph/costs/neighbors/{fromId}", "11111111-1111-1111-1111-111111111111"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void minPaths_ok() throws Exception {
        String from = "11111111-1111-1111-1111-111111111111";
        String to   = "44444444-4444-4444-4444-444444444444";

        MinPathsResponse resp = new MinPathsResponse(
                12,
                List.of(
                        new PathDetail(
                                List.of(from, "22222222-2222-2222-2222-222222222222", to),
                                List.of("Sucursal Centro", "Sucursal Norte", "Sucursal Sur"),
                                List.of(1, 2, 4)
                        ),
                        new PathDetail(
                                List.of(from, "33333333-3333-3333-3333-333333333333", to),
                                List.of("Sucursal Centro", "Sucursal Este", "Sucursal Sur"),
                                List.of(1, 3, 4)
                        )
                )
        );
        when(graph.shortestPaths(from, to)).thenReturn(resp);

        mvc.perform(get("/api/graph/costs/min-paths")
                        .param("from", from)
                        .param("to", to))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalCost").value(12))
                .andExpect(jsonPath("$.paths[0].routeIds[1]").value("22222222-2222-2222-2222-222222222222"))
                .andExpect(jsonPath("$.paths[0].routeCodes[2]").value(4))
                .andExpect(jsonPath("$.paths[1].routeNames[2]").value("Sucursal Sur"));
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void minPaths_404_si_no_hay_camino() throws Exception {
        when(graph.shortestPaths(anyString(), anyString()))
                .thenThrow(new ResponseStatusException(NOT_FOUND, "No path found"));

        mvc.perform(get("/api/graph/costs/min-paths")
                        .param("from", "11111111-1111-1111-1111-111111111111")
                        .param("to",   "99999999-9999-9999-9999-999999999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void minPaths_unauth_401() throws Exception {
        mvc.perform(get("/api/graph/costs/min-paths")
                        .param("from", "11111111-1111-1111-1111-111111111111")
                        .param("to",   "44444444-4444-4444-4444-444444444444"))
                .andExpect(status().isUnauthorized());
    }
}
