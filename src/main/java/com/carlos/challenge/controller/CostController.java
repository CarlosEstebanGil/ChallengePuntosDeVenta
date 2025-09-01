package com.carlos.challenge.controller;

import com.carlos.challenge.dto.EdgeRequest;
import com.carlos.challenge.dto.MinPathResponse;
import com.carlos.challenge.dto.NeighborResponse;
import com.carlos.challenge.service.GraphService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Tag(name = "Costs", description = "Operations related to graph costs and paths")
@RestController
@RequestMapping("/api/costs")
@Validated
@RequiredArgsConstructor
public class CostController {

    private final GraphService graph;

    @Operation(
            summary = "Create or update an edge cost",
            description = "Creates or updates the cost between two nodes. Requires ADMIN role.",
            security = @SecurityRequirement(name = "basicAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Edge upserted successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> upsert(
            @RequestBody(
                    description = "Edge creation or update request",
                    required = true
            )
            @org.springframework.web.bind.annotation.RequestBody @Valid EdgeRequest req) {
        graph.upsertEdge(req.fromId(), req.toId(), req.cost());
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Delete an edge",
            description = "Deletes the edge between two nodes. Requires ADMIN role.",
            security = @SecurityRequirement(name = "basicAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Edge deleted successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @DeleteMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(
            @RequestBody(
                    description = "Edge deletion request",
                    required = true
            )
            @org.springframework.web.bind.annotation.RequestBody @Valid EdgeRequest req) {
        graph.removeEdge(req.fromId(), req.toId());
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "List neighbors of a node",
            description = "Returns the neighbors and costs for a given node.",
            security = @SecurityRequirement(name = "basicAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successful operation"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Node not found")
    })
    @GetMapping("/neighbors/{fromId}")
    public ResponseEntity<List<NeighborResponse>> neighbors(
            @Parameter(description = "Source node ID", required = true)
            @PathVariable Integer fromId) {
        return ResponseEntity.ok(graph.neighborsOf(fromId));
    }

    @Operation(
            summary = "Get minimum cost path",
            description = "Returns the minimum cost path between two nodes.",
            security = @SecurityRequirement(name = "basicAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successful operation"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Path not found")
    })
// In CostController.java
    @GetMapping("/min-path")
    public ResponseEntity<MinPathResponse> minPath(
            @RequestParam Integer from,
            @RequestParam Integer to) {
        MinPathResponse path = graph.shortestPath(from, to);
        if (path == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Path not found");
        }
        return ResponseEntity.ok(path);
    }
}