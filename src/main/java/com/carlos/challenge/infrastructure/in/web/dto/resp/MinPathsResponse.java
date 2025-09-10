package com.carlos.challenge.infrastructure.in.web.dto.resp;


import java.util.List;

public record MinPathsResponse(
        int totalCost,
        List<PathDetail> paths
) {}