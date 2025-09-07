package com.carlos.challenge.service;

import com.carlos.challenge.dto.MinPathsResponse;
import com.carlos.challenge.dto.NeighborResponse;

import java.util.List;

public interface GraphService {

    void upsertEdge(String fromId, String toId, int cost);

    void removeEdge(String fromId, String toId);

    List<NeighborResponse> neighborsOf(String fromId);

    MinPathsResponse shortestPaths(String fromId, String toId);
}
