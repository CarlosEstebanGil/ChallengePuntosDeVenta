package com.carlos.challenge.domain.model.graph;

import java.util.List;

public record MinPaths(
        int totalCost,
        List<List<String>> paths
) {}
