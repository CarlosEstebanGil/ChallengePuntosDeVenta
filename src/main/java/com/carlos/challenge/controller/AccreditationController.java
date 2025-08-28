package com.carlos.challenge.controller;

import com.carlos.challenge.dto.AccreditationResponse;
import com.carlos.challenge.dto.CreateAccreditationRequest;
import com.carlos.challenge.service.AccreditationService;
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

@RestController
@RequestMapping("/api/acreditaciones")
@Validated
@RequiredArgsConstructor
public class AccreditationController {

    private final AccreditationService service;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public ResponseEntity<AccreditationResponse> crear(@RequestBody @Valid CreateAccreditationRequest req) {
        var created = service.create(req);
        return ResponseEntity
                .created(URI.create("/api/acreditaciones/" + created.id()))
                .body(created);
    }

    @GetMapping
    public ResponseEntity<Page<AccreditationResponse>> listar(
            @RequestParam(required = false) Integer idPuntoVenta,
            @RequestParam(name = "from", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(name = "to", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        PageRequest pr = PageRequest.of(page, size);
        return ResponseEntity.ok(
                service.list(Optional.ofNullable(idPuntoVenta),
                        Optional.ofNullable(from),
                        Optional.ofNullable(to),
                        pr)
        );
    }
}
