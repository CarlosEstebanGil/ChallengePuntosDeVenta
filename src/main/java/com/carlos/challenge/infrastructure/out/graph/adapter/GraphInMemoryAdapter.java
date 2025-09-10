package com.carlos.challenge.infrastructure.out.graph.adapter;

import com.carlos.challenge.domain.port.out.GraphRepositoryPort;
import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class GraphInMemoryAdapter implements GraphRepositoryPort {

    public static final String FROM_ID_TO_ID_CANNOT_BE_NULL = "fromId/toId cannot be null";
    public static final String LOOPS_ARE_NOT_ALLOWED = "Loops are not allowed: ";

    private final Map<String, Map<String, Integer>> adj = new ConcurrentHashMap<>();

    @Override
    public void upsertEdge(String fromId, String toId, int cost) {
        if (fromId == null || toId == null) throw new IllegalArgumentException(FROM_ID_TO_ID_CANNOT_BE_NULL);
        if (fromId.equals(toId)) throw new IllegalArgumentException(LOOPS_ARE_NOT_ALLOWED + fromId);
        put(fromId, toId, cost);
        put(toId, fromId, cost);
    }

    @Override
    public void removeEdge(String fromId, String toId) {
        remove(fromId, toId);
        remove(toId, fromId);
    }

    @Override
    public Map<String, Integer> neighborsOf(String fromId) {
        var m = adj.get(fromId);
        return m == null ? Map.of() : Map.copyOf(m);
    }

    private void put(String a, String b, int cost) {
        adj.computeIfAbsent(a, k -> new ConcurrentHashMap<>()).put(b, cost);
    }

    private void remove(String a, String b) {
        var m = adj.get(a);
        if (m != null) {
            m.remove(b);
            if (m.isEmpty()) adj.remove(a);
        }
    }
}
