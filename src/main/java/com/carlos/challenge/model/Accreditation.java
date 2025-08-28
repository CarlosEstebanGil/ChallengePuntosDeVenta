package com.carlos.challenge.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;

@Document(collection = "acreditacionesV2")
@CompoundIndexes({
        @CompoundIndex(name = "pv_fecha_idx", def = "{'idPuntoVenta': 1, 'fechaRecepcion': -1}")
})
@Getter
@Setter
public class Accreditation {

    @Id
    private String id;

    private BigDecimal importe;

    @Indexed
    private Integer idPuntoVenta;

    @Indexed
    private Instant fechaRecepcion;       // lo seteo en el servicio

    private String nombrePuntoVenta;      // lo obtengo del caché

    public Accreditation() {}

    public Accreditation(BigDecimal importe, Integer idPuntoVenta, Instant fechaRecepcion, String nombrePuntoVenta) {
        this.importe = importe;
        this.idPuntoVenta = idPuntoVenta;
        this.fechaRecepcion = fechaRecepcion;
        this.nombrePuntoVenta = nombrePuntoVenta;
    }
}