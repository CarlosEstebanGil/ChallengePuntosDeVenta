package com.carlos.challenge.dto;

import java.util.List;

public record MinPathResponse(
        int costoTotal,
        List<Integer> rutaIds,
        List<String> rutaNombres
) {}
