package com.carlos.challenge.dto;

import jakarta.validation.constraints.NotBlank;

public record CreatePointRequest(
        @NotBlank String name,
        Integer code
) {}