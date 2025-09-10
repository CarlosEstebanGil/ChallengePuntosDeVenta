package com.carlos.challenge.testutil;

import com.carlos.challenge.domain.model.Accreditation;
import com.carlos.challenge.domain.model.PointOfSale;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public final class TestData {
    private TestData(){}

    public static PointOfSale pos(String id, String name, int code) {
        return new PointOfSale(id, name, code);
    }

    public static Accreditation acc(String id, BigDecimal amount, String posId, String posName, Instant when) {
        return new Accreditation(id, amount, posId, posName, when);
    }

    public static List<PointOfSale> threePOS() {
        return List.of(
            pos("p1", "One", 1001),
            pos("p2", "Two", 1002),
            pos("p3", "Three", 1003)
        );
    }
}
