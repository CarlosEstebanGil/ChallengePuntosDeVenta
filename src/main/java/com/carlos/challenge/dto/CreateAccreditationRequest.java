package com.carlos.challenge.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record CreateAccreditationRequest(
        @NotNull @Positive BigDecimal amount,
        @NotNull String pointOfSaleId
) {}
