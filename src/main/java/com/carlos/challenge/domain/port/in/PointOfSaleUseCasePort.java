package com.carlos.challenge.domain.port.in;

import com.carlos.challenge.domain.model.PointOfSale;
import java.util.List;
import java.util.Optional;

public interface PointOfSaleUseCasePort {
    List<PointOfSale> findAll();
    PointOfSale create(String name);
    PointOfSale create(String name, Integer code);
    PointOfSale update(String id, String name);
    void delete(String id);
    PointOfSale findById(String id);
    Optional<PointOfSale> findByName(String name);
    Optional<PointOfSale> findByCode(Integer code);
    String resolveId(String idOrCode);
}
