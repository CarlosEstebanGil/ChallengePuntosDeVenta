package com.carlos.challenge.controller;

import com.carlos.challenge.dto.EdgeDeleteRequest;
import com.carlos.challenge.dto.EdgeRequest;
import com.carlos.challenge.dto.MinPathsResponse;
import com.carlos.challenge.dto.NeighborResponse;
import com.carlos.challenge.service.GraphService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@Tag(name = "Costs", description = "Operations related to graph costs and paths")
@RestController
@RequestMapping("/api/graph/costs")
@Validated
@RequiredArgsConstructor
public class CostController {

    private final GraphService graph;

    @Operation(
            summary = "Create or update an edge cost (UUID only)",
            description = "Creates or updates the cost between two nodes. Both IDs must be UUIDs.",
            security = @SecurityRequirement(name = "basicAuth")
    )
    @ApiResponse(responseCode = "204", description = "Edge upserted successfully")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> upsert(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(examples = @ExampleObject(
                            name = "Using UUIDs",
                            value = """
                                    {
                                      "fromId": "6c0d4f6e-8d7c-4e60-9c0c-2f2c9a5e1a10",
                                      "toId":   "9f1a2b3c-4d5e-6f70-8a9b-01c2d3e4f5a6",
                                      "cost":   5
                                    }
                                    """
                    ))
            )
            @org.springframework.web.bind.annotation.RequestBody @Valid EdgeRequest req) {

        graph.upsertEdge(req.fromId().toString(), req.toId().toString(), req.cost());
        return ResponseEntity.noContent().build();
    }


    @Operation(
            summary = "Delete an edge (UUID only)",
            description = "Deletes the edge between two nodes. Both IDs must be UUIDs.",
            security = @SecurityRequirement(name = "basicAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Edge deleted successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Node or edge not found")
    })
    @DeleteMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(examples = @ExampleObject(
                            name = "Using UUIDs",
                            value = """
                                {
                                  "fromId": "6c0d4f6e-8d7c-4e60-9c0c-2f2c9a5e1a10",
                                  "toId":   "9f1a2b3c-4d5e-6f70-8a9b-01c2d3e4f5a6"
                                }
                                """
                    ))
            )
            @org.springframework.web.bind.annotation.RequestBody @jakarta.validation.Valid EdgeDeleteRequest req
    ) {
        graph.removeEdge(req.fromId().toString(), req.toId().toString());
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "List neighbors of a node (UUID only)",
            description = "Path variable must be a UUID.",
            security = @SecurityRequirement(name = "basicAuth")
    )
    @GetMapping("/neighbors/{fromId}")
    public ResponseEntity<List<NeighborResponse>> neighbors(@PathVariable UUID fromId) {
        return ResponseEntity.ok(graph.neighborsOf(fromId.toString()));
    }

    @Operation(
            summary = "Get all minimum cost paths (UUID only)",
            description = "Query params must be UUIDs.",
            security = @SecurityRequirement(name = "basicAuth")
    )
    @GetMapping("/min-paths")
    public ResponseEntity<MinPathsResponse> minPaths(@RequestParam UUID from, @RequestParam UUID to) {
        return ResponseEntity.ok(graph.shortestPaths(from.toString(), to.toString()));
    }
}
