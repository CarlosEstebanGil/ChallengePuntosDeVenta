package com.carlos.challenge.service;

import com.carlos.challenge.model.PuntoVenta;

import java.util.List;

public interface PointCacheService {
    List<PuntoVenta> findAll();

    PuntoVenta create(Integer id, String nombre);

    PuntoVenta update(Integer id, String nombre);

    void delete(Integer id);

    PuntoVenta findById(Integer id);
}
