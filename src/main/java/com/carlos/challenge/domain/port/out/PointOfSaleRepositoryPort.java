package com.carlos.challenge.domain.port.out;

import com.carlos.challenge.domain.model.PointOfSale;
import java.util.List;
import java.util.Optional;

public interface PointOfSaleRepositoryPort {
    List<PointOfSale> findAll();
    PointOfSale save(PointOfSale point);
    void deleteById(String id);
    Optional<PointOfSale> findById(String id);
    Optional<PointOfSale> findByName(String name);
    Optional<PointOfSale> findByCode(Integer code);
    String resolveId(String idOrCode);
}
