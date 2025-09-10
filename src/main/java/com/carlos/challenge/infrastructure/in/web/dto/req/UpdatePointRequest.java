package com.carlos.challenge.infrastructure.in.web.dto.req;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

public record UpdatePointRequest(
        @NotBlank @JsonProperty("name") String name
) {}
