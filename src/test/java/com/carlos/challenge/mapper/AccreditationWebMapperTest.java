package com.carlos.challenge.mapper;

import com.carlos.challenge.infrastructure.in.web.mapper.AccreditationWebMapper;
import com.carlos.challenge.infrastructure.in.web.mapper.AccreditationWebMapperImpl;
import com.carlos.challenge.infrastructure.in.web.dto.resp.AccreditationResponse;
import com.carlos.challenge.domain.model.Accreditation;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.*;

class AccreditationWebMapperTest {

    AccreditationWebMapper mapper = new AccreditationWebMapperImpl();

    @Test
    void toResponse_mapsAllFields() {
        Accreditation acc = new Accreditation("A1", new BigDecimal("10.00"),
                "P1", "Point 1", Instant.parse("2024-01-01T00:00:00Z"));

        AccreditationResponse r = mapper.toResponse(acc);

        assertThat(r.id()).isEqualTo("A1");
        assertThat(r.amount()).isEqualTo("10.00");
        assertThat(r.pointOfSaleId()).isEqualTo("P1");
        assertThat(r.pointOfSaleName()).isEqualTo("Point 1");
        assertThat(r.receptionDate()).isEqualTo(Instant.parse("2024-01-01T00:00:00Z"));
    }
}
