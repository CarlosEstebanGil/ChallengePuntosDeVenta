package com.carlos.challenge.domain;

import com.carlos.challenge.domain.model.Accreditation;
import com.carlos.challenge.domain.port.out.AccreditationRepositoryPort;
import com.carlos.challenge.infrastructure.in.web.service.AccreditationUseCaseService;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class AccreditationUseCaseServiceTest {

    @Test
    void create_delegatesToRepository() {
        AccreditationRepositoryPort repo = mock(AccreditationRepositoryPort.class);
        BigDecimal amount = new BigDecimal("30.50");
        String pointOfSaleId = "POS1";
        Accreditation acc = new Accreditation(
                null, amount, pointOfSaleId, null, Instant.now()
        );
        when(repo.save(any())).thenReturn(acc);

        AccreditationUseCaseService svc = new AccreditationUseCaseService(repo);
        Accreditation out = svc.create(amount, pointOfSaleId);

        assertThat(out).isSameAs(acc);
        verify(repo).save(any(Accreditation.class));
    }

    @Test
    void list_filtersAndPaginates() {
        AccreditationRepositoryPort repo = mock(AccreditationRepositoryPort.class);
        Page<Accreditation> page = new PageImpl<>(List.of(
                new Accreditation("a1", new BigDecimal("1.00"), "P1","Point 1", Instant.now())
        ), PageRequest.of(0, 20), 1);

        when(repo.findAll(any())).thenReturn(page);

        AccreditationUseCaseService svc = new AccreditationUseCaseService(repo);
        Page<Accreditation> out = svc.findAll(PageRequest.of(0,20));

        assertThat(out.getTotalElements()).isEqualTo(1);
        verify(repo).findAll(PageRequest.of(0,20));
    }
}
