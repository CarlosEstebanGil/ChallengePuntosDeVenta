package com.carlos.challenge.domain.port.in;


import com.carlos.challenge.domain.model.graph.MinPaths;
import com.carlos.challenge.domain.model.graph.Neighbor;

import java.util.List;

public interface GraphUseCasePort {
    void upsertEdge(String fromId, String toId, int cost);
    void removeEdge(String fromId, String toId);
    List<Neighbor> neighborsOf(String fromId);
    MinPaths shortestPaths(String fromId, String toId);
}
