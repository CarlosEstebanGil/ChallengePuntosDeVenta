package com.carlos.challenge.controller;

import com.carlos.challenge.dto.CreatePointRequest;
import com.carlos.challenge.dto.UpdatePointRequest;
import com.carlos.challenge.model.PointOfSale;
import com.carlos.challenge.service.PointCacheService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@Tag(name = "Points of Sale", description = "Operations related to points of sale")
@RestController
@RequestMapping("/api/pos")
@Validated
@RequiredArgsConstructor
public class PointOfSaleController {

    private final PointCacheService service;

    @Operation(
            summary = "List all points of sale",
            description = "Returns a list of all points of sale.",
            security = @SecurityRequirement(name = "basicAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successful operation"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping
    public ResponseEntity<List<PointOfSale>> list() {
        return ResponseEntity.ok(service.findAll());
    }

    @Operation(
            summary = "Create a new point of sale",
            description = "Creates a new point of sale. Requires ADMIN role.",
            security = @SecurityRequirement(name = "basicAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Point of sale created"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PostMapping
    public ResponseEntity<PointOfSale> create(
            @RequestBody(
                    description = "Point of sale creation request",
                    required = true
            )
            @org.springframework.web.bind.annotation.RequestBody @Valid CreatePointRequest req) {
        PointOfSale created = service.create(req.id(), req.name());
        return ResponseEntity.created(URI.create("/api/pos/" + created.id())).body(created);
    }

    @Operation(
            summary = "Update a point of sale",
            description = "Updates the name of a point of sale. Requires ADMIN role.",
            security = @SecurityRequirement(name = "basicAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Point of sale updated"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Point of sale not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<PointOfSale> update(
            @Parameter(description = "Point of sale ID", required = true)
            @PathVariable Integer id,
            @RequestBody(
                    description = "Point of sale update request",
                    required = true
            )
            @org.springframework.web.bind.annotation.RequestBody @Valid UpdatePointRequest req) {
        return ResponseEntity.ok(service.update(id, req.name()));
    }

    @Operation(
            summary = "Delete a point of sale",
            description = "Deletes a point of sale by ID. Requires ADMIN role.",
            security = @SecurityRequirement(name = "basicAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Point of sale deleted"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Point of sale not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "Point of sale ID", required = true)
            @PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Get a point of sale by ID",
            description = "Returns a point of sale by its ID.",
            security = @SecurityRequirement(name = "basicAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successful operation"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Point of sale not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<PointOfSale> findById(
            @Parameter(description = "Point of sale ID", required = true)
            @PathVariable Integer id) {
        return ResponseEntity.ok(service.findById(id));
    }
}