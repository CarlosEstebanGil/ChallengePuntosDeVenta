// com.carlos.challenge.service.impl.PointCacheServiceImpl
package com.carlos.challenge.service.impl;

import com.carlos.challenge.model.PointOfSale;
import com.carlos.challenge.service.PointCacheService;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static org.springframework.http.HttpStatus.*;

@Service
public class PointCacheServiceImpl implements PointCacheService {

    public static final String ID_VACÍO = "Id vacío";
    private static final String NO_EXISTE = "The point with id %s does not exist";
    private static final String NAME_REQ = "The name cannot be empty";
    private static final String ID_REQ = "The id cannot be empty";
    public static final String NO_EXISTE_EL_PUNTO_CON_ID = "The point with id does not exist ";
    public static final String NO_EXISTE_EL_PUNTO_CON_CODE = "The point with code does not exist ";
    public static final String FORMATO_DE_ID_CODE_INVALIDO = "Invalid id/code format: ";
    public static final String EL_CODE_D_YA_EXISTE = "The code %d already exists";

    private final Map<String, PointOfSale> byId = new ConcurrentHashMap<>();
    private final Map<Integer, String> codeToId = new ConcurrentHashMap<>();
    private final Map<String, String> nameToId = new ConcurrentHashMap<>();

    private final AtomicInteger nextCode = new AtomicInteger(1);

    @Override
    public List<PointOfSale> findAll() {
        return byId.values().stream()
                .sorted(
                        Comparator.comparing(PointOfSale::name, String.CASE_INSENSITIVE_ORDER)
                                .thenComparing(PointOfSale::id)
                )
                .toList();
    }

    @Override
    public PointOfSale create(String name) {
        return create(name, null);
    }

    @Override
    public PointOfSale create(String name, Integer code) {
        if (name == null || name.isBlank()) {
            throw new ResponseStatusException(BAD_REQUEST, NAME_REQ);
        }

        String id = UUID.randomUUID().toString();
        Integer effectiveCode = code != null ? code : nextAvailableCode();

        if (codeToId.containsKey(effectiveCode)) {
            throw new ResponseStatusException(
                    CONFLICT,
                    EL_CODE_D_YA_EXISTE.formatted(effectiveCode)
            );
        }

        codeToId.put(effectiveCode, id);

        PointOfSale pos = new PointOfSale(id, name, effectiveCode);
        byId.put(id, pos);
        nameToId.put(name.trim(), id);

        return pos;
    }

    private int nextAvailableCode() {
        for (;;) {
            int candidate = nextCode.getAndIncrement();
            if (!codeToId.containsKey(candidate)) return candidate;
        }
    }

    @Override
    public PointOfSale update(String id, String name) {
        if (id == null || id.isBlank()) {
            throw new ResponseStatusException(BAD_REQUEST, ID_REQ);
        }
        if (name == null || name.isBlank()) {
            throw new ResponseStatusException(BAD_REQUEST, NAME_REQ);
        }

        PointOfSale existing = byId.get(id);
        if (existing == null) {
            throw new ResponseStatusException(NOT_FOUND, NO_EXISTE.formatted(id));
        }

        PointOfSale updated = new PointOfSale(id, name, existing.code());
        byId.put(id, updated);
        nameToId.put(name.trim(), id);
        return updated;
    }

    @Override
    public void delete(String id) {
        PointOfSale removed = byId.remove(id);
        if (removed == null) {
            throw new ResponseStatusException(NOT_FOUND, NO_EXISTE.formatted(id));
        }
        if (removed.code() != null) {
            codeToId.remove(removed.code());
        }

        nameToId.computeIfPresent(removed.name(), (k, v) -> v.equals(id) ? null : v);
    }

    @Override
    public PointOfSale findById(String id) {
        PointOfSale p = byId.get(id);
        if (p == null) {
            throw new ResponseStatusException(NOT_FOUND, NO_EXISTE.formatted(id));
        }
        return p;
    }

    @Override
    public Optional<PointOfSale> findByName(String name) {
        if (name == null || name.isBlank()) {
            return Optional.empty();
        }
        String id = nameToId.get(name.trim());
        return Optional.ofNullable(id).map(byId::get);
    }

    @Override
    public Optional<PointOfSale> findByCode(Integer code) {
        if (code == null) return Optional.empty();
        String id = codeToId.get(code);
        return Optional.ofNullable(id).map(byId::get);
    }

    @Override
    public String resolveId(String idOrCode) {
        if (idOrCode == null || idOrCode.isBlank()) {
            throw new ResponseStatusException(BAD_REQUEST, ID_VACÍO);
        }

        // ¿es UUID?
        if (isUuid(idOrCode)) {
            if (byId.containsKey(idOrCode)) return idOrCode;
            throw new ResponseStatusException(NOT_FOUND, NO_EXISTE_EL_PUNTO_CON_ID + idOrCode);
        }

        // ¿es número?
        try {
            int code = Integer.parseInt(idOrCode);
            return findByCode(code)
                    .map(PointOfSale::id)
                    .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, NO_EXISTE_EL_PUNTO_CON_CODE + code));
        } catch (NumberFormatException ignore) {
            throw new ResponseStatusException(BAD_REQUEST, FORMATO_DE_ID_CODE_INVALIDO + idOrCode);
        }
    }

    private static boolean isUuid(String s) {
        try {
            UUID.fromString(s);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
