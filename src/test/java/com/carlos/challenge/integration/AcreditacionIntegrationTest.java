package com.carlos.challenge.integration;

import com.carlos.challenge.dto.AccreditationResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class AcreditacionIntegrationTest {

    public static final String HTTP_LOCALHOST = "http://localhost:";

    @Container
    static MongoDBContainer mongo = new MongoDBContainer("mongo:7");

    @DynamicPropertySource
    @SuppressWarnings("unused")
    static void mongoProps(DynamicPropertyRegistry r) {
        r.add("spring.data.mongodb.uri", mongo::getReplicaSetUrl);
    }

    @LocalServerPort
    int port;

    @Autowired
    TestRestTemplate rest;

    @SuppressWarnings("unused")
    static class PageResponse<T> {
        public List<T> content;
        public int number;
        public int size;
        public long totalElements;
        public int totalPages;
        public boolean first;
        public boolean last;
        public boolean empty;
    }

    private String url(String path) {
        return HTTP_LOCALHOST + port + path;
    }

    @Test
    void flujo_completo_create_y_list_acreditaciones() {

        Instant now = Instant.now();
        Instant from = now.minus(1, ChronoUnit.HOURS);
        Instant to   = now.plus(1, ChronoUnit.HOURS);

        Map<String, Object> pv = Map.of("id", 1, "name", "Sucursal Centro");
        ResponseEntity<Void> pvResp = rest
                .withBasicAuth("admin", "admin")
                .postForEntity(url("/api/pos"), pv, Void.class);

        assertThat(pvResp.getStatusCode().is2xxSuccessful()).isTrue();

        Map<String, Object> accReq = new HashMap<>();
        accReq.put("amount", new BigDecimal("1234.56"));
        accReq.put("pointOfSaleId", 1);

        ResponseEntity<AccreditationResponse> accResp = rest
                .withBasicAuth("user", "user")
                .postForEntity(url("/api/accreditations"), accReq, AccreditationResponse.class);

        assertThat(accResp.getStatusCode()).isIn(HttpStatus.OK, HttpStatus.CREATED);

        AccreditationResponse creada = Objects.requireNonNull(
                accResp.getBody(), "Body null al create acreditación");
        assertThat(creada.id()).isNotBlank();
        assertThat(creada.pointOfSaleId()).isEqualTo(1);
        assertThat(creada.pointOfSaleName()).isEqualTo("Sucursal Centro");
        assertThat(creada.receptionDate()).isNotNull();

        ResponseEntity<PageResponse<AccreditationResponse>> pageAll = rest
                .withBasicAuth("user", "user")
                .exchange(
                        url("/api/accreditations?page=0&size=10"),
                        HttpMethod.GET,
                        null,
                        new ParameterizedTypeReference<>() {}
                );

        assertThat(pageAll.getStatusCode()).isEqualTo(HttpStatus.OK);
        PageResponse<AccreditationResponse> bodyAll = Objects.requireNonNull(
                pageAll.getBody(), "Body null en listado general");
        assertThat(bodyAll.content).isNotEmpty();
        assertThat(bodyAll.totalElements).isGreaterThanOrEqualTo(1);

        assertThat(bodyAll.size).isGreaterThan(0);
        assertThat(bodyAll.number).isGreaterThanOrEqualTo(0);

        ResponseEntity<PageResponse<AccreditationResponse>> pageByPv = rest
                .withBasicAuth("user", "user")
                .exchange(
                        url("/api/accreditations?pointOfSaleId=1&page=0&size=10"),
                        HttpMethod.GET,
                        null,
                        new ParameterizedTypeReference<>() {}
                );

        assertThat(pageByPv.getStatusCode()).isEqualTo(HttpStatus.OK);
        PageResponse<AccreditationResponse> bodyByPv = Objects.requireNonNull(
                pageByPv.getBody(), "Body null en filtro por PV");
        assertThat(bodyByPv.content).isNotEmpty();

        assertThat(bodyByPv.content).allSatisfy(r -> assertThat(r.pointOfSaleId()).isEqualTo(1));


        String qs = String.format("?from=%s&to=%s&page=0&size=10", from, to);
        ResponseEntity<PageResponse<AccreditationResponse>> pageByDate = rest
                .withBasicAuth("user", "user")
                .exchange(
                        url("/api/accreditations" + qs),
                        HttpMethod.GET,
                        null,
                        new ParameterizedTypeReference<>() {}
                );

        assertThat(pageByDate.getStatusCode()).isEqualTo(HttpStatus.OK);
        PageResponse<AccreditationResponse> bodyByDate = Objects.requireNonNull(
                pageByDate.getBody(), "Body null en filtro por fechas");
        assertThat(bodyByDate.content).isNotEmpty();

        boolean contieneCreada = bodyByDate.content.stream()
                .anyMatch(r -> Objects.equals(r.id(), creada.id()));
        assertThat(contieneCreada).isTrue();


        ResponseEntity<String> unauth = rest
                .getForEntity(url("/api/accreditations?page=0&size=5"), String.class);
        assertThat(unauth.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void paginado_devuelve_vacio_fuera_de_rango() {

        Map<String, Object> pv = Map.of("id", 99, "name", "PV Test");
        rest.withBasicAuth("admin", "admin")
                .postForEntity(url("/api/pos"), pv, Void.class);

        ResponseEntity<PageResponse<AccreditationResponse>> page = rest
                .withBasicAuth("user", "user")
                .exchange(
                        url("/api/accreditations?page=9999&size=5"),
                        HttpMethod.GET,
                        null,
                        new ParameterizedTypeReference<>() {}
                );

        assertThat(page.getStatusCode()).isEqualTo(HttpStatus.OK);
        PageResponse<AccreditationResponse> body = Objects.requireNonNull(
                page.getBody(), "Body null en página fuera de rango");
        assertThat(body.content).isEmpty();
        assertThat(body.empty).isTrue();
    }

    @Test
    void delete_accreditation_ok() {
        // Create POS
        Map<String, Object> pv = Map.of("id", 2, "name", "Sucursal Norte");
        rest.withBasicAuth("admin", "admin")
                .postForEntity(url("/api/pos"), pv, Void.class);

        // Create accreditation
        Map<String, Object> accReq = new HashMap<>();
        accReq.put("amount", new BigDecimal("100.00"));
        accReq.put("pointOfSaleId", 2);

        ResponseEntity<AccreditationResponse> accResp = rest
                .withBasicAuth("user", "user")
                .postForEntity(url("/api/accreditations"), accReq, AccreditationResponse.class);

        String id = Objects.requireNonNull(accResp.getBody()).id();

        // Delete accreditation
        ResponseEntity<Void> delResp = rest
                .withBasicAuth("admin", "admin")
                .exchange(url("/api/accreditations/" + id), HttpMethod.DELETE, null, Void.class);

        assertThat(delResp.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        // Optionally, verify it's gone (e.g., not in list)
        ResponseEntity<String> getResp = rest
                .withBasicAuth("user", "user")
                .getForEntity(url("/api/accreditations/" + id), String.class);

        assertThat(getResp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
