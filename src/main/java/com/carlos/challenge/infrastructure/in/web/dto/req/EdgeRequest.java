package com.carlos.challenge.infrastructure.in.web.dto.req;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record EdgeRequest(
        @NotNull
        @JsonProperty("from") @JsonAlias({"fromId"})
        UUID from,

        @NotNull
        @JsonProperty("to") @JsonAlias({"toId"})
        UUID to,

        @NotNull
        Integer cost
) {}
