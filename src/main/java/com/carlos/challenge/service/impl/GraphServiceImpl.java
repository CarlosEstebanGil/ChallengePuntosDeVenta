package com.carlos.challenge.service.impl;

import com.carlos.challenge.dto.MinPathResponse;
import com.carlos.challenge.dto.NeighborResponse;
import com.carlos.challenge.model.PointOfSale;
import com.carlos.challenge.service.GraphService;
import com.carlos.challenge.service.PointCacheService;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class GraphServiceImpl implements GraphService {

    public static final String NO_EXISTE_CAMINO_ENTRE_D_Y_D = "No existe camino entre %d y %d";
    public static final String NO_SE_PERMITE_ARISTA_REFLEXIVA_FROM_ID_TO_ID = "No se permite arista reflexiva (fromId==toId)";
    public static final String EL_cost_DEBE_SER_0 = "El cost debe ser >= 0";

    private final ConcurrentMap<Integer, ConcurrentMap<Integer, Integer>> adj = new ConcurrentHashMap<>();

    private final PointCacheService pointCache;

    public GraphServiceImpl(PointCacheService pointCache) {
        this.pointCache = pointCache;
    }

    @Override
    public void upsertEdge(int fromId, int toId, int cost) {
        if (fromId == toId) {
            throw new ResponseStatusException(BAD_REQUEST, NO_SE_PERMITE_ARISTA_REFLEXIVA_FROM_ID_TO_ID);
        }
        if (cost < 0) {
            throw new ResponseStatusException(BAD_REQUEST, EL_cost_DEBE_SER_0);
        }

        PointOfSale a = pointCache.findById(fromId);
        PointOfSale b = pointCache.findById(toId);

        adj.computeIfAbsent(a.id(), k -> new ConcurrentHashMap<>()).put(b.id(), cost);
        adj.computeIfAbsent(b.id(), k -> new ConcurrentHashMap<>()).put(a.id(), cost);  // B->A
    }

    @Override
    public void removeEdge(int fromId, int toId) {

        pointCache.findById(fromId);
        pointCache.findById(toId);

        Optional.ofNullable(adj.get(fromId)).ifPresent(m -> m.remove(toId));
        Optional.ofNullable(adj.get(toId)).ifPresent(m -> m.remove(fromId));
    }

    @Override
    public List<NeighborResponse> neighborsOf(int fromId) {
        PointOfSale pv = pointCache.findById(fromId);

        Map<Integer, Integer> neighbors = new HashMap<>(adj.getOrDefault(pv.id(), new ConcurrentHashMap<>()));
        if (neighbors.isEmpty()) return List.of();

        List<NeighborResponse> list = new ArrayList<>();
        for (Map.Entry<Integer, Integer> e : neighbors.entrySet()) {
            PointOfSale nb = pointCache.findById(e.getKey()); // resuelvo name
            list.add(new NeighborResponse(nb.id(), nb.name(), e.getValue()));
        }

        list.sort(Comparator.comparingInt(NeighborResponse::cost).thenComparing(NeighborResponse::id));
        return list;
    }

    @Override
    public MinPathResponse shortestPath(int fromId, int toId) {
        PointOfSale from = pointCache.findById(fromId);
        PointOfSale to   = pointCache.findById(toId);

        if (from.id().equals(to.id())) {
            return new MinPathResponse(0, List.of(from.id()), List.of(from.name()));
        }

        // Dijkstra (read only)
        Map<Integer, Integer> dist = new HashMap<>();
        Map<Integer, Integer> prev = new HashMap<>();
        PriorityQueue<int[]> pq = new PriorityQueue<>(Comparator.comparingInt(a -> a[1])); // [node, dist]

        dist.put(from.id(), 0);
        pq.offer(new int[]{from.id(), 0});

        while (!pq.isEmpty()) {
            int[] cur = pq.poll();
            int u = cur[0], du = cur[1];
            if (du != dist.getOrDefault(u, Integer.MAX_VALUE))
                continue;
            if (u == to.id())
                break;

            Map<Integer, Integer> neigh = adj.getOrDefault(u, new ConcurrentHashMap<>());
            for (Map.Entry<Integer, Integer> e : neigh.entrySet()) {
                int v = e.getKey();
                int w = e.getValue();
                int alt = du + w;
                if (alt < dist.getOrDefault(v, Integer.MAX_VALUE)) {
                    dist.put(v, alt);
                    prev.put(v, u);
                    pq.offer(new int[]{v, alt});
                }
            }
        }

        Integer best = dist.get(to.id());
        if (best == null) {
            throw new ResponseStatusException(NOT_FOUND, NO_EXISTE_CAMINO_ENTRE_D_Y_D.formatted(from.id(), to.id()));
        }


        LinkedList<Integer> path = new LinkedList<>();
        for (Integer v = to.id(); v != null; v = prev.get(v)) { // reconstruyo la ruta
            path.addFirst(v);
            if (v.equals(from.id()))
                break;
        }

        List<String> names = path.stream()
                .map(id -> pointCache.findById(id).name())
                .toList();

        return new MinPathResponse(best, List.copyOf(path), names);
    }
}
