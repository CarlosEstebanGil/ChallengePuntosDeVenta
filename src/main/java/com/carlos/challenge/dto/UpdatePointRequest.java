package com.carlos.challenge.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdatePointRequest(
        @NotBlank String name
) {}