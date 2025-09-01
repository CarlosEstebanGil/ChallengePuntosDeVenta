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
    public static final String LOADING_HARDCODED_PVS_AND_GRAPH = "==> Loading hardcoded Points of Sale and Graph (quick bootstrap)";
    public static final String FAILED_TO_CREATE_EDGE_COST = "Failed to create edge {}-{} (cost {}): {}";
    public static final String QUICK_BOOTSTRAP_PVS_AND_EDGES_LOADED = "<== Quick bootstrap: {} Points of Sale and {} initial edges loaded";

    private final PointCacheService pointCacheService;
    private final GraphService graphService;

    @Override
    public void run(ApplicationArguments args) {
        log.info(LOADING_HARDCODED_PVS_AND_GRAPH);

        List<PointOfSale> pointsOfSale = List.of(
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

        for (PointOfSale pos : pointsOfSale) {
            try {
                pointCacheService.create(pos.id(), pos.name());
            } catch (Exception e) {
                try {
                    pointCacheService.update(pos.id(), pos.name());
                } catch (Exception ignore) {
                }
            }
        }

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

        for (int[] edge : edges) {
            try {
                graphService.upsertEdge(edge[0], edge[1], edge[2]);
            } catch (Exception ex) {
                log.warn(FAILED_TO_CREATE_EDGE_COST, edge[0], edge[1], edge[2], ex.getMessage());
            }
        }

        log.info(QUICK_BOOTSTRAP_PVS_AND_EDGES_LOADED,
                pointsOfSale.size(), edges.length);
    }
}