package com.carlos.challenge.bootstrap;

import com.carlos.challenge.model.PointOfSale;
import com.carlos.challenge.service.GraphService;
import com.carlos.challenge.service.PointCacheService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import java.util.List;

@Profile("!test")
@Component
@RequiredArgsConstructor
public class QuickBootstrap implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(QuickBootstrap.class);
    public static final String CARGANDO_DATOS_POR_AHORA_HARDCODEADOS_DE_PV_Y_GRAFO_QUICK_BOOTSTRAP = "==> Cargando datos, por ahora hardcodeados, de PV y Grafo (quick bootstrap)";
    public static final String NO_SE_PUDO_CREAR_ARISTA_COSTO = "No se pudo crear arista {}-{} (costo {}): {}";
    public static final String QUICK_BOOTSTRAP_PVS_Y_ARISTAS_INICIALES_CARGADAS = "<== Quick bootstrap: {} PVs y {} aristas iniciales cargadas";

    private final PointCacheService pointService;
    private final GraphService graphService;

    @Override
    public void run(ApplicationArguments args) {
        log.info(CARGANDO_DATOS_POR_AHORA_HARDCODEADOS_DE_PV_Y_GRAFO_QUICK_BOOTSTRAP);

        // 1) Puntos de Venta (challenge)
        List<PointOfSale> puntos = List.of(
                new PointOfSale(1, "CABA"),
                new PointOfSale(2, "GBA_1"),
                new PointOfSale(3, "GBA_2"),
                new PointOfSale(4, "Santa Fe"),
                new PointOfSale(5, "Córdoba"),
                new PointOfSale(6, "Misiones"),
                new PointOfSale(7, "Salta"),
                new PointOfSale(8, "Chubut"),
                new PointOfSale(9, "Santa Cruz"),
                new PointOfSale(10, "Catamarca")
        );

        for (PointOfSale pv : puntos) {
            try {
                pointService.create(pv.id(), pv.nombre());
            } catch (Exception e) {
                try {
                    pointService.update(pv.id(), pv.nombre());
                } catch (Exception ignore) {
                }
            }
        }

        // 2) Grafo (challenge) — aristas bidireccionales con costo

        int[][] edges = new int[][]{
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

        for (int[] e : edges) {
            try {
                graphService.upsertEdge(e[0], e[1], e[2]);
            } catch (Exception ex) {
                log.warn(NO_SE_PUDO_CREAR_ARISTA_COSTO, e[0], e[1], e[2], ex.getMessage());
            }
        }

        log.info(QUICK_BOOTSTRAP_PVS_Y_ARISTAS_INICIALES_CARGADAS,
                puntos.size(), edges.length);
    }
}
