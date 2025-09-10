package com.carlos.challenge.infrastructure.out.persistence.mongo.repository;

import com.carlos.challenge.infrastructure.out.persistence.mongo.entity.AccreditationDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.Instant;

public interface AccreditationMongoRepository extends MongoRepository<AccreditationDocument, String> {
    Page<AccreditationDocument> findByPointOfSaleId(String pointOfSaleId, Pageable pageable);

    Page<AccreditationDocument> findByPointOfSaleIdAndReceptionDateBetween(String pointOfSaleId, Instant from, Instant to, Pageable pageable);

    Page<AccreditationDocument> findByReceptionDateBetween(Instant from, Instant to, Pageable pageable);

}
