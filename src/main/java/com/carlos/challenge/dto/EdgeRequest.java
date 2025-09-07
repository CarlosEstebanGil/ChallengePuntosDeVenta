package com.carlos.challenge.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record EdgeRequest(
        @NotNull String fromId,
        @NotNull String toId,
        @Min(0) int cost
) {}