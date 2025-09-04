package com.carlos.challenge.controller;

import com.carlos.challenge.dto.AccreditationResponse;
import com.carlos.challenge.dto.CreateAccreditationRequest;
import com.carlos.challenge.service.AccreditationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.time.Instant;
import java.util.Optional;

@Tag(name = "Accreditations", description = "Operations related to accreditations")
@RestController
@RequestMapping("/api/accreditations")
@Validated
@RequiredArgsConstructor
public class AccreditationController {

    public static final String API_ACCREDITATIONS = "/api/accreditations/";
    private final AccreditationService service;

    @Operation(
            summary = "Create a new accreditation",
            description = "Creates a new accreditation record. Requires ADMIN or USER role.",
            security = @SecurityRequirement(name = "basicAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Accreditation created"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public ResponseEntity<AccreditationResponse> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Accreditation creation request",
                    required = true
            )
            @RequestBody @Valid CreateAccreditationRequest req) {
        var created = service.create(req);
        return ResponseEntity
                .created(URI.create(API_ACCREDITATIONS + created.id()))
                .body(created);
    }

    @Operation(
            summary = "List accreditations",
            description = "Returns a paginated list of accreditations, optionally filtered by point of sale and date range.",
            security = @SecurityRequirement(name = "basicAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successful operation"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping
    public ResponseEntity<Page<AccreditationResponse>> list(
            @RequestParam(required = false) String pointOfSaleId, // <-- String (antes Integer)
            @RequestParam(name = "from", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(name = "to", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        PageRequest pr = PageRequest.of(page, size);
        return ResponseEntity.ok(
                service.list(
                        Optional.ofNullable(pointOfSaleId), // <-- Optional<String>
                        Optional.ofNullable(from),
                        Optional.ofNullable(to),
                        pr
                )
        );
    }

    @Operation(
            summary = "Delete an accreditation",
            description = "Deletes an accreditation by its ID. Requires ADMIN role.",
            security = @SecurityRequirement(name = "basicAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Accreditation deleted"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(
            @Parameter(description = "Accreditation ID", required = true)
            @PathVariable String id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Get accreditation by ID",
            description = "Returns an accreditation by its ID. Requires USER or ADMIN role.",
            security = @SecurityRequirement(name = "basicAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Accreditation found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<AccreditationResponse> getById(
            @Parameter(description = "Accreditation ID", required = true)
            @PathVariable String id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
