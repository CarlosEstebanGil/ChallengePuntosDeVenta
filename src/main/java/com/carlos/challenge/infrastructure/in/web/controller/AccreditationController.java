package com.carlos.challenge.infrastructure.in.web.controller;

import com.carlos.challenge.domain.model.Accreditation;
import com.carlos.challenge.domain.port.in.AccreditationUseCasePort;
import com.carlos.challenge.infrastructure.in.web.dto.req.CreateAccreditationRequest;
import com.carlos.challenge.infrastructure.in.web.dto.resp.AccreditationResponse;
import com.carlos.challenge.infrastructure.in.web.mapper.AccreditationWebMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.bind.annotation.RequestBody;

import java.net.URI;
import java.time.Instant;

@Tag(name = "accreditations")
@RestController
@RequestMapping(value = "/api/accreditations", produces = MediaType.APPLICATION_JSON_VALUE)
public class AccreditationController {

    private final AccreditationUseCasePort useCase;
    private final AccreditationWebMapper mapper;

    public AccreditationController(AccreditationUseCasePort useCase, AccreditationWebMapper mapper) {
        this.useCase = useCase;
        this.mapper = mapper;
    }

    @Operation(
            summary = "Create an accreditation",
            security = @SecurityRequirement(name = "basicAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Accreditation created"),
            @ApiResponse(responseCode = "400", description = "Validation error", content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    examples = @ExampleObject(value = "{\"code\":\"BAD_REQUEST\",\"errors\":{\"amount\":\"must not be null\"}}")
            )),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AccreditationResponse> create(
            @Valid @RequestBody CreateAccreditationRequest request
    ) {
        Accreditation saved = useCase.create(request.amount(), request.pointOfSaleId());
        return ResponseEntity.created(URI.create("/api/accreditations/" + saved.id()))
                .body(mapper.toResponse(saved));
    }

    @Operation(
            summary = "Get accreditation by id",
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
            @Parameter(description = "Accreditation ID", required = true) @PathVariable String id
    ) {
        Accreditation acc = useCase.findById(id); // IllegalArgumentException → 404 via ApiExceptionHandler
        return ResponseEntity.ok(mapper.toResponse(acc));
    }

    @Operation(
            summary = "Delete accreditation by id",
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
            @Parameter(description = "Accreditation ID", required = true) @PathVariable String id
    ) {
        useCase.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "List accreditations by Point of Sale (paged)",
            security = @SecurityRequirement(name = "basicAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Page of accreditations"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping("/by-pos/{pointOfSaleId}")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<Page<AccreditationResponse>> byPos(
            @Parameter(description = "Point of Sale ID (UUID)", required = true) @PathVariable String pointOfSaleId,
            @PageableDefault @Parameter(hidden = true) Pageable pageable
    ) {
        Page<AccreditationResponse> page = useCase.findByPointOfSale(pointOfSaleId, pageable)
                .map(mapper::toResponse);
        return ResponseEntity.ok(page);
    }

    @Operation(
            summary = "List accreditations by date range (paged, UTC ISO-8601)",
            security = @SecurityRequirement(name = "basicAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Page of accreditations"),
            @ApiResponse(responseCode = "400", description = "Validation error", content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    examples = @ExampleObject(value = "{\"code\":\"BAD_REQUEST\",\"message\":\"Validation failed\"}")
            )),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping("/by-date")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<Page<AccreditationResponse>> byDate(
            @Parameter(description = "From (inclusive). ISO-8601 UTC, e.g. 2025-09-01T00:00:00Z", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @Parameter(description = "To (inclusive). ISO-8601 UTC, e.g. 2025-09-30T23:59:59Z", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @PageableDefault @Parameter(hidden = true) Pageable pageable
    ) {
        Page<AccreditationResponse> page = useCase.findByDateBetween(from, to, pageable)
                .map(mapper::toResponse);
        return ResponseEntity.ok(page);
    }

    @Operation(
            summary = "List accreditations by Point of Sale and date range (paged)",
            security = @SecurityRequirement(name = "basicAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Page of accreditations"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping("/by-pos-and-date")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<Page<AccreditationResponse>> byPosAndDate(
            @Parameter(description = "Point of Sale ID (UUID)", required = true)
            @RequestParam String pointOfSaleId,
            @Parameter(description = "From (inclusive). ISO-8601 UTC", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @Parameter(description = "To (inclusive). ISO-8601 UTC", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @PageableDefault @Parameter(hidden = true) Pageable pageable
    ) {
        Page<AccreditationResponse> page = useCase.findByPointOfSaleAndDateBetween(pointOfSaleId, from, to, pageable)
                .map(mapper::toResponse);
        return ResponseEntity.ok(page);
    }

    @Operation(
            summary = "List accreditations (paged)",
            security = @SecurityRequirement(name = "basicAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Page of accreditations"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<Page<AccreditationResponse>> list(
            @PageableDefault @Parameter(hidden = true) Pageable pageable
    ) {
        Page<AccreditationResponse> page = useCase.findAll(pageable).map(mapper::toResponse);
        return ResponseEntity.ok(page);
    }
}
