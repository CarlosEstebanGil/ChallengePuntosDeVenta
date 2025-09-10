package com.carlos.challenge.data;

import com.carlos.challenge.config.TestProfiles;
import com.carlos.challenge.infrastructure.out.persistence.mongo.adapter.AccreditationMongoAdapter;
import com.carlos.challenge.infrastructure.out.persistence.mongo.repository.AccreditationMongoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.containers.MongoDBContainer;
import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.*;

@DataMongoTest
@Testcontainers
@ActiveProfiles(TestProfiles.TEST)
@Import({AccreditationMongoAdapter.class,
        com.carlos.challenge.infrastructure.out.persistence.mongo.mapper.AccreditationMongoMapperImpl.class})
class AccreditationMongoAdapterTest {

    @Container
    static MongoDBContainer mongo = new MongoDBContainer("mongo:7.0.14");

    @Autowired AccreditationMongoRepository springRepo;
    @Autowired AccreditationMongoAdapter adapter;

    @Test
    void create_and_queryByFilters() {
        springRepo.deleteAll(); // Ensure clean DB before test

        var acc = new com.carlos.challenge.domain.model.Accreditation(
                null, new BigDecimal("12.34"),
                "POS1", "Point 1", Instant.parse("2024-01-01T00:00:00Z"));
        var saved = adapter.save(acc);
        assertThat(saved.id()).isNotNull();

        Page<com.carlos.challenge.domain.model.Accreditation> page =
                adapter.findByPointOfSale("POS1", PageRequest.of(0,10));
        assertThat(page.getTotalElements()).isEqualTo(1);

        Page<com.carlos.challenge.domain.model.Accreditation> datePage =
                adapter.findByDateBetween(Instant.parse("2023-12-31T00:00:00Z"),
                        Instant.parse("2024-12-31T23:59:59Z"),
                        PageRequest.of(0,10));
        assertThat(datePage.getTotalElements()).isEqualTo(1);
    }

}
