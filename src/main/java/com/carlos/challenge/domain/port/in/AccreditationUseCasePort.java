package com.carlos.challenge.domain.port.in;

import com.carlos.challenge.domain.model.Accreditation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.Instant;

public interface AccreditationUseCasePort {

    Accreditation create(BigDecimal amount, String pointOfSaleId);

    Accreditation findById(String id);

    void delete(String id);

    Page<Accreditation> findAll(Pageable pageable);

    Page<Accreditation> findByPointOfSale(String pointOfSaleId, Pageable pageable);

    Page<Accreditation> findByDateBetween(Instant from, Instant to, Pageable pageable);

    Page<Accreditation> findByPointOfSaleAndDateBetween(String pointOfSaleId, Instant from, Instant to, Pageable pageable);
}
