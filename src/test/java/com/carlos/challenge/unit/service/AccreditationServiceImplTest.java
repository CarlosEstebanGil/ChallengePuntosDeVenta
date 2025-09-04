package com.carlos.challenge.unit.service;

import com.carlos.challenge.dto.CreateAccreditationRequest;
import com.carlos.challenge.model.Accreditation;
import com.carlos.challenge.model.PointOfSale;
import com.carlos.challenge.repository.AccreditationRepository;
import com.carlos.challenge.service.PointCacheService;
import com.carlos.challenge.service.impl.AccreditationServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccreditationServiceImplTest {

    @Mock
    AccreditationRepository repo;

    @Mock
    PointCacheService pointCache;

    @InjectMocks
    AccreditationServiceImpl service;

    @Test
    void create_ok() {
        PointOfSale pv = new PointOfSale("pos-1", "Sucursal Centro", 1);
        when(pointCache.findById("pos-1")).thenReturn(pv);
        when(repo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var req = new CreateAccreditationRequest(BigDecimal.TEN, "pos-1");
        var resp = service.create(req);

        assertEquals("pos-1", resp.pointOfSaleId());
        assertEquals("Sucursal Centro", resp.pointOfSaleName());
        assertNotNull(resp.receptionDate());
    }

    @Test
    void list_bothPresent() {
        Instant from = Instant.now().minusSeconds(1000);
        Instant to = Instant.now();
        Accreditation acc = new Accreditation(BigDecimal.ONE, "pos-1", from, "PV1");
        acc.setId("10");
        Page<Accreditation> page = new PageImpl<>(List.of(acc));

        when(repo.findByPointOfSaleIdAndReceptionDateBetween(eq("pos-1"), eq(from), eq(to), any(Pageable.class)))
                .thenReturn(page);

        var result = service.list(Optional.of("pos-1"), Optional.of(from), Optional.of(to), Pageable.unpaged());

        assertEquals(1, result.getTotalElements());
        var resp = result.getContent().getFirst();
        assertEquals("10", resp.id());
        assertEquals("PV1", resp.pointOfSaleName());
    }

    @Test
    void list_onlyPointOfSaleId() {
        Accreditation acc = new Accreditation(BigDecimal.ONE, "pos-2", Instant.now(), "PV2");
        acc.setId("20");
        Page<Accreditation> page = new PageImpl<>(List.of(acc));

        when(repo.findByPointOfSaleId(eq("pos-2"), any(Pageable.class))).thenReturn(page);

        var result = service.list(Optional.of("pos-2"), Optional.empty(), Optional.empty(), Pageable.unpaged());

        assertEquals(1, result.getTotalElements());
        assertEquals("20", result.getContent().getFirst().id());
    }

    @Test
    void list_onlyRange() {
        Instant from = Instant.now().minusSeconds(2000);
        Instant to = Instant.now();
        Accreditation acc = new Accreditation(BigDecimal.ONE, "pos-3", from, "PV3");
        acc.setId("30");
        Page<Accreditation> page = new PageImpl<>(List.of(acc));

        when(repo.findByReceptionDateBetween(eq(from), eq(to), any(Pageable.class))).thenReturn(page);

        var result = service.list(Optional.empty(), Optional.of(from), Optional.of(to), Pageable.unpaged());

        assertEquals(1, result.getTotalElements());
        assertEquals("30", result.getContent().getFirst().id());
    }

    @Test
    void list_nonePresent() {
        Accreditation acc = new Accreditation(BigDecimal.ONE, "pos-4", Instant.now(), "PV4");
        acc.setId("40");
        Page<Accreditation> page = new PageImpl<>(List.of(acc));

        when(repo.findAll(any(Pageable.class))).thenReturn(page);

        var result = service.list(Optional.empty(), Optional.empty(), Optional.empty(), Pageable.unpaged());

        assertEquals(1, result.getTotalElements());
        assertEquals("40", result.getContent().getFirst().id());
    }
}
