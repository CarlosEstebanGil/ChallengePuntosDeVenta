package com.carlos.challenge.unit.bootstrap;

import com.carlos.challenge.bootstrap.QuickBootstrap;
import com.carlos.challenge.service.GraphService;
import com.carlos.challenge.service.PointCacheService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.ApplicationArguments;
import java.util.Map;
import java.util.stream.IntStream;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuickBootstrapTest {

    @Mock
    PointCacheService pointService;

    @Mock
    GraphService graphService;

    @Mock
    ApplicationArguments args;

    QuickBootstrap bootstrap;

    final Map<Integer, String> expectedPoints = Map.ofEntries(
            Map.entry(1, "CABA"),
            Map.entry(2, "GBA_1"),
            Map.entry(3, "GBA_2"),
            Map.entry(4, "Santa Fe"),
            Map.entry(5, "Córdoba"),
            Map.entry(6, "Misiones"),
            Map.entry(7, "Salta"),
            Map.entry(8, "Chubut"),
            Map.entry(9, "Santa Cruz"),
            Map.entry(10, "Catamarca")
    );

    // edges: {from,to,costo}
    final int[][] expectedEdges = new int[][]{
            {1,2,2},
            {1,3,3},
            {2,3,5},
            {2,4,10},
            {1,4,11},
            {4,5,5},
            {2,5,14},
            {6,7,32},
            {8,9,11},
            {10,7,5},
            {3,8,10},
            {5,8,30},
            {10,5,5},
            {4,6,6}
    };

    @BeforeEach
    void setUp() {
        bootstrap = new QuickBootstrap(pointService, graphService);
    }

    @Test
    void run_inserta_todos_los_puntos_y_todas_las_aristas() throws Exception {
        // when
        bootstrap.run(args);

        // then:
        ArgumentCaptor<Integer> idCap = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<String> nameCap = ArgumentCaptor.forClass(String.class);
        verify(pointService, times(10)).create(idCap.capture(), nameCap.capture());

        var created = IntStream.range(0, idCap.getAllValues().size())
                .boxed()
                .collect(java.util.stream.Collectors.toMap(
                        i -> idCap.getAllValues().get(i),
                        i -> nameCap.getAllValues().get(i)
                ));

        assertThat(created).isEqualTo(expectedPoints);

        verify(pointService, never()).update(anyInt(), anyString());

        for (int[] e : expectedEdges) {
            verify(graphService).upsertEdge(e[0], e[1], e[2]);
        }

        verify(graphService, times(expectedEdges.length)).upsertEdge(anyInt(), anyInt(), anyInt());
        verifyNoMoreInteractions(pointService, graphService);
    }

    @Test
    void run_si_create_falla_hace_update_y_continua() throws Exception {

        doThrow(new RuntimeException("conflict"))
                .when(pointService).create(1, "CABA");

        bootstrap.run(args);

        verify(pointService, times(10)).create(anyInt(), anyString());
        verify(pointService).update(1, "CABA");
        verify(graphService, times(expectedEdges.length)).upsertEdge(anyInt(), anyInt(), anyInt());
    }

    @Test
    void run_si_una_arista_falla_sigue_con_las_demas() throws Exception {
        doThrow(new RuntimeException("boom"))
                .when(graphService).upsertEdge(6, 7, 32);

        bootstrap.run(args);

        verify(pointService, times(10)).create(anyInt(), anyString());
        verify(graphService).upsertEdge(6, 7, 32);
        verify(graphService).upsertEdge(1, 2, 2);
        verify(graphService, times(expectedEdges.length)).upsertEdge(anyInt(), anyInt(), anyInt());
    }
}
