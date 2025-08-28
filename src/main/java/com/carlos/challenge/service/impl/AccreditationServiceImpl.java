package com.carlos.challenge.service.impl;

import com.carlos.challenge.dto.AccreditationResponse;
import com.carlos.challenge.dto.CreateAccreditationRequest;
import com.carlos.challenge.model.Accreditation;
import com.carlos.challenge.model.PointOfSale;
import com.carlos.challenge.repository.AccreditationRepository;
import com.carlos.challenge.service.AccreditationService;
import com.carlos.challenge.service.PointCacheService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

@Service
public class AccreditationServiceImpl implements AccreditationService {

    private final AccreditationRepository repo;
    private final PointCacheService pointCache;

    public AccreditationServiceImpl(AccreditationRepository repo, PointCacheService pointCache) {
        this.repo = repo;
        this.pointCache = pointCache;
    }

    @Override
    public AccreditationResponse create(CreateAccreditationRequest req) {

        PointOfSale pv = pointCache.findById(req.idPuntoVenta());

        Instant now = Instant.now();
        Accreditation acc = new Accreditation(
                req.importe(),
                req.idPuntoVenta(),
                now,
                pv.nombre()
        );

        Accreditation saved = repo.save(acc);

        return new AccreditationResponse(
                saved.getId(),
                saved.getImporte(),
                saved.getIdPuntoVenta(),
                saved.getNombrePuntoVenta(),
                saved.getFechaRecepcion()
        );
    }

    @Override
    public Page<AccreditationResponse> list(Optional<Integer> idPuntoVenta,
                                              Optional<Instant> from,
                                              Optional<Instant> to,
                                              Pageable pageable) {

        boolean hasPv = idPuntoVenta.isPresent();
        boolean hasRange = from.isPresent() && to.isPresent(); //solo aplica el filtro de fechas si vienen ambos from y to sino hago findall

        Page<Accreditation> page;
        if (hasPv && hasRange) {
            page = repo.findByIdPuntoVentaAndFechaRecepcionBetween(idPuntoVenta.get(), from.get(), to.get(), pageable);
        } else if (hasPv) {
            page = repo.findByIdPuntoVenta(idPuntoVenta.get(), pageable);
        } else if (hasRange) {
            page = repo.findByFechaRecepcionBetween(from.get(), to.get(), pageable);
        } else {
            page = repo.findAll(pageable);
        }

        return page.map(a -> new AccreditationResponse(
                a.getId(), a.getImporte(), a.getIdPuntoVenta(), a.getNombrePuntoVenta(), a.getFechaRecepcion()
        ));
    }
}
