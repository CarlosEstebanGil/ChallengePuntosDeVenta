package com.carlos.challenge.repository;

import com.carlos.challenge.model.Accreditation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.Instant;

public interface AccreditationRepository extends MongoRepository<Accreditation, String> {
    Page<Accreditation> findByPointOfSaleId(String pointOfSaleId, Pageable pageable);

    Page<Accreditation> findByPointOfSaleIdAndReceptionDateBetween(String pointOfSaleId, Instant from, Instant to, Pageable pageable);

    Page<Accreditation> findByReceptionDateBetween(Instant from, Instant to, Pageable pageable);
}
