package com.carlos.challenge.domain.model;

import java.math.BigDecimal;
import java.time.Instant;

public record Accreditation(
        String id,
        BigDecimal amount,
        String pointOfSaleId,
        String pointOfSaleName,
        Instant receptionDate
) {}
