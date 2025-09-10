package com.carlos.challenge.domain;

import com.carlos.challenge.domain.model.PointOfSale;
import com.carlos.challenge.domain.model.graph.MinPaths;
import com.carlos.challenge.domain.model.graph.Neighbor;
import com.carlos.challenge.domain.port.in.PointOfSaleUseCasePort;
import com.carlos.challenge.infrastructure.in.web.service.GraphUseCaseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;


class GraphUseCaseServiceTest {

    PointOfSaleUseCasePort posUseCase;
    GraphUseCaseService service;

    @BeforeEach
    void setUp() {
        posUseCase = mock(PointOfSaleUseCasePort.class);

        when(posUseCase.findById(anyString())).thenAnswer(inv -> {
            String id = inv.getArgument(0);
            return new PointOfSale(id, id, 1);
        });

        service = new GraphUseCaseService(posUseCase);
    }

    @Test
    void upsertEdge_valid_createsUndirectedEdge() {
        service.upsertEdge("A", "B", 7);

        List<Neighbor> nA = service.neighborsOf("A");
        List<Neighbor> nB = service.neighborsOf("B");

        assertThat(nA).extracting(Neighbor::id).contains("B");
        assertThat(nA.stream().filter(n -> n.id().equals("B")).findFirst().get().cost()).isEqualTo(7);

        assertThat(nB).extracting(Neighbor::id).contains("A");
        assertThat(nB.stream().filter(n -> n.id().equals("A")).findFirst().get().cost()).isEqualTo(7);
    }

    @Test
    void neighborsOf_returnsExpectedNeighbors() {
        service.upsertEdge("A", "B", 5);
        service.upsertEdge("A", "C", 3);

        List<Neighbor> res = service.neighborsOf("A");

        assertThat(res).hasSize(2);
        assertThat(res).extracting(Neighbor::id).containsExactlyInAnyOrder("B", "C");
        assertThat(res).extracting(Neighbor::cost).containsExactlyInAnyOrder(5, 3);
    }

    @Test
    void shortestPaths_multipleShortest() {
        service.upsertEdge("A", "B", 1);
        service.upsertEdge("B", "C", 1);
        service.upsertEdge("A", "C", 2);

        MinPaths r = service.shortestPaths("A", "C");

        assertThat(r.totalCost()).isEqualTo(2);

        assertThat(r.paths()).hasSize(2);
        for (List<String> path : r.paths()) {
            assertThat(path.get(0)).isEqualTo("A");
            assertThat(path.get(path.size() - 1)).isEqualTo("C");
        }
    }

    @Test
    void upsertEdge_negativeCost_throws() {
        assertThatThrownBy(() -> service.upsertEdge("X", "Y", -1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("must be >=");
    }

    @Test
    void upsertEdge_reflexive_throws() {
        assertThatThrownBy(() -> service.upsertEdge("Z", "Z", 1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Reflexive edge");
    }
}
