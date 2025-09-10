package com.carlos.challenge.infrastructure.in.web.dto.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record CreateAccreditationRequest(
        @NotNull BigDecimal amount,
        @NotBlank String pointOfSaleId
) {}
