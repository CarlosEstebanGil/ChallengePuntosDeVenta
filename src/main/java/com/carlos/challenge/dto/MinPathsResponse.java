package com.carlos.challenge.dto;

import java.util.List;

public record MinPathsResponse(
        int totalCost,
        List<PathDetail> paths
) {}