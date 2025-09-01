package com.carlos.challenge.repository;

import com.carlos.challenge.model.Accreditation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.Instant;

public interface AccreditationRepository extends MongoRepository<Accreditation, String> {

    Page<Accreditation> findBypointOfSaleId(Integer pointOfSaleId, Pageable pageable);

    Page<Accreditation> findByreceptionDateBetween(Instant from, Instant to, Pageable pageable);

    Page<Accreditation> findBypointOfSaleIdAndReceptionDateBetween(Integer pointOfSaleId, Instant from, Instant to, Pageable pageable);
}
