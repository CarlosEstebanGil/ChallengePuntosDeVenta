package com.carlos.challenge.unit.bootstrap;

import com.carlos.challenge.bootstrap.QuickBootstrap;
import com.carlos.challenge.model.PointOfSale;
import com.carlos.challenge.service.GraphService;
import com.carlos.challenge.service.PointCacheService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.ApplicationArguments;
import java.util.*;
import java.util.stream.Collectors;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuickBootstrapTest {

    @Mock PointCacheService pointService;
    @Mock GraphService graphService;
    @Mock ApplicationArguments args;

    QuickBootstrap bootstrap;


    final LinkedHashMap<Integer, String> expectedPoints = new LinkedHashMap<>() {{
        put(1, "CABA");
        put(2, "GBA_1");
        put(3, "GBA_2");
        put(4, "Santa Fe");
        put(5, "Córdoba");
        put(6, "Misiones");
        put(7, "Salta");
        put(8, "Chubut");
        put(9, "Santa Cruz");
        put(10, "Catamarca");
    }};

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

    List<Integer> expectedCosts;

    @BeforeEach
    void setUp() {
        bootstrap = new QuickBootstrap(pointService, graphService);

        expectedCosts = Arrays.stream(expectedEdges)
                .map(e -> e[2])
                .collect(Collectors.toList());

        when(pointService.create(anyString(), any()))
                .thenAnswer(inv -> {
                    String name = inv.getArgument(0, String.class);
                    Integer code = inv.getArgument(1, Integer.class);
                    return new PointOfSale(
                            UUID.randomUUID().toString(),
                            name,
                            code
                    );
                });

    }

    @Test
    void run_inserta_todos_los_puntos_y_todas_las_aristas() throws Exception {

        bootstrap.run(args);

        ArgumentCaptor<String>  nameCap = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Integer> codeCap = ArgumentCaptor.forClass(Integer.class);

        verify(pointService, times(10)).create(nameCap.capture(), codeCap.capture());

        assertThat(nameCap.getAllValues())
                .containsExactlyElementsOf(expectedPoints.values());

        assertThat(codeCap.getAllValues())
                .containsExactly(1,2,3,4,5,6,7,8,9,10);

        ArgumentCaptor<String> fromCap  = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> toCap    = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Integer> costCap = ArgumentCaptor.forClass(Integer.class);

        verify(graphService, times(expectedEdges.length))
                .upsertEdge(fromCap.capture(), toCap.capture(), costCap.capture());

        List<Integer> actualCosts = costCap.getAllValues();
        assertThat(actualCosts).hasSize(expectedEdges.length);
        assertThat(actualCosts).containsExactlyInAnyOrderElementsOf(expectedCosts);
    }

    @Test
    void run_si_create_de_un_nombre_falla_continua_con_el_resto_y_aristas() throws Exception {
        doThrow(new RuntimeException("conflict"))
                .when(pointService).create(eq("CABA"), eq(1));

        when(pointService.findByName("CABA")).thenReturn(Optional.of(
                new PointOfSale(UUID.randomUUID().toString(), "CABA", 1)
        ));

        bootstrap.run(args);

        verify(pointService, times(10)).create(anyString(), any());

        verify(pointService, atLeastOnce()).findByName("CABA");

        verify(graphService, atLeast(1))
                .upsertEdge(anyString(), anyString(), anyInt());
    }

    @Test
    void run_si_una_arista_falla_sigue_con_las_demas() throws Exception {
        doThrow(new RuntimeException("boom"))
                .when(graphService).upsertEdge(anyString(), anyString(), eq(32));

        bootstrap.run(args);

        verify(pointService, times(10)).create(anyString(), any());

        verify(graphService, atLeastOnce()).upsertEdge(anyString(), anyString(), eq(32));

        verify(graphService, times(expectedEdges.length))
                .upsertEdge(anyString(), anyString(), anyInt());
    }
}
