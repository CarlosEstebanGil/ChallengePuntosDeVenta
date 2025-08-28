package com.carlos.challenge.controller;

import com.carlos.challenge.dto.CreatePointRequest;
import com.carlos.challenge.dto.UpdatePointRequest;
import com.carlos.challenge.model.PointOfSale;
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
public class PointOfSaleController {

    private final PointCacheService service;

    @GetMapping
    public ResponseEntity<List<PointOfSale>> list() {
        return ResponseEntity.ok(service.findAll());
    }

    @PostMapping
    public ResponseEntity<PointOfSale> create(@RequestBody @Valid CreatePointRequest req) {
        PointOfSale created = service.create(req.id(), req.nombre());
        return ResponseEntity.created(URI.create("/api/pos/" + created.id())).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PointOfSale> update(@PathVariable Integer id,
                                              @RequestBody @Valid UpdatePointRequest req) {
        return ResponseEntity.ok(service.update(id, req.nombre()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<PointOfSale> findById(@PathVariable Integer id) {
        return ResponseEntity.ok(service.findById(id));
    }

}
