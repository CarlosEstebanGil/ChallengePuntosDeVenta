package com.carlos.challenge.repository;

import com.carlos.challenge.model.Accreditation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.Instant;

public interface AccreditationRepository extends MongoRepository<Accreditation, String> {

    Page<Accreditation> findByIdPuntoVenta(Integer idPuntoVenta, Pageable pageable);

    Page<Accreditation> findByFechaRecepcionBetween(Instant from, Instant to, Pageable pageable);

    Page<Accreditation> findByIdPuntoVentaAndFechaRecepcionBetween(Integer idPuntoVenta, Instant from, Instant to, Pageable pageable);
}
