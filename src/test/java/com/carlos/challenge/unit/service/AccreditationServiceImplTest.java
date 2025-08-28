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
    void crear_ok() {
        PointOfSale pv = new PointOfSale(1, "Sucursal Centro");
        when(pointCache.findById(1)).thenReturn(pv);
        when(repo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var req = new CreateAccreditationRequest(BigDecimal.TEN, 1);
        var resp = service.create(req);

        assertEquals(1, resp.idPuntoVenta());
        assertEquals("Sucursal Centro", resp.nombrePuntoVenta());
        assertNotNull(resp.fechaRecepcion());
    }

    @Test
    void list_bothPresent() {
        Instant from = Instant.now().minusSeconds(1000);
        Instant to = Instant.now();
        Accreditation acc = new Accreditation(BigDecimal.ONE, 1, from, "PV1");
        acc.setId(String.valueOf(10L));
        Page<Accreditation> page = new PageImpl<>(List.of(acc));
        when(repo.findByIdPuntoVentaAndFechaRecepcionBetween(eq(1), eq(from), eq(to), any(Pageable.class)))
                .thenReturn(page);

        var result = service.list(Optional.of(1), Optional.of(from), Optional.of(to), Pageable.unpaged());

        assertEquals(1, result.getTotalElements());
        var resp = result.getContent().getFirst();
        assertEquals("10", resp.id());
        assertEquals("PV1", resp.nombrePuntoVenta());
    }

    @Test
    void list_onlyIdPuntoVenta() {
        Accreditation acc = new Accreditation(BigDecimal.ONE, 2, Instant.now(), "PV2");
        acc.setId(String.valueOf(20L));
        Page<Accreditation> page = new PageImpl<>(List.of(acc));
        when(repo.findByIdPuntoVenta(eq(2), any(Pageable.class))).thenReturn(page);

        var result = service.list(Optional.of(2), Optional.empty(), Optional.empty(), Pageable.unpaged());

        assertEquals(1, result.getTotalElements());
        assertEquals("20", result.getContent().getFirst().id());
    }

    @Test
    void list_onlyRange() {
        Instant from = Instant.now().minusSeconds(2000);
        Instant to = Instant.now();
        Accreditation acc = new Accreditation(BigDecimal.ONE, 3, from, "PV3");
        acc.setId(String.valueOf(30L));
        Page<Accreditation> page = new PageImpl<>(List.of(acc));
        when(repo.findByFechaRecepcionBetween(eq(from), eq(to), any(Pageable.class))).thenReturn(page);

        var result = service.list(Optional.empty(), Optional.of(from), Optional.of(to), Pageable.unpaged());

        assertEquals(1, result.getTotalElements());
        assertEquals("30", result.getContent().getFirst().id());
    }

    @Test
    void list_nonePresent() {
        Accreditation acc = new Accreditation(BigDecimal.ONE, 4, Instant.now(), "PV4");
        acc.setId(String.valueOf(40L));
        Page<Accreditation> page = new PageImpl<>(List.of(acc));
        when(repo.findAll(any(Pageable.class))).thenReturn(page);

        var result = service.list(Optional.empty(), Optional.empty(), Optional.empty(), Pageable.unpaged());

        assertEquals(1, result.getTotalElements());
        assertEquals("40", result.getContent().getFirst().id());
    }
}