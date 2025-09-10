package com.carlos.challenge.domain.port.out;

import com.carlos.challenge.domain.model.Accreditation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.Optional;

public interface AccreditationRepositoryPort {

    Accreditation save(Accreditation accreditation);

    Optional<Accreditation> findById(String id);

    void deleteById(String id);

    Page<Accreditation> findAll(Pageable pageable);

    Page<Accreditation> findByPointOfSale(String pointOfSaleId, Pageable pageable);

    Page<Accreditation> findByDateBetween(Instant from, Instant to, Pageable pageable);

    Page<Accreditation> findByPointOfSaleAndDateBetween(String pointOfSaleId, Instant from, Instant to, Pageable pageable);
}
