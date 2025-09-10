package com.carlos.challenge.mapper;

import com.carlos.challenge.infrastructure.in.web.mapper.PointOfSaleWebMapper;
import com.carlos.challenge.infrastructure.in.web.mapper.PointOfSaleWebMapperImpl;
import com.carlos.challenge.infrastructure.in.web.dto.resp.PointOfSaleResponse;
import com.carlos.challenge.domain.model.PointOfSale;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class PointOfSaleWebMapperTest {

    PointOfSaleWebMapper mapper = new PointOfSaleWebMapperImpl();

    @Test
    void mapsAllFields() {
        PointOfSale p = new PointOfSale("ID","Name", 1234);
        PointOfSaleResponse r = mapper.toResponse(p);
        assertThat(r.id()).isEqualTo("ID");
        assertThat(r.name()).isEqualTo("Name");
        assertThat(r.code()).isEqualTo(1234);
    }
}
