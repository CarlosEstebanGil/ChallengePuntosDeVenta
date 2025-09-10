package com.carlos.challenge.web;

import com.carlos.challenge.config.TestProfiles;
import com.carlos.challenge.config.TestSecurityConfig;
import com.carlos.challenge.domain.model.PointOfSale;
import com.carlos.challenge.domain.port.in.PointOfSaleUseCasePort;
import com.carlos.challenge.infrastructure.config.SecurityUsersProperties;
import com.carlos.challenge.infrastructure.in.web.controller.PointOfSaleController;
import com.carlos.challenge.infrastructure.in.web.dto.resp.PointOfSaleResponse;
import com.carlos.challenge.infrastructure.in.web.mapper.PointOfSaleWebMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = PointOfSaleController.class)
@ActiveProfiles(TestProfiles.TEST)
@Import(TestSecurityConfig.class)
class PointOfSaleControllerWebTest {

    @Autowired MockMvc mvc;

    @MockitoBean
    PointOfSaleUseCasePort usecase;

    @MockitoBean
    PointOfSaleWebMapper mapper;

    @Test
    @WithMockUser(roles = {"USER"})
    void list_allowsUserOrAdmin() throws Exception {
        PointOfSale pos = new PointOfSale("1", "Alpha", 1001);
        PointOfSaleResponse resp = new PointOfSaleResponse("1", "Alpha", 1001);

        when(usecase.findAll()).thenReturn(List.of(pos));
        when(mapper.toResponse(pos)).thenReturn(resp);

        mvc.perform(get("/api/pointsofsale"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("1"))
                .andExpect(jsonPath("$[0].name").value("Alpha"))
                .andExpect(jsonPath("$[0].code").value(1001));
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void getById_allowsUserOrAdmin() throws Exception {
        PointOfSale pos = new PointOfSale("1", "Alpha", 1001);
        PointOfSaleResponse resp = new PointOfSaleResponse("1", "Alpha", 1001);

        when(usecase.findById("1")).thenReturn(pos);
        when(mapper.toResponse(pos)).thenReturn(resp);

        mvc.perform(get("/api/pointsofsale/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.name").value("Alpha"))
                .andExpect(jsonPath("$.code").value(1001));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void create_requiresAdmin_andValidates() throws Exception {
        String bodyJson = "{\"name\":\"Alpha\",\"code\":1001}";
        PointOfSale pos = new PointOfSale("1", "Alpha", 1001);
        PointOfSaleResponse resp = new PointOfSaleResponse("1", "Alpha", 1001);

        when(usecase.create(eq("Alpha"), eq(1001))).thenReturn(pos);
        when(mapper.toResponse(pos)).thenReturn(resp);

        mvc.perform(post("/api/pointsofsale")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyJson))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/pointsofsale/1"))
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.name").value("Alpha"))
                .andExpect(jsonPath("$.code").value(1001));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void update_requiresAdmin() throws Exception {
        String bodyJson = "{\"name\":\"Alpha 2\"}";
        PointOfSale pos = new PointOfSale("1", "Alpha 2", 1001);
        PointOfSaleResponse resp = new PointOfSaleResponse("1", "Alpha 2", 1001);

        when(usecase.update(eq("1"), eq("Alpha 2"))).thenReturn(pos);
        when(mapper.toResponse(pos)).thenReturn(resp);

        mvc.perform(put("/api/pointsofsale/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.name").value("Alpha 2"))
                .andExpect(jsonPath("$.code").value(1001));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void delete_requiresAdmin() throws Exception {
        mvc.perform(delete("/api/pointsofsale/1"))
                .andExpect(status().isNoContent());

        verify(usecase).delete("1");
    }

    @TestConfiguration
    static class TestPropsConfig {
        @Bean
        public SecurityUsersProperties securityUsersProperties() {
            return new SecurityUsersProperties();
        }
    }
}
