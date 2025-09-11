package com.carlos.challenge.infrastructure.out.persistence.cache.adapter;

import com.carlos.challenge.domain.model.PointOfSale;
import com.carlos.challenge.domain.port.out.PointOfSaleRepositoryPort;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.StampedLock;

@Profile("!redis")
@Component
public class PointOfSaleCacheAdapter implements PointOfSaleRepositoryPort {

    public static final String EMPTY_ID_CODE = "Empty id/code";
    public static final String INVALID_ID_CODE_FORMAT = "Invalid id/code format: ";
    public static final String POINT_OF_SALE_NOT_FOUND_FOR_CODE = "PointOfSale not found for code: ";
    public static final String POINT_OF_SALE_CODE_ALREADY_IN_USE = "PointOfSale code already in use: ";
    private final Map<String, PointOfSale> byId = new ConcurrentHashMap<>();
    private final Map<Integer, String> idByCode = new ConcurrentHashMap<>();
    private final Map<String, String> idByName = new ConcurrentHashMap<>();
    private final AtomicInteger codeSeq = new AtomicInteger(1);

    private final StampedLock lock = new StampedLock();

    @Override
    public List<PointOfSale> findAll() {
        long stamp = lock.tryOptimisticRead();
        List<PointOfSale> snapshot = new ArrayList<>(byId.values());
        if (!lock.validate(stamp)) {
            stamp = lock.readLock();
            try {
                snapshot = new ArrayList<>(byId.values());
            } finally {
                lock.unlockRead(stamp);
            }
        }
        return snapshot;
    }

    @Override
    public Optional<PointOfSale> findById(String id) {
        long stamp = lock.tryOptimisticRead();
        PointOfSale pos = byId.get(id);
        if (!lock.validate(stamp)) {
            stamp = lock.readLock();
            try {
                pos = byId.get(id);
            } finally {
                lock.unlockRead(stamp);
            }
        }
        return Optional.ofNullable(pos);
    }

    @Override
    public Optional<PointOfSale> findByName(String name) {
        long stamp = lock.tryOptimisticRead();
        String id = idByName.get(name);
        PointOfSale pos = (id == null) ? null : byId.get(id);
        if (!lock.validate(stamp)) {
            stamp = lock.readLock();
            try {
                id = idByName.get(name);
                pos = (id == null) ? null : byId.get(id);
            } finally {
                lock.unlockRead(stamp);
            }
        }
        return Optional.ofNullable(pos);
    }

    @Override
    public Optional<PointOfSale> findByCode(Integer code) {
        long stamp = lock.tryOptimisticRead();
        String id = idByCode.get(code);
        PointOfSale pos = (id == null) ? null : byId.get(id);
        if (!lock.validate(stamp)) {
            stamp = lock.readLock();
            try {
                id = idByCode.get(code);
                pos = (id == null) ? null : byId.get(id);
            } finally {
                lock.unlockRead(stamp);
            }
        }
        return Optional.ofNullable(pos);
    }

    @Override
    public String resolveId(String idOrCode) {
        if (idOrCode == null || idOrCode.isBlank()) {
            throw new IllegalArgumentException(EMPTY_ID_CODE);
        }
        if (isUuid(idOrCode)) return idOrCode;

        final int code;
        try {
            code = Integer.parseInt(idOrCode.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(INVALID_ID_CODE_FORMAT + idOrCode);
        }

        long stamp = lock.tryOptimisticRead();
        String id = idByCode.get(code);
        if (!lock.validate(stamp)) {
            stamp = lock.readLock();
            try {
                id = idByCode.get(code);
            } finally {
                lock.unlockRead(stamp);
            }
        }
        if (id == null) throw new NoSuchElementException(POINT_OF_SALE_NOT_FOUND_FOR_CODE + code);
        return id;
    }


    @Override
    public PointOfSale save(PointOfSale point) {
        long stamp = lock.writeLock();
        try {
            String id = (point.id() == null || point.id().isBlank())
                    ? UUID.randomUUID().toString()
                    : point.id();

            Integer code = point.code();
            if (code == null) {
                code = nextAvailableCodeNoLock();
            } else {
                String existingIdForCode = idByCode.get(code);
                if (existingIdForCode != null && !existingIdForCode.equals(id)) {
                    throw new IllegalArgumentException(POINT_OF_SALE_CODE_ALREADY_IN_USE + code);
                }
            }

            PointOfSale stored = new PointOfSale(id, point.name(), code);


            PointOfSale previous = byId.put(id, stored);
            if (previous != null) {
                if (previous.code() != null) idByCode.remove(previous.code());
                if (previous.name() != null) idByName.remove(previous.name());
            }

            if (stored.code() != null) idByCode.put(stored.code(), id);
            if (stored.name() != null) idByName.put(stored.name(), id);

            return stored;
        } finally {
            lock.unlockWrite(stamp);
        }
    }

    @Override
    public void deleteById(String id) {
        long stamp = lock.writeLock();
        try {
            PointOfSale removed = byId.remove(id);
            if (removed != null) {
                if (removed.code() != null) idByCode.remove(removed.code());
                if (removed.name() != null) idByName.remove(removed.name());
            }
        } finally {
            lock.unlockWrite(stamp);
        }
    }

    private int nextAvailableCodeNoLock() {
        int c;
        do {
            c = codeSeq.getAndIncrement();
        } while (idByCode.containsKey(c));
        return c;
    }

    private static boolean isUuid(String s) {
        try { UUID.fromString(s); return true; }
        catch (Exception e) { return false; }
    }
}