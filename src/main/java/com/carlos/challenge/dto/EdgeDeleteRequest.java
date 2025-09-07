package com.carlos.challenge.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record EdgeDeleteRequest(
        @NotNull UUID fromId,
        @NotNull UUID toId
) {}
