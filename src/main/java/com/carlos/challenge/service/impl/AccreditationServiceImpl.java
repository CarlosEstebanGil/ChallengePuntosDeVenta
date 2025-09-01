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

        PointOfSale pv = pointCache.findById(req.pointOfSaleId());

        Instant now = Instant.now();
        Accreditation acc = new Accreditation(
                req.amount(),
                req.pointOfSaleId(),
                now,
                pv.name()
        );

        Accreditation saved = repo.save(acc);

        return new AccreditationResponse(
                saved.getId(),
                saved.getAmount(),
                saved.getPointOfSaleId(),
                saved.getPointOfSaleName(),
                saved.getReceptionDate()
        );
    }

    @Override
    public Page<AccreditationResponse> list(Optional<Integer> pointOfSaleId,
                                              Optional<Instant> from,
                                              Optional<Instant> to,
                                              Pageable pageable) {

        boolean hasPv = pointOfSaleId.isPresent();
        boolean hasRange = from.isPresent() && to.isPresent(); //solo aplica el filtro de fechas si vienen ambos from y to sino hago findall

        Page<Accreditation> page;
        if (hasPv && hasRange) {
            page = repo.findBypointOfSaleIdAndReceptionDateBetween(pointOfSaleId.get(), from.get(), to.get(), pageable);
        } else if (hasPv) {
            page = repo.findBypointOfSaleId(pointOfSaleId.get(), pageable);
        } else if (hasRange) {
            page = repo.findByreceptionDateBetween(from.get(), to.get(), pageable);
        } else {
            page = repo.findAll(pageable);
        }

        return page.map(a -> new AccreditationResponse(
                a.getId(), a.getAmount(), a.getPointOfSaleId(), a.getPointOfSaleName(), a.getReceptionDate()
        ));
    }
    @Override
    public void delete(String id) {
        if (!repo.existsById(id)) {
            throw new IllegalArgumentException("Accreditation not found with id: " + id);
        }
        repo.deleteById(id);
    }

    @Override
    public Optional<AccreditationResponse> findById(String id) {
        return repo.findById(id)
                .map(a -> new AccreditationResponse(
                        a.getId(),
                        a.getAmount(),
                        a.getPointOfSaleId(),
                        a.getPointOfSaleName(),
                        a.getReceptionDate()
                ));
    }
}
