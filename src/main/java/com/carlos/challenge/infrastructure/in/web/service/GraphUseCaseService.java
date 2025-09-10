package com.carlos.challenge.infrastructure.in.web.service;


import com.carlos.challenge.domain.model.graph.MinPaths;
import com.carlos.challenge.domain.model.graph.Neighbor;
import com.carlos.challenge.domain.port.in.GraphUseCasePort;
import com.carlos.challenge.domain.port.in.PointOfSaleUseCasePort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.StampedLock;

@Service
@Transactional
public class GraphUseCaseService implements GraphUseCasePort {

    private static final String ERR_REFLEXIVE_EDGE = "Reflexive edge is not allowed";
    private static final String ERR_COST_NEGATIVE  = "The cost must be >= 0";
    private static final String ERR_NO_MIN_PATH    = "There is no minimum path between the points";

    private final PointOfSaleUseCasePort points;

    private final ConcurrentMap<String, ConcurrentMap<String, Integer>> adj = new ConcurrentHashMap<>();
    private final StampedLock lock = new StampedLock();

    public GraphUseCaseService(PointOfSaleUseCasePort points) {
        this.points = points;
    }

    @Override
    public void upsertEdge(String fromId, String toId, int cost) {
        if (cost < 0) {
            throw new IllegalArgumentException(ERR_COST_NEGATIVE);
        }
        if (fromId.equals(toId)) {
            throw new IllegalArgumentException(ERR_REFLEXIVE_EDGE);
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
    public void removeEdge(String fromId, String toId) {
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
    public List<Neighbor> neighborsOf(String id) {
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

        List<Neighbor> out = new ArrayList<>(copy.size());
        for (var e : copy.entrySet()) {
            out.add(new Neighbor(e.getKey(), e.getValue()));
        }

        out.sort(Comparator.comparing(Neighbor::id));
        return out;
    }

    @Override
    public MinPaths shortestPaths(String fromId, String toId) {

        points.findById(fromId);
        points.findById(toId);

        if (fromId.equals(toId)) {
            return new MinPaths(0, List.of(List.of(fromId)));
        }

        Map<String, Map<String, Integer>> graph = deepSnapshot(adj);

        Map<String, Integer> dist = new HashMap<>();
        Map<String, Set<String>> preds = new HashMap<>();
        Set<String> visited = new HashSet<>();
        PriorityQueue<String> pq = new PriorityQueue<>(Comparator.comparingInt(n -> dist.getOrDefault(n, Integer.MAX_VALUE)));

        graph.keySet().forEach(n -> dist.put(n, Integer.MAX_VALUE));
        dist.put(fromId, 0);
        pq.add(fromId);

        while (!pq.isEmpty()) {
            String u = pq.poll();
            if (!visited.add(u)) continue;
            if (u.equals(toId)) break;

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

        int best = dist.getOrDefault(toId, Integer.MAX_VALUE);
        if (best == Integer.MAX_VALUE) {
            throw new IllegalArgumentException(ERR_NO_MIN_PATH);
        }

        List<List<String>> allPaths = new ArrayList<>();
        Deque<String> stack = new ArrayDeque<>();
        stack.push(toId);
        backtrackPaths(fromId, toId, preds, stack, allPaths);

        return new MinPaths(best, allPaths);
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