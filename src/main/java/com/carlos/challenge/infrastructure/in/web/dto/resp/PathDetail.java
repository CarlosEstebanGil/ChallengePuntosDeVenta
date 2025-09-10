package com.carlos.challenge.infrastructure.in.web.dto.resp;

import java.util.List;

public record PathDetail(
        List<String> pointIds,
        List<String> pointNames,
        List<Integer> pointCodes
) {}
