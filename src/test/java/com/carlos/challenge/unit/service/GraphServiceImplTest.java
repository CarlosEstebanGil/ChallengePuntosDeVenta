package com.carlos.challenge.unit.service;

import com.carlos.challenge.dto.MinPathResponse;
import com.carlos.challenge.dto.NeighborResponse;
import com.carlos.challenge.model.PointOfSale;
import com.carlos.challenge.service.GraphService;
import com.carlos.challenge.service.PointCacheService;
import com.carlos.challenge.service.impl.GraphServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import java.util.Set;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GraphServiceImplTest {

    @Mock
    private PointCacheService pointCache;

    @InjectMocks
    private GraphServiceImpl impl;

    private GraphService graph;

    @BeforeEach
    void setUp() {
        graph = impl;

        Set<Integer> valid = Set.of(1, 2, 3, 4, 5);

        lenient().when(pointCache.findById(anyInt())).then(inv -> {
            int id = inv.getArgument(0, Integer.class);
            if (!valid.contains(id)) {
                throw new ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "No existe el punto");
            }
            return new PointOfSale(id, "PV " + id);
        });
    }

    @Test
    void upsertEdge_crea_arista_bidir_y_neighbors_ordenados() {
        graph.upsertEdge(1, 2, 5);
        graph.upsertEdge(1, 3, 9);

        List<NeighborResponse> ns = graph.neighborsOf(1);
        assertThat(ns).extracting(NeighborResponse::id).containsExactly(2,3);
        assertThat(ns.get(0).cost()).isEqualTo(5);
        assertThat(ns.get(1).cost()).isEqualTo(9);

        assertThat(graph.neighborsOf(2)).extracting(NeighborResponse::id).contains(1);
    }

    @Test
    void upsertEdge_valida_reflexiva_y_costs_negativos() {
        assertThatThrownBy(() -> graph.upsertEdge(1, 1, 5))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("reflexiva");

        assertThatThrownBy(() -> graph.upsertEdge(1, 2, -1))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining(">=");
    }

    @Test
    void removeEdge_elimina_en_ambos_sentidos() {
        graph.upsertEdge(1, 2, 5);
        graph.upsertEdge(2, 3, 3);

        assertThat(graph.neighborsOf(1)).extracting(NeighborResponse::id).contains(2);
        assertThat(graph.neighborsOf(2)).extracting(NeighborResponse::id).contains(1,3);

        graph.removeEdge(1, 2);

        assertThat(graph.neighborsOf(1)).extracting(NeighborResponse::id).doesNotContain(2);
        assertThat(graph.neighborsOf(2)).extracting(NeighborResponse::id).doesNotContain(1);
        assertThat(graph.neighborsOf(2)).extracting(NeighborResponse::id).contains(3);
    }

    @Test
    void shortestPath_devuelve_camino_minimo_y_cost() {
        graph.upsertEdge(1, 2, 5);
        graph.upsertEdge(1, 3, 9);
        graph.upsertEdge(2, 3, 3);
        graph.upsertEdge(2, 4, 9);
        graph.upsertEdge(3, 4, 2);

        MinPathResponse r = graph.shortestPath(1, 4);
        assertThat(r.totalCost()).isEqualTo(10);
        assertThat(r.routeIds()).containsExactly(1,2,3,4);
        assertThat(r.routeNames()).containsExactly("PV 1","PV 2","PV 3","PV 4");
    }

    @Test
    void shortestPath_mismo_nodo() {
        MinPathResponse r = graph.shortestPath(5, 5);
        assertThat(r.totalCost()).isEqualTo(0);
        assertThat(r.routeIds()).containsExactly(5);
    }

    @Test
    void shortestPath_sin_camino_404() {
        graph.upsertEdge(1, 2, 5);
        graph.upsertEdge(3, 4, 7);

        assertThatThrownBy(() -> graph.shortestPath(1, 4))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("No existe camino");
    }

    @Test
    void neighborsOf_punto_inexistente_404() {
        assertThatThrownBy(() -> graph.neighborsOf(999))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("No existe el punto");
    }
}
