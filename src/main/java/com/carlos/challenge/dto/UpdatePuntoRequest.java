package com.carlos.challenge.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdatePuntoRequest(
        @NotBlank String nombre
) {}