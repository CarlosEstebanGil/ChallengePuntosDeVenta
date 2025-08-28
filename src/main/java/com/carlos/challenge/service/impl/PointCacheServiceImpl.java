package com.carlos.challenge.service.impl;

import com.carlos.challenge.model.PointOfSale;
import com.carlos.challenge.service.PointCacheService;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import static org.springframework.http.HttpStatus.*;

@Service
public class PointCacheServiceImpl implements PointCacheService {

    public static final String EL_PUNTO_CON_ID_D_YA_EXISTE = "El punto con id %d ya existe";
    public static final String NO_EXISTE_EL_PUNTO_CON_ID_D = "No existe el punto con id %d";
    public static final String NO_EXISTE_EL_PUNTO_CON_ID_D1 = "No existe el punto con id %d";

    private final Map<Integer, String> points = new ConcurrentHashMap<>();

    @Override
    public List<PointOfSale> findAll() {
        return points.entrySet().stream()
                .map(e -> new PointOfSale(e.getKey(), e.getValue()))
                .sorted((a,b) -> a.id().compareTo(b.id()))
                .toList();
    }

    @Override
    public PointOfSale create(Integer id, String nombre) {
        String prev = points.putIfAbsent(id, nombre);
        if (prev != null) {
            throw new ResponseStatusException(CONFLICT, EL_PUNTO_CON_ID_D_YA_EXISTE.formatted(id));
        }
        return new PointOfSale(id, nombre);
    }

    @Override
    public PointOfSale update(Integer id, String nombre) {
        return Optional.ofNullable(points.computeIfPresent(id, (k, v) -> nombre))
                .map(n -> new PointOfSale(id, n))
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, NO_EXISTE_EL_PUNTO_CON_ID_D.formatted(id)));
    }

    @Override
    public void delete(Integer id) {
        String removed = points.remove(id);
        if (removed == null) {
            throw new ResponseStatusException(NOT_FOUND, NO_EXISTE_EL_PUNTO_CON_ID_D1.formatted(id));
        }
    }

    @Override
    public PointOfSale findById(Integer id) {
        String nombre = points.get(id);
        if (nombre == null) {
            throw new ResponseStatusException(NOT_FOUND, NO_EXISTE_EL_PUNTO_CON_ID_D.formatted(id));
        }
        return new PointOfSale(id, nombre);
    }
}