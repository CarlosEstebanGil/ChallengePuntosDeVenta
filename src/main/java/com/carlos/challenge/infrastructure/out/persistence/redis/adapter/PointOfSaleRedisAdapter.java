package com.carlos.challenge.infrastructure.out.persistence.redis.adapter;

import com.carlos.challenge.domain.model.PointOfSale;
import com.carlos.challenge.domain.port.out.PointOfSaleRepositoryPort;
import org.redisson.api.*;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import java.util.*;
import java.util.concurrent.TimeUnit;
import org.redisson.client.codec.Codec;
import org.redisson.codec.TypedJsonJacksonCodec;

@Profile("redis")
@Component
public class PointOfSaleRedisAdapter implements PointOfSaleRepositoryPort {

    private static final String POINT_OF_SALE_NOT_FOUND_FOR_CODE = "PointOfSale not found for code: ";
    private static final String EMPTY_ID_CODE = "Empty id/code";
    private static final String INVALID_ID_CODE_FORMAT = "Invalid id/code format: ";
    private static final String CODE_ALREADY_IN_USE = "PointOfSale code already in use: ";

    private final RMap<String, PointOfSale> byId;
    private final RMap<Integer, String> idByCode;
    private final RMap<String, String> idByName;
    private final RAtomicLong codeSeq;
    private final RReadWriteLock rw;

    public PointOfSaleRedisAdapter(RedissonClient redisson) {
        Codec byIdCodec     = new TypedJsonJacksonCodec(String.class, PointOfSale.class);
        Codec codeIdxCodec  = new TypedJsonJacksonCodec(Integer.class, String.class);
        Codec nameIdxCodec  = new TypedJsonJacksonCodec(String.class, String.class);

        this.byId     = redisson.getMap("pos:byId", byIdCodec);
        this.idByCode = redisson.getMap("pos:idByCode", codeIdxCodec);
        this.idByName = redisson.getMap("pos:idByName", nameIdxCodec);

        this.codeSeq  = redisson.getAtomicLong("pos:codeSeq");
        this.rw       = redisson.getReadWriteLock("pos:lock");

        this.codeSeq.compareAndSet(0, 1);
    }

    @Override
    public List<PointOfSale> findAll() {
        RLock r = rw.readLock();
        r.lock();
        try {
            return new ArrayList<>(byId.values());
        } finally {
            r.unlock();
        }
    }

    @Override
    public Optional<PointOfSale> findById(String id) {
        RLock r = rw.readLock();
        r.lock();
        try {
            return Optional.ofNullable(byId.get(id));
        } finally {
            r.unlock();
        }
    }

    @Override
    public Optional<PointOfSale> findByName(String name) {
        RLock r = rw.readLock();
        r.lock();
        try {
            String id = idByName.get(name);
            return Optional.ofNullable(id == null ? null : byId.get(id));
        } finally {
            r.unlock();
        }
    }

    @Override
    public Optional<PointOfSale> findByCode(Integer code) {
        RLock r = rw.readLock();
        r.lock();
        try {
            String id = idByCode.get(code);
            return Optional.ofNullable(id == null ? null : byId.get(id));
        } finally {
            r.unlock();
        }
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

        RLock r = rw.readLock();
        r.lock();
        try {
            String id = idByCode.get(code);
            if (id == null) throw new NoSuchElementException(POINT_OF_SALE_NOT_FOUND_FOR_CODE + code);
            return id;
        } finally {
            r.unlock();
        }
    }

    @Override
    public PointOfSale save(PointOfSale point) {
        RLock w = rw.writeLock();
        w.lock(10, TimeUnit.SECONDS);
        try {
            String id = (point.id() == null || point.id().isBlank())
                    ? UUID.randomUUID().toString()
                    : point.id();

            Integer code = point.code();
            if (code == null) {
                int c;
                do {
                    c = (int) codeSeq.getAndIncrement();
                } while (idByCode.containsKey(c));
                code = c;
            } else {
                String existingIdForCode = idByCode.get(code);
                if (existingIdForCode != null && !existingIdForCode.equals(id)) {
                    throw new IllegalArgumentException(CODE_ALREADY_IN_USE + code);
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
            w.unlock();
        }
    }

    @Override
    public void deleteById(String id) {
        RLock w = rw.writeLock();
        w.lock(10, TimeUnit.SECONDS);
        try {
            PointOfSale removed = byId.remove(id);
            if (removed != null) {
                if (removed.code() != null) idByCode.remove(removed.code());
                if (removed.name() != null) idByName.remove(removed.name());
            }
        } finally {
            w.unlock();
        }
    }

    private static boolean isUuid(String s) {
        try { UUID.fromString(s); return true; }
        catch (Exception e) { return false; }
    }
}
