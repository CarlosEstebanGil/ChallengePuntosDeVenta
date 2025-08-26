package com.carlos.challenge.controller;

import com.carlos.challenge.dto.CreatePuntoRequest;
import com.carlos.challenge.dto.UpdatePuntoRequest;
import com.carlos.challenge.model.PuntoVenta;
import com.carlos.challenge.service.PointCacheService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/pos")
@Validated
@RequiredArgsConstructor
public class PuntoVentaController {

    private final PointCacheService service;

    @GetMapping
    public ResponseEntity<List<PuntoVenta>> list() {
        return ResponseEntity.ok(service.findAll());
    }

    @PostMapping
    public ResponseEntity<PuntoVenta> create(@RequestBody @Valid CreatePuntoRequest req) {
        PuntoVenta created = service.create(req.id(), req.nombre());
        return ResponseEntity.created(URI.create("/api/pos/" + created.id())).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PuntoVenta> update(@PathVariable Integer id,
                                             @RequestBody @Valid UpdatePuntoRequest req) {
        return ResponseEntity.ok(service.update(id, req.nombre()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<PuntoVenta> findById(@PathVariable Integer id) {
        return ResponseEntity.ok(service.findById(id));
    }

}
