// com.carlos.challenge.service.impl.GraphServiceImpl
package com.carlos.challenge.service.impl;

import com.carlos.challenge.dto.MinPathsResponse;
import com.carlos.challenge.dto.NeighborResponse;
import com.carlos.challenge.dto.PathDetail;
import com.carlos.challenge.model.PointOfSale;
import com.carlos.challenge.service.GraphService;
import com.carlos.challenge.service.PointCacheService;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.StampedLock;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class GraphServiceImpl implements GraphService {

    public static final String NO_EXISTE_CAMINO_MÍNIMO_ENTRE_LOS_PUNTOS = "There is no minimum path between the points";
    public static final String NO_SE_PERMITE_ARISTA_REFLEXIVA = "Reflexive edge is not allowed";
    public static final String EL_COSTO_DEBE_SER_0 = "The cost must be >= 0";
    private final PointCacheService points;

    private final ConcurrentMap<String, ConcurrentMap<String, Integer>> adj = new ConcurrentHashMap<>();

    private final StampedLock lock = new StampedLock();

    public GraphServiceImpl(PointCacheService points) {
        this.points = points;
    }

    @Override
    public void upsertEdge(String fromIdOrCode, String toIdOrCode, int cost) {
        if (cost < 0) {
            throw new ResponseStatusException(BAD_REQUEST, EL_COSTO_DEBE_SER_0);
        }

        final String fromId = points.resolveId(fromIdOrCode);
        final String toId   = points.resolveId(toIdOrCode);

        if (fromId.equals(toId)) {
            throw new ResponseStatusException(BAD_REQUEST, NO_SE_PERMITE_ARISTA_REFLEXIVA);
        }

        points.findById(fromId);
        points.findById(toId);

        long stamp = lock.writeLock();
        try {
            adj.computeIfAbsent(fromId, k -> new ConcurrentHashMap<>()).put(toId, cost);
            adj.computeIfAbsent(toId,   k -> new ConcurrentHashMap<>()).put(fromId, cost);
        } finally {
            lock.unlockWrite(stamp);
        }
    }

    @Override
    public void removeEdge(String fromIdOrCode, String toIdOrCode) {
        final String fromId = points.resolveId(fromIdOrCode);
        final String toId   = points.resolveId(toIdOrCode);

        points.findById(fromId);
        points.findById(toId);

        long stamp = lock.writeLock();
        try {
            Optional.ofNullable(adj.get(fromId)).ifPresent(m -> m.remove(toId));
            Optional.ofNullable(adj.get(toId)).ifPresent(m -> m.remove(fromId));
        } finally {
            lock.unlockWrite(stamp);
        }
    }

    @Override
    public List<NeighborResponse> neighborsOf(String idOrCode) {
        final String id = points.resolveId(idOrCode);

        points.findById(id);

        long stamp = lock.tryOptimisticRead();
        Map<String, Integer> snap = adj.getOrDefault(id, new ConcurrentHashMap<>());
        Map<String, Integer> copy = new HashMap<>(snap);
        if (!lock.validate(stamp)) {
            stamp = lock.readLock();
            try {
                snap = adj.getOrDefault(id, new ConcurrentHashMap<>());
                copy = new HashMap<>(snap);
            } finally {
                lock.unlockRead(stamp);
            }
        }

        if (copy.isEmpty()) return List.of();

        List<NeighborResponse> out = new ArrayList<>(copy.size());
        for (var e : copy.entrySet()) {
            PointOfSale p = points.findById(e.getKey());
            out.add(new NeighborResponse(p.id(), p.name(), e.getValue()));
        }

        out.sort(Comparator
                .comparing(NeighborResponse::name, String.CASE_INSENSITIVE_ORDER)
                .thenComparing(NeighborResponse::id));
        return out;
    }

    @Override
    public MinPathsResponse shortestPaths(String fromIdOrCode, String toIdOrCode) {
        final String start = points.resolveId(fromIdOrCode);
        final String goal  = points.resolveId(toIdOrCode);

        points.findById(start);
        points.findById(goal);

        if (start.equals(goal)) {
            PointOfSale p = points.findById(start);
            PathDetail path = new PathDetail(
                    List.of(p.id()),
                    List.of(p.name()),
                    List.of(p.code())
            );
            return new MinPathsResponse(0, List.of(path));
        }

        Map<String, Map<String, Integer>> graph = deepSnapshot(adj);

        Map<String, Integer> dist = new HashMap<>();
        Map<String, Set<String>> preds = new HashMap<>();
        Set<String> visited = new HashSet<>();
        PriorityQueue<String> pq = new PriorityQueue<>(Comparator.comparingInt(n -> dist.getOrDefault(n, Integer.MAX_VALUE)));

        graph.keySet().forEach(n -> dist.put(n, Integer.MAX_VALUE));
        dist.put(start, 0);
        pq.add(start);

        while (!pq.isEmpty()) {
            String u = pq.poll();
            if (!visited.add(u)) continue;
            if (u.equals(goal)) break;

            for (var e : graph.getOrDefault(u, Map.of()).entrySet()) {
                String v = e.getKey();
                int w = e.getValue();
                if (w < 0) continue;

                int du = dist.getOrDefault(u, Integer.MAX_VALUE);
                if (du == Integer.MAX_VALUE) continue;

                int alt = du + w;
                int dv  = dist.getOrDefault(v, Integer.MAX_VALUE);

                if (alt < dv) {
                    dist.put(v, alt);
                    preds.put(v, new HashSet<>(List.of(u)));
                    pq.add(v);
                } else if (alt == dv) {
                    preds.computeIfAbsent(v, k -> new HashSet<>()).add(u);
                }
            }
        }

        int best = dist.getOrDefault(goal, Integer.MAX_VALUE);
        if (best == Integer.MAX_VALUE) {
            throw new ResponseStatusException(NOT_FOUND, NO_EXISTE_CAMINO_MÍNIMO_ENTRE_LOS_PUNTOS);
        }

               List<List<String>> allPaths = new ArrayList<>();
        Deque<String> stack = new ArrayDeque<>();
        stack.push(goal);
        backtrackPaths(start, goal, preds, stack, allPaths);

                List<PathDetail> details = allPaths.stream()
                .map(routeIds -> {
                    List<String> ids = List.copyOf(routeIds);
                    List<String> names = ids.stream()
                            .map(points::findById)
                            .map(PointOfSale::name)
                            .toList();
                    List<Integer> codes = ids.stream()
                            .map(points::findById)
                            .map(PointOfSale::code)
                            .toList();
                    return new PathDetail(ids, names, codes);
                })
                .toList();

        return new MinPathsResponse(best, details);
    }

    private static void backtrackPaths(String start, String current,
                                       Map<String, Set<String>> preds,
                                       Deque<String> stack,
                                       List<List<String>> out) {
        if (current.equals(start)) {
            out.add(new ArrayList<>(stack));
            return;
        }
        for (String p : preds.getOrDefault(current, Set.of())) {
            stack.push(p);
            backtrackPaths(start, p, preds, stack, out);
            stack.pop();
        }
    }

    private Map<String, Map<String, Integer>> deepSnapshot(
            ConcurrentMap<String, ConcurrentMap<String, Integer>> source) {

        long stamp = lock.tryOptimisticRead();
        Map<String, Map<String, Integer>> copy = copyAdj(source);
        if (!lock.validate(stamp)) {
            stamp = lock.readLock();
            try {
                copy = copyAdj(source);
            } finally {
                lock.unlockRead(stamp);
            }
        }
        return copy;
    }

    private static Map<String, Map<String, Integer>> copyAdj(
            ConcurrentMap<String, ConcurrentMap<String, Integer>> source) {
        Map<String, Map<String, Integer>> copy = new HashMap<>(source.size());
        for (var e : source.entrySet()) {
            copy.put(e.getKey(), new HashMap<>(e.getValue()));
        }
        return copy;
    }
}
