package com.carlos.challenge.web;

import com.carlos.challenge.config.TestProfiles;
import com.carlos.challenge.config.TestSecurityConfig;
import com.carlos.challenge.domain.model.Accreditation;
import com.carlos.challenge.domain.port.in.AccreditationUseCasePort;
import com.carlos.challenge.infrastructure.in.web.controller.AccreditationController;
import com.carlos.challenge.infrastructure.in.web.dto.resp.AccreditationResponse;
import com.carlos.challenge.infrastructure.in.web.mapper.AccreditationWebMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AccreditationController.class)
@ActiveProfiles(TestProfiles.TEST)
@Import({TestSecurityConfig.class, AccreditationControllerWebTest.MockConfig.class})
class AccreditationControllerWebTest {

    @TestConfiguration
    static class MockConfig {
        @Bean
        AccreditationWebMapper accreditationWebMapper() {
            return Mockito.mock(AccreditationWebMapper.class);
        }

        @Bean
        com.carlos.challenge.infrastructure.config.SecurityUsersProperties securityUsersProperties() {
            return Mockito.mock(com.carlos.challenge.infrastructure.config.SecurityUsersProperties.class);
        }
    }

    @Autowired MockMvc mvc;
    @Autowired AccreditationWebMapper accreditationWebMapper;
    @MockitoBean AccreditationUseCasePort usecase;

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void create_requiresAdmin_andValidates() throws Exception {
        String bodyJson = "{\"amount\":10.00,\"pointOfSaleId\":\"POS1\"}";
        Accreditation created = new Accreditation("A1", new BigDecimal("10.00"), "POS1", "Point 1", Instant.parse("2024-01-01T00:00:00Z"));
        AccreditationResponse response = new AccreditationResponse("A1", new BigDecimal("10.00"), "POS1", "Point 1", Instant.parse("2024-01-01T00:00:00Z"));

        when(usecase.create(any(), any())).thenReturn(created);
        when(accreditationWebMapper.toResponse(any(Accreditation.class))).thenReturn(response);

        mvc.perform(post("/api/accreditations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyJson))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").value("A1"))
                .andExpect(jsonPath("$.pointOfSaleId").value("POS1"))
                .andExpect(jsonPath("$.amount").value(10.00));
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void create_forbiddenForUser() throws Exception {
        String bodyJson = "{\"amount\":10.00,\"pointOfSaleId\":\"POS1\"}";
        mvc.perform(post("/api/accreditations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyJson))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void list_allowsUserOrAdmin_andPaginates() throws Exception {
        Accreditation acc = new Accreditation("A1", new BigDecimal("11.00"), "P1", "Point 1", Instant.now());
        AccreditationResponse response = new AccreditationResponse("A1", new BigDecimal("11.00"), "P1", "Point 1", acc.receptionDate());
        Page<Accreditation> page = new PageImpl<>(List.of(acc), PageRequest.of(0, 20), 1);

        when(usecase.findAll(any())).thenReturn(page);
        when(accreditationWebMapper.toResponse(any(Accreditation.class))).thenReturn(response);

        mvc.perform(get("/api/accreditations")
                        .param("page", "0").param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value("A1"))
                .andExpect(jsonPath("$.pageable.pageNumber").value(0))
                .andExpect(jsonPath("$.totalElements").value(1));
    }
}
