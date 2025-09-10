package com.carlos.challenge.infrastructure.out.persistence.mongo.adapter;

import com.carlos.challenge.domain.model.Accreditation;
import com.carlos.challenge.domain.port.out.AccreditationRepositoryPort;
import com.carlos.challenge.infrastructure.out.persistence.mongo.entity.AccreditationDocument;
import com.carlos.challenge.infrastructure.out.persistence.mongo.mapper.AccreditationMongoMapper;
import com.carlos.challenge.infrastructure.out.persistence.mongo.repository.AccreditationMongoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import java.time.Instant;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AccreditationMongoAdapter implements AccreditationRepositoryPort {

    private final AccreditationMongoRepository repository;
    private final AccreditationMongoMapper mapper;

    @Override
    public Accreditation save(Accreditation accreditation) {
        AccreditationDocument saved = repository.save(mapper.toDocument(accreditation));
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Accreditation> findById(String id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public void deleteById(String id) {
        repository.deleteById(id);
    }

    @Override
    public Page<Accreditation> findAll(Pageable pageable) {
        return repository.findAll(pageable).map(mapper::toDomain);
    }

    @Override
    public Page<Accreditation> findByPointOfSale(String pointOfSaleId, Pageable pageable) {
        return repository.findByPointOfSaleId(pointOfSaleId, pageable).map(mapper::toDomain);
    }

    @Override
    public Page<Accreditation> findByDateBetween(Instant from, Instant to, Pageable pageable) {
        return repository.findByReceptionDateBetween(from, to, pageable).map(mapper::toDomain);
    }

    @Override
    public Page<Accreditation> findByPointOfSaleAndDateBetween(String pointOfSaleId, Instant from, Instant to, Pageable pageable) {
        return repository.findByPointOfSaleIdAndReceptionDateBetween(pointOfSaleId, from, to, pageable)
                .map(mapper::toDomain);
    }
}
