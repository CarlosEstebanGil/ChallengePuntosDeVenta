package com.carlos.challenge.service;

import com.carlos.challenge.model.PuntoVenta;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import static org.springframework.http.HttpStatus.*;

@Service
public class PointCacheServiceImpl implements PointCacheService {

    public static final String EL_PUNTO_CON_ID_D_YA_EXISTE = "El punto con id %d ya existe";
    public static final String NO_EXISTE_EL_PUNTO_CON_ID_D = "No existe el punto con id %d";
    public static final String NO_EXISTE_EL_PUNTO_CON_ID_D1 = "No existe el punto con id %d";

    private final Map<Integer, String> points = new ConcurrentHashMap<>();

    @Override
    public List<PuntoVenta> findAll() {
        return points.entrySet().stream()
                .map(e -> new PuntoVenta(e.getKey(), e.getValue()))
                .sorted((a,b) -> a.id().compareTo(b.id()))
                .toList();
    }

    @Override
    public PuntoVenta create(Integer id, String nombre) {
        String prev = points.putIfAbsent(id, nombre);
        if (prev != null) {
            throw new ResponseStatusException(CONFLICT, EL_PUNTO_CON_ID_D_YA_EXISTE.formatted(id));
        }
        return new PuntoVenta(id, nombre);
    }

    @Override
    public PuntoVenta update(Integer id, String nombre) {
        String nuevo = points.compute(id, (k, v) -> {
            if (v == null) {
                throw new ResponseStatusException(NOT_FOUND, NO_EXISTE_EL_PUNTO_CON_ID_D.formatted(id));
            }
            return nombre;
        });
        return new PuntoVenta(id, nuevo);
    }

    @Override
    public void delete(Integer id) {
        String removed = points.remove(id);
        if (removed == null) {
            throw new ResponseStatusException(NOT_FOUND, NO_EXISTE_EL_PUNTO_CON_ID_D1.formatted(id));
        }
    }

    @Override
    public PuntoVenta findById(Integer id) {
        String nombre = points.get(id);
        if (nombre == null) {
            throw new ResponseStatusException(NOT_FOUND, NO_EXISTE_EL_PUNTO_CON_ID_D.formatted(id));
        }
        return new PuntoVenta(id, nombre);
    }
}