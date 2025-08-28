package com.carlos.challenge.unit.service;

import com.carlos.challenge.model.PointOfSale;
import com.carlos.challenge.service.PointCacheService;
import com.carlos.challenge.service.impl.PointCacheServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class PointCacheServiceImplTest {

    @InjectMocks
    private PointCacheServiceImpl impl;

    private PointCacheService service;

    @BeforeEach
    void setUp() {
        service = impl;
    }

    @Test
    void create_ok_y_luego_findById() {
        PointOfSale pv = service.create(1, "Sucursal Centro");
        assertThat(pv.id()).isEqualTo(1);
        assertThat(pv.nombre()).isEqualTo("Sucursal Centro");

        assertThat(service.findById(1).nombre()).isEqualTo("Sucursal Centro");
    }

    @Test
    void create_conflict_si_id_existente() {
        service.create(1, "A");
        assertThatThrownBy(() -> service.create(1, "B"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("ya existe");
    }

    @Test
    void update_ok() {
        service.create(2, "Viejo");
        PointOfSale updated = service.update(2, "Nuevo");
        assertThat(updated.nombre()).isEqualTo("Nuevo");
        assertThat(service.findById(2).nombre()).isEqualTo("Nuevo");
    }

    @Test
    void update_not_found() {
        assertThatThrownBy(() -> service.update(999, "X"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("No existe el punto");
    }

    @Test
    void delete_ok_y_luego_findById_not_found() {
        service.create(3, "PV3");
        service.delete(3);
        assertThatThrownBy(() -> service.findById(3))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("No existe el punto");
    }

    @Test
    void delete_not_found() {
        assertThatThrownBy(() -> service.delete(777))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("No existe el punto");
    }

    @Test
    void findAll_devuelve_ordenado_por_id() {
        service.create(10, "PV10");
        service.create(2, "PV2");
        service.create(5, "PV5");

        List<PointOfSale> all = service.findAll();
        assertThat(all).extracting(PointOfSale::id).containsExactly(2,5,10);
    }
}
