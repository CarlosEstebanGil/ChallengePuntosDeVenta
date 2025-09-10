package com.carlos.challenge.domain;

import com.carlos.challenge.domain.model.PointOfSale;
import com.carlos.challenge.domain.port.out.PointOfSaleRepositoryPort;
import com.carlos.challenge.infrastructure.in.web.service.PointOfSaleUseCaseService;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class PointOfSaleUseCaseServiceTest {

    @Test
    void create_read_update_delete() {
        PointOfSaleRepositoryPort repo = mock(PointOfSaleRepositoryPort.class);
        PointOfSaleUseCaseService svc = new PointOfSaleUseCaseService(repo);

        PointOfSale created = new PointOfSale("id-1", "Alpha", 1001);
        when(repo.save(new PointOfSale(null, "Alpha", null))).thenReturn(created);
        assertThat(svc.create("Alpha")).isEqualTo(created);

        when(repo.findById("id-1")).thenReturn(Optional.of(created));
        assertThat(svc.findById("id-1")).isEqualTo(created);

        PointOfSale updated = new PointOfSale("id-1", "Alpha 2", 1001);
        when(repo.findById("id-1")).thenReturn(Optional.of(created));
        when(repo.save(new PointOfSale("id-1", "Alpha 2", 1001))).thenReturn(updated);
        assertThat(svc.update("id-1", "Alpha 2")).isEqualTo(updated);

        doNothing().when(repo).deleteById("id-1");
        svc.delete("id-1");
        verify(repo).deleteById("id-1");
    }

    @Test
    void listAll() {
        PointOfSaleRepositoryPort repo = mock(PointOfSaleRepositoryPort.class);
        PointOfSaleUseCaseService svc = new PointOfSaleUseCaseService(repo);
        PointOfSale pos = new PointOfSale("a", "A", 100);
        when(repo.findAll()).thenReturn(List.of(pos));
        assertThat(svc.findAll()).hasSize(1).contains(pos);
    }
}
