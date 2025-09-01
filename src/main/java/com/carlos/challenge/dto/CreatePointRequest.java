package com.carlos.challenge.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreatePointRequest(
        @NotNull Integer id,
        @NotBlank String name
) {}
