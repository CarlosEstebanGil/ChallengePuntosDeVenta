package com.carlos.challenge.unit.service;

import com.carlos.challenge.dto.MinPathsResponse;
import com.carlos.challenge.dto.NeighborResponse;
import com.carlos.challenge.dto.PathDetail;
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
import java.util.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;

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

        Set<String> valid = Set.of("pos-1", "pos-2", "pos-3", "pos-4", "pos-5");

        lenient().when(pointCache.resolveId(anyString())).thenAnswer(inv -> inv.getArgument(0, String.class));

        lenient().when(pointCache.findById(anyString())).then(inv -> {
            String id = inv.getArgument(0, String.class);
            if (!valid.contains(id)) {
                throw new ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "The point does not exist");
            }
            String name = switch (id) {
                case "pos-1" -> "PV 1";
                case "pos-2" -> "PV 2";
                case "pos-3" -> "PV 3";
                case "pos-4" -> "PV 4";
                case "pos-5" -> "PV 5";
                default -> "PV ?";
            };
            Integer code = switch (id) {
                case "pos-1" -> 1;
                case "pos-2" -> 2;
                case "pos-3" -> 3;
                case "pos-4" -> 4;
                case "pos-5" -> 5;
                default -> 0;
            };
            return new PointOfSale(id, name, code);
        });
    }

    @Test
    void upsertEdge_crea_arista_bidir_y_neighbors_ordenados() {
        graph.upsertEdge("pos-1", "pos-2", 5);
        graph.upsertEdge("pos-1", "pos-3", 9);

        List<NeighborResponse> ns = graph.neighborsOf("pos-1");
        assertThat(ns).extracting(NeighborResponse::id).containsExactly("pos-2", "pos-3");
        assertThat(ns.get(0).cost()).isEqualTo(5);
        assertThat(ns.get(1).cost()).isEqualTo(9);
    }

    @Test
    void upsertEdge_valida_reflexiva_y_costs_negativos() {
        assertThatThrownBy(() -> graph.upsertEdge("pos-1", "pos-1", 5))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Reflexive");

        assertThatThrownBy(() -> graph.upsertEdge("pos-1", "pos-2", -1))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining(">=");
    }

    @Test
    void shortestPaths_devuelve_camino_minimo_con_codes() {
        graph.upsertEdge("pos-1", "pos-2", 5);
        graph.upsertEdge("pos-1", "pos-3", 9);
        graph.upsertEdge("pos-2", "pos-3", 3);
        graph.upsertEdge("pos-2", "pos-4", 9);
        graph.upsertEdge("pos-3", "pos-4", 2);

        MinPathsResponse r = graph.shortestPaths("pos-1", "pos-4");

        assertThat(r.totalCost()).isEqualTo(10);
        assertThat(r.paths()).hasSize(1);

        PathDetail p = r.paths().getFirst();

        List<String> idsExp   = List.of("pos-1", "pos-2", "pos-3", "pos-4");
        List<String> namesExp = List.of("PV 1",   "PV 2",   "PV 3",   "PV 4");
        List<Integer> codesExp= List.of(1,        2,        3,        4);

        assertPathEitherDirection(p, idsExp, namesExp, codesExp);
    }

    @Test
    void shortestPaths_mismo_nodo_con_codes() {
        MinPathsResponse r = graph.shortestPaths("pos-5", "pos-5");

        assertThat(r.totalCost()).isEqualTo(0);
        assertThat(r.paths()).hasSize(1);
        PathDetail p = r.paths().getFirst();

        List<String> idsExp   = List.of("pos-5");
        List<String> namesExp = List.of("PV 5");
        List<Integer> codesExp= List.of(5);

        assertPathEitherDirection(p, idsExp, namesExp, codesExp);
    }

    @Test
    void shortestPaths_sin_camino_404() {
        graph.upsertEdge("pos-1", "pos-2", 5);
        graph.upsertEdge("pos-3", "pos-4", 7);

        assertThatThrownBy(() -> graph.shortestPaths("pos-1", "pos-4"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("minimum path");
    }

    private static void assertPathEitherDirection(PathDetail p,
                                                  List<String> idsExp,
                                                  List<String> namesExp,
                                                  List<Integer> codesExp) {
        List<String> ids = p.routeIds();
        List<String> names = p.routeNames();
        List<Integer> codes = p.routeCodes();

        List<String> idsRev = new ArrayList<>(idsExp);
        Collections.reverse(idsRev);

        List<String> namesRev = new ArrayList<>(namesExp);
        Collections.reverse(namesRev);

        List<Integer> codesRev = new ArrayList<>(codesExp);
        Collections.reverse(codesRev);

        assertThat(ids).isIn(idsExp, idsRev);
        assertThat(names).isIn(namesExp, namesRev);
        assertThat(codes).isIn(codesExp, codesRev);

        if (idsExp.size() == 1) {
            assertThat(ids).hasSize(1);
            assertThat(ids.getFirst()).isEqualTo(idsExp.getFirst());
        } else {
            boolean direct = ids.getFirst().equals(idsExp.getFirst()) && ids.getLast().equals(idsExp.getLast());
            boolean reverse = ids.getFirst().equals(idsExp.getLast()) && ids.getLast().equals(idsExp.getFirst());
            assertThat(direct || reverse).isTrue();
        }
    }
}
