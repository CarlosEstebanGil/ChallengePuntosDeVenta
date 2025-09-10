package com.carlos.challenge.infrastructure.in.web.controller;

import com.carlos.challenge.domain.model.PointOfSale;
import com.carlos.challenge.domain.model.graph.MinPaths;
import com.carlos.challenge.domain.model.graph.Neighbor;
import com.carlos.challenge.domain.port.in.GraphUseCasePort;
import com.carlos.challenge.domain.port.in.PointOfSaleUseCasePort;
import com.carlos.challenge.infrastructure.in.web.dto.req.EdgeDeleteRequest;
import com.carlos.challenge.infrastructure.in.web.dto.req.EdgeRequest;
import com.carlos.challenge.infrastructure.in.web.dto.resp.MinPathsResponse;
import com.carlos.challenge.infrastructure.in.web.dto.resp.NeighborResponse;
import com.carlos.challenge.infrastructure.in.web.dto.resp.PathDetail;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Tag(name = "Costs", description = "Operations related to graph costs and paths")
@RestController
@RequestMapping(value = "/api/graph/costs", produces = MediaType.APPLICATION_JSON_VALUE)
@Validated
public class CostController {

    private final GraphUseCasePort graph;
    private final PointOfSaleUseCasePort posUseCase;

    public CostController(GraphUseCasePort graph, PointOfSaleUseCasePort posUseCase) {
        this.graph = graph;
        this.posUseCase = posUseCase;
    }

    @Operation(
            summary = "Upsert edge (UUID only)",
            description = "Body must contain UUIDs for 'from' and 'to'.",
            security = @SecurityRequirement(name = "basicAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Edge created or updated"),
            @ApiResponse(responseCode = "400", description = "Validation error", content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    examples = @ExampleObject(value = "{\"code\":\"BAD_REQUEST\",\"message\":\"Validation failed\"}")
            )),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> upsert(
            @Valid @org.springframework.web.bind.annotation.RequestBody EdgeRequest request) {
        graph.upsertEdge(request.from().toString(), request.to().toString(), request.cost());
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Delete edge (UUID only)",
            description = "Body must contain UUIDs for 'from' and 'to'.",
            security = @SecurityRequirement(name = "basicAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Edge deleted"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @DeleteMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(
            @Valid @org.springframework.web.bind.annotation.RequestBody EdgeDeleteRequest request) {
        graph.removeEdge(request.from().toString(), request.to().toString());
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "List neighbors (UUID only)",
            description = "Path variable must be a UUID. Names and codes are enriched from Points of Sale.",
            security = @SecurityRequirement(name = "basicAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Neighbors listed"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping("/neighbors/{fromId}")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<List<NeighborResponse>> neighbors(
            @Parameter(description = "Point of Sale ID (UUID)", required = true)
            @PathVariable UUID fromId
    ) {
        List<Neighbor> neighbors = graph.neighborsOf(fromId.toString());

        List<NeighborResponse> resp = neighbors.stream().map(n -> {
            String name = null;
            Integer code = null;
            try {
                PointOfSale pos = posUseCase.findById(n.id());
                if (pos != null) {
                    name = pos.name();
                    code = pos.code();
                }
            } catch (IllegalArgumentException ignore) {
                // dejamos name/code en null si el POS no existe
            }
            return new NeighborResponse(n.id(), name, n.cost());
        }).toList();

        return ResponseEntity.ok(resp);
    }

    @Operation(
            summary = "Get all minimum cost paths (UUID only)",
            description = "Query params must be UUIDs.",
            security = @SecurityRequirement(name = "basicAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Minimum paths computed"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping("/min-paths")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<MinPathsResponse> minPaths(
            @Parameter(description = "From POS ID (UUID)", required = true) @RequestParam UUID from,
            @Parameter(description = "To POS ID (UUID)", required = true) @RequestParam UUID to
    ) {
        MinPaths mp = graph.shortestPaths(from.toString(), to.toString());

        List<PathDetail> details = new ArrayList<>(mp.paths().size());
        for (List<String> route : mp.paths()) {
            List<String> names = new ArrayList<>(route.size());
            List<Integer> codes = new ArrayList<>(route.size());
            for (String id : route) {
                try {
                    PointOfSale pos = posUseCase.findById(id);
                    names.add(pos != null ? pos.name() : null);
                    codes.add(pos != null ? pos.code() : null);
                } catch (IllegalArgumentException ex) {
                    names.add(null);
                    codes.add(null);
                }
            }
            details.add(new PathDetail(route, names, codes));
        }

        return ResponseEntity.ok(new MinPathsResponse(mp.totalCost(), details));
    }
}
