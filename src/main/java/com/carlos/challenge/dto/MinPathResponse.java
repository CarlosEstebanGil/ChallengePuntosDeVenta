package com.carlos.challenge.dto;

import java.util.List;

public record MinPathResponse(
        int totalCost,
        List<Integer> routeIds,
        List<String> routeNames
) {}
