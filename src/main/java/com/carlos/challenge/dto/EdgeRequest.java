package com.carlos.challenge.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record EdgeRequest(
        @NotNull Integer fromId,
        @NotNull Integer toId,
        @Min(0) int costo
) {}