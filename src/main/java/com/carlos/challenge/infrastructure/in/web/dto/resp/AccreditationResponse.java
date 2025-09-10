package com.carlos.challenge.infrastructure.in.web.dto.resp;

import java.math.BigDecimal;
import java.time.Instant;

public record AccreditationResponse(
        String id,
        BigDecimal amount,
        String pointOfSaleId,
        String pointOfSaleName,
        Instant receptionDate
) {}
