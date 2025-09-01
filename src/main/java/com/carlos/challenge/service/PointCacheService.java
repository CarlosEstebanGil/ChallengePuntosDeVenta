package com.carlos.challenge.service;

import com.carlos.challenge.model.PointOfSale;

import java.util.List;

public interface PointCacheService {
    List<PointOfSale> findAll();

    PointOfSale create(Integer id, String name);

    PointOfSale update(Integer id, String name);

    void delete(Integer id);

    PointOfSale findById(Integer id);
}
