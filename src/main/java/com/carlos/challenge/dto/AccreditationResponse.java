package com.carlos.challenge.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record AccreditationResponse(
        String id,
        BigDecimal importe,
        Integer idPuntoVenta,
        String nombrePuntoVenta,
        Instant fechaRecepcion
) {}
