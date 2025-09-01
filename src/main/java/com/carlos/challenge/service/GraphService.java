package com.carlos.challenge.service;

import com.carlos.challenge.dto.MinPathResponse;
import com.carlos.challenge.dto.NeighborResponse;

import java.util.List;

public interface GraphService {

    void upsertEdge(int fromId, int toId, int cost);

    void removeEdge(int fromId, int toId);

    List<NeighborResponse> neighborsOf(int fromId);

    MinPathResponse shortestPath(int fromId, int toId);
}
