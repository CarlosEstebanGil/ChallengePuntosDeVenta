package com.carlos.challenge.service;

import com.carlos.challenge.dto.AccreditationResponse;
import com.carlos.challenge.dto.CreateAccreditationRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.Optional;

public interface AccreditationService {

    AccreditationResponse create(CreateAccreditationRequest req);

    Page<AccreditationResponse> list(Optional<Integer> idPuntoVenta,
                                       Optional<Instant> from,
                                       Optional<Instant> to,
                                       Pageable pageable);
}
