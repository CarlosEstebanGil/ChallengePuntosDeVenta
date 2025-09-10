package com.carlos.challenge.domain.port.out;

import java.util.Map;

public interface GraphRepositoryPort {
    void upsertEdge(String fromId, String toId, int cost);
    void removeEdge(String fromId, String toId);
    Map<String, Integer> neighborsOf(String fromId);
}
