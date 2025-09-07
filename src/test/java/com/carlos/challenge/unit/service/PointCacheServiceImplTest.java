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
import java.util.UUID;
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
        PointOfSale pv = service.create("Sucursal Centro");

        assertThat(pv.id()).isNotBlank();
        assertThat(pv.name()).isEqualTo("Sucursal Centro");

        PointOfSale fetched = service.findById(pv.id());
        assertThat(fetched.id()).isEqualTo(pv.id());
        assertThat(fetched.name()).isEqualTo("Sucursal Centro");
        assertThat(fetched.code()).isNotNull();
    }

    @Test
    void update_ok() {
        PointOfSale created = service.create("Viejo");
        PointOfSale updated = service.update(created.id(), "Nuevo");

        assertThat(updated.id()).isEqualTo(created.id());
        assertThat(updated.name()).isEqualTo("Nuevo");
        assertThat(updated.code()).isEqualTo(created.code());

        assertThat(service.findById(created.id()).name()).isEqualTo("Nuevo");
    }

    @Test
    void update_not_found() {
        String randomId = UUID.randomUUID().toString();
        assertThatThrownBy(() -> service.update(randomId, "X"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("does not exist");
    }

    @Test
    void delete_ok_y_luego_findById_not_found() {
        PointOfSale pv = service.create("PV3");
        service.delete(pv.id());

        assertThatThrownBy(() -> service.findById(pv.id()))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("does not exist");
    }

    @Test
    void delete_not_found() {
        String randomId = UUID.randomUUID().toString();
        assertThatThrownBy(() -> service.delete(randomId))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("does not exist");
    }

    @Test
    void findAll_devuelve_ordenado_por_nombre() {
        service.create("Zeta");
        service.create("Alfa");
        service.create("Mango");

        List<PointOfSale> all = service.findAll();

        assertThat(all).extracting(PointOfSale::name)
                .containsExactly("Alfa", "Mango", "Zeta");

        assertThat(all).allSatisfy(p -> assertThat(p.id()).isNotBlank());
    }

    @Test
    void findByName_varios_casos() {
        assertThat(service.findByName(null)).isEmpty();
        assertThat(service.findByName("   ")).isEmpty();
        assertThat(service.findByName("NoExiste")).isEmpty();

        PointOfSale pv = service.create("Sucursal Norte");
        assertThat(service.findByName("Sucursal Norte"))
                .isPresent()
                .get()
                .extracting(PointOfSale::id, PointOfSale::name)
                .containsExactly(pv.id(), "Sucursal Norte");

        assertThat(service.findByName("sucursal norte")).isEmpty();
    }

    @Test
    void create_con_code_explicito_y_findByCode_y_resolveId() {
        PointOfSale pv10 = service.create("Sucursal 10", 10);
        assertThat(pv10.id()).isNotBlank();
        assertThat(pv10.name()).isEqualTo("Sucursal 10");
        assertThat(pv10.code()).isEqualTo(10);

        assertThat(service.findByCode(10))
                .isPresent()
                .get()
                .extracting(PointOfSale::id, PointOfSale::name, PointOfSale::code)
                .containsExactly(pv10.id(), "Sucursal 10", 10);

        String resolved = service.resolveId("10");
        assertThat(resolved).isEqualTo(pv10.id());

        String resolved2 = service.resolveId(pv10.id());
        assertThat(resolved2).isEqualTo(pv10.id());
    }

    @Test
    void create_con_code_duplicado_debe_fallar() {
        service.create("A", 99);
        assertThatThrownBy(() -> service.create("B", 99))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("already exists")
                .hasMessageContaining("code");
    }

    @Test
    void findByCode_not_found_y_resolveId_desconocido() {
        assertThat(service.findByCode(12345)).isEmpty();

        assertThatThrownBy(() -> service.resolveId("12345"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("does not exist");

        assertThatThrownBy(() -> service.resolveId("no-numero-no-uuid"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Invalid");
    }
}
