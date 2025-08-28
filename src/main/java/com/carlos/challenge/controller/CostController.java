package com.carlos.challenge.controller;

import com.carlos.challenge.dto.EdgeRequest;
import com.carlos.challenge.dto.MinPathResponse;
import com.carlos.challenge.dto.NeighborResponse;
import com.carlos.challenge.service.GraphService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/costs")
@Validated
@RequiredArgsConstructor
public class CostController {

    private final GraphService graph;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> upsert(@RequestBody @Valid EdgeRequest req) {
        graph.upsertEdge(req.fromId(), req.toId(), req.costo());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@RequestBody @Valid EdgeRequest req) {
        graph.removeEdge(req.fromId(), req.toId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/neighbors/{fromId}")
    public ResponseEntity<List<NeighborResponse>> neighbors(@PathVariable Integer fromId) {
        return ResponseEntity.ok(graph.neighborsOf(fromId));
    }

    @GetMapping("/min-path")
    public ResponseEntity<MinPathResponse> minPath(@RequestParam Integer from,
                                                   @RequestParam Integer to) {
        return ResponseEntity.ok(graph.shortestPath(from, to));
    }
}
