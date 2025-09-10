package com.carlos.challenge.mapper;

import com.carlos.challenge.infrastructure.out.persistence.mongo.mapper.AccreditationMongoMapper;
import com.carlos.challenge.infrastructure.out.persistence.mongo.mapper.AccreditationMongoMapperImpl;
import com.carlos.challenge.infrastructure.out.persistence.mongo.entity.AccreditationDocument;
import com.carlos.challenge.domain.model.Accreditation;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.*;

class AccreditationMongoMapperTest {

    AccreditationMongoMapper mapper = new AccreditationMongoMapperImpl();

    @Test
    void roundTrip_domain_document_domain() {
        Accreditation a = new Accreditation("A1", new BigDecimal("9.99"), "P1", "Point 1",
                Instant.parse("2024-02-02T00:00:00Z"));
        AccreditationDocument d = mapper.toDocument(a);
        assertThat(d.getId()).isEqualTo("A1");
        assertThat(d.getPointOfSaleId()).isEqualTo("P1");
        assertThat(d.getAmount()).isEqualTo(new BigDecimal("9.99"));

        Accreditation a2 = mapper.toDomain(d);
        assertThat(a2).isEqualTo(a);
    }
}
