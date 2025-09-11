package com.carlos.challenge.infrastructure.out.persistence.redis.adapter;

import com.carlos.challenge.domain.model.PointOfSale;
import com.carlos.challenge.domain.port.out.PointOfSaleRepositoryPort;
import org.junit.jupiter.api.*;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.codec.JsonJacksonCodec;
import org.redisson.config.Config;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import java.util.List;
import java.util.NoSuchElementException;


import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
class PointOfSaleRedisAdapterIT {

    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7").withExposedPorts(6379);

    static RedissonClient redisson;
    static PointOfSaleRepositoryPort repo;

    @BeforeAll
    static void setupAll() {
        String host = redis.getHost();
        Integer port = redis.getMappedPort(6379);

        Config cfg = new Config();
        cfg.setCodec(new JsonJacksonCodec());
        cfg.useSingleServer().setAddress("redis://" + host + ":" + port);

        redisson = Redisson.create(cfg);
        repo = new PointOfSaleRedisAdapter(redisson);
    }

    @AfterAll
    static void tearDownAll() {
        if (redisson != null) redisson.shutdown();
    }

    @BeforeEach
    void clean() {
        redisson.getKeys().delete("pos:byId", "pos:idByCode", "pos:idByName");
        redisson.getAtomicLong("pos:codeSeq").set(1L);
    }

    @Test
    void save_withoutCode_assignsSequentialCode_andPersists() {
        PointOfSale created = repo.save(new PointOfSale(null, "Sucursal Centro", null));

        assertNotNull(created.id());
        assertNotNull(created.code());
        assertEquals("Sucursal Centro", created.name());

        assertEquals(created, repo.findById(created.id()).orElseThrow());
        assertEquals(created.id(), repo.findByCode(created.code()).orElseThrow().id());
    }

    @Test
    void save_withCustomCode_enforcesUniqueness() {
        PointOfSale a = repo.save(new PointOfSale(null, "Norte", 123));
        assertEquals(123, a.code());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> repo.save(new PointOfSale(null, "Norte2", 123)));
        assertTrue(ex.getMessage().toLowerCase().contains("already"));
    }

    @Test
    void findByName_and_updateIndicesOnRename() {
        PointOfSale a = repo.save(new PointOfSale(null, "Flores", null));
        assertTrue(repo.findByName("Flores").isPresent());

        PointOfSale updated = repo.save(new PointOfSale(a.id(), "Flores-New", a.code()));

        assertTrue(repo.findByName("Flores-New").isPresent());
        assertTrue(repo.findByName("Flores").isEmpty());
        assertEquals(updated.id(), repo.findByCode(updated.code()).orElseThrow().id());
    }

    @Test
    void deleteById_removesFromAllIndexes() {
        PointOfSale a = repo.save(new PointOfSale(null, "Sur", 777));
        repo.deleteById(a.id());

        assertTrue(repo.findById(a.id()).isEmpty());
        assertTrue(repo.findByName("Sur").isEmpty());
        assertTrue(repo.findByCode(777).isEmpty());
    }

    @Test
    void resolveId_worksForUuidOrCode() {
        PointOfSale a = repo.save(new PointOfSale(null, "Caballito", 55));

        assertEquals(a.id(), repo.resolveId(a.id()));
        assertEquals(a.id(), repo.resolveId("55"));

        assertThrows(NoSuchElementException.class, () -> repo.resolveId("99999"));
        assertThrows(IllegalArgumentException.class, () -> repo.resolveId("not-a-code"));
        assertThrows(IllegalArgumentException.class, () -> repo.resolveId(" "));
    }

    @Test
    void findAll_returnsAll() {
        repo.save(new PointOfSale(null, "A", null));
        repo.save(new PointOfSale(null, "B", null));

        List<PointOfSale> all = repo.findAll();
        assertTrue(all.size() >= 2);
    }
}
