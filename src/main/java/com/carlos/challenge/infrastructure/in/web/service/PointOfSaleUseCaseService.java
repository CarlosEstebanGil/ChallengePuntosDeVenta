package com.carlos.challenge.infrastructure.in.web.service;

import com.carlos.challenge.domain.model.PointOfSale;
import com.carlos.challenge.domain.port.in.PointOfSaleUseCasePort;
import com.carlos.challenge.domain.port.out.PointOfSaleRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;


@Service
@Transactional
public class PointOfSaleUseCaseService implements PointOfSaleUseCasePort {

    public static final String POINT_OF_SALE_NOT_FOUND = "Point of sale not found: ";
    private final PointOfSaleRepositoryPort repositoryPort;

    public PointOfSaleUseCaseService(PointOfSaleRepositoryPort repositoryPort) {
        this.repositoryPort = repositoryPort;
    }

    @Override
    public List<PointOfSale> findAll() {
        return repositoryPort.findAll();
    }

    @Override
    public PointOfSale create(String name) {
        return repositoryPort.save(new PointOfSale(null, name, null));
    }

    @Override
    public PointOfSale create(String name, Integer code) {
        return repositoryPort.save(new PointOfSale(null, name, code));
    }

    @Override
    public PointOfSale update(String id, String name) {
        return repositoryPort.save(new PointOfSale(id, name, repositoryPort.findById(id).map(PointOfSale::code).orElse(null)));
    }

    @Override
    public void delete(String id) {
        repositoryPort.deleteById(id);
    }


    @Override
    @Transactional(readOnly = true)
    public PointOfSale findById(String id) {
        return repositoryPort.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(POINT_OF_SALE_NOT_FOUND + id));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PointOfSale> findByName(String name) {
        return repositoryPort.findByName(name);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PointOfSale> findByCode(Integer code) {
        return repositoryPort.findByCode(code);
    }

    @Override
    @Transactional(readOnly = true)
    public String resolveId(String idOrCode) {
        return repositoryPort.resolveId(idOrCode);
    }
}
