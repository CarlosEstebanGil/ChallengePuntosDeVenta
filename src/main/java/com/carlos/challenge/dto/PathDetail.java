package com.carlos.challenge.dto;

import java.util.List;

public record PathDetail(
        List<String> routeIds,
        List<String> routeNames,
        List<Integer> routeCodes
) {}