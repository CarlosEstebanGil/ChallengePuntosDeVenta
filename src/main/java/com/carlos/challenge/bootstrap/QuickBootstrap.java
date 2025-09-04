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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Profile("!test")
@Component
@RequiredArgsConstructor
public class QuickBootstrap implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(QuickBootstrap.class);

    private static final String LOADING_HARDCODED_PVS_AND_GRAPH =
            "==> Loading hardcoded Points of Sale and Graph (quick bootstrap)";
    private static final String QUICK_BOOTSTRAP_PVS_AND_EDGES_LOADED =
            "<== Quick bootstrap: {} Points of Sale and {} initial edges loaded";
    private static final String FAILED_TO_CREATE_EDGE_COST =
            "Failed to create edge {}-{} (cost {}): {}";

    private final PointCacheService pointCacheService;
    private final GraphService graphService;

    @Override
    public void run(ApplicationArguments args) {
        log.info(LOADING_HARDCODED_PVS_AND_GRAPH);

        List<String> names = List.of(
                "CABA",
                "GBA_1",
                "GBA_2",
                "Santa Fe",
                "Córdoba",
                "Misiones",
                "Salta",
                "Chubut",
                "Santa Cruz",
                "Catamarca"
        );

        List<PointOfSale> created = new ArrayList<>(names.size());

        for (int i = 0; i < names.size(); i++) {
            String name = names.get(i);
            int code = i + 1;
            try {
                PointOfSale pos = pointCacheService.create(name, code);
                created.add(pos);
            } catch (Exception e) {
                Optional<PointOfSale> existing = pointCacheService.findByName(name);
                if (existing.isPresent()) {
                    created.add(existing.get());
                } else {
                    log.warn("Could not create nor find existing POS '{}': {}", name, e.getMessage());
                    created.add(null);
                }
            }
        }

        java.util.function.IntFunction<String> idOf = (idx1) -> {
            int idx0 = idx1 - 1;
            PointOfSale pos = created.get(idx0);
            if (pos == null) {
                throw new IllegalStateException("POS at index " + idx1 + " was not created");
            }
            return pos.id();
        };

        int[][] edges = new int[][]{
                {1, 2, 2},
                {1, 3, 3},
                {2, 3, 5},
                {2, 4, 10},
                {1, 4, 11},
                {4, 5, 5},
                {2, 5, 14},
                {6, 7, 32},
                {8, 9, 11},
                {10, 7, 5},
                {3, 8, 10},
                {5, 8, 30},
                {10, 5, 5},
                {4, 6, 6}
        };

        int ok = 0;
        for (int[] e : edges) {
            int fromCode = e[0];
            int toCode = e[1];
            int cost = e[2];
            try {
                String fromId = idOf.apply(fromCode);
                String toId = idOf.apply(toCode);
                graphService.upsertEdge(fromId, toId, cost);
                ok++;
            } catch (Exception ex) {
                log.warn(FAILED_TO_CREATE_EDGE_COST, fromCode, toCode, cost, ex.getMessage());
            }
        }

        log.info(QUICK_BOOTSTRAP_PVS_AND_EDGES_LOADED, created.size(), ok);
    }
}
