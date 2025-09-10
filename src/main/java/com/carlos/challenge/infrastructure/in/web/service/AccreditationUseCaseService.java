package com.carlos.challenge.infrastructure.in.web.service;

import com.carlos.challenge.domain.model.Accreditation;
import com.carlos.challenge.domain.port.in.AccreditationUseCasePort;
import com.carlos.challenge.domain.port.out.AccreditationRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.Instant;

@Service
@RequiredArgsConstructor
@Transactional
public class AccreditationUseCaseService implements AccreditationUseCasePort {

    public static final String ACCREDITATION_NOT_FOUND = "Accreditation not found: ";
    private final AccreditationRepositoryPort repository;

    @Override
    public Accreditation create(BigDecimal amount, String pointOfSaleId) {
        Accreditation acc = new Accreditation(
                null,
                amount,
                pointOfSaleId,
                null,
                java.time.Instant.now()
        );
        return repository.save(acc);
    }


    @Override
    @Transactional(readOnly = true)
    public Accreditation findById(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(ACCREDITATION_NOT_FOUND + id));
    }

    @Override
    public void delete(String id) {
        repository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Accreditation> findAll(Pageable pageable) {
        return repository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Accreditation> findByPointOfSale(String pointOfSaleId, Pageable pageable) {
        return repository.findByPointOfSale(pointOfSaleId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Accreditation> findByDateBetween(Instant from, Instant to, Pageable pageable) {
        return repository.findByDateBetween(from, to, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Accreditation> findByPointOfSaleAndDateBetween(String pointOfSaleId, Instant from, Instant to, Pageable pageable) {
        return repository.findByPointOfSaleAndDateBetween(pointOfSaleId, from, to, pageable);
    }
}
