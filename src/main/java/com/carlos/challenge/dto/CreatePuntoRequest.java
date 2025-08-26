package com.carlos.challenge.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreatePuntoRequest(
        @NotNull Integer id,
        @NotBlank String nombre
) {}
