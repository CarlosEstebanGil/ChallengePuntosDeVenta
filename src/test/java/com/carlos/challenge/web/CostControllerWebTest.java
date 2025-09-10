package com.carlos.challenge.web;

import com.carlos.challenge.config.TestProfiles;
import com.carlos.challenge.config.TestSecurityConfig;
import com.carlos.challenge.domain.model.PointOfSale;
import com.carlos.challenge.domain.model.graph.MinPaths;
import com.carlos.challenge.domain.model.graph.Neighbor;
import com.carlos.challenge.domain.port.in.GraphUseCasePort;
import com.carlos.challenge.domain.port.in.PointOfSaleUseCasePort;
import com.carlos.challenge.infrastructure.config.SecurityUsersProperties;
import com.carlos.challenge.infrastructure.in.web.controller.CostController;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.boot.test.mock.mockito.MockBean;
@WebMvcTest(controllers = CostController.class)
@ActiveProfiles(TestProfiles.TEST)
@Import({TestSecurityConfig.class, CostControllerWebTest.MockConfig.class})
class CostControllerWebTest {

    @TestConfiguration
    static class MockConfig {
        @Bean
        SecurityUsersProperties securityUsersProperties() {
            return Mockito.mock(SecurityUsersProperties.class);
        }
    }

    @Autowired MockMvc mvc;

    @MockBean GraphUseCasePort graph;
    @MockBean PointOfSaleUseCasePort posUseCase;

    static final UUID UUID_A = UUID.fromString("11111111-1111-1111-1111-111111111111");
    static final UUID UUID_B = UUID.fromString("22222222-2222-2222-2222-222222222222");
    static final UUID UUID_C = UUID.fromString("33333333-3333-3333-3333-333333333333");

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void upsertEdge_requiresAdmin() throws Exception {
        mvc.perform(post("/api/graph/costs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"from\":\"" + UUID_A + "\",\"to\":\"" + UUID_B + "\",\"cost\":7}"))
                .andExpect(status().isNoContent());
        verify(graph).upsertEdge(UUID_A.toString(), UUID_B.toString(), 7);
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void neighbors_allowsUserOrAdmin() throws Exception {
        when(graph.neighborsOf(UUID_A.toString())).thenReturn(List.of(
                new Neighbor(UUID_B.toString(), 5),
                new Neighbor(UUID_C.toString(), 3)
        ));

        when(posUseCase.findById(UUID_B.toString())).thenReturn(new PointOfSale(UUID_B.toString(), "POS B", 101));
        when(posUseCase.findById(UUID_C.toString())).thenReturn(new PointOfSale(UUID_C.toString(), "POS C", 102));

        mvc.perform(get("/api/graph/costs/neighbors/" + UUID_A))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(UUID_B.toString()))
                .andExpect(jsonPath("$[0].cost").value(5))
                .andExpect(jsonPath("$[0].name").value("POS B"))
                .andExpect(jsonPath("$[1].id").value(UUID_C.toString()))
                .andExpect(jsonPath("$[1].cost").value(3))
                .andExpect(jsonPath("$[1].name").value("POS C"));
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void minPaths_contract() throws Exception {
        MinPaths mp = new MinPaths(2, List.of(List.of(UUID_A.toString(), UUID_B.toString(), UUID_C.toString())));
        when(graph.shortestPaths(UUID_A.toString(), UUID_C.toString())).thenReturn(mp);

        mvc.perform(get("/api/graph/costs/min-paths")
                        .param("from", UUID_A.toString()).param("to", UUID_C.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCost").value(2))
                .andExpect(jsonPath("$.paths[0].pointIds[2]").value(UUID_C.toString()));
    }
}
