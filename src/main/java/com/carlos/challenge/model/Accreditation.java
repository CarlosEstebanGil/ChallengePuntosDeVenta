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
        @CompoundIndex(name = "pv_fecha_idx", def = "{'pointOfSaleId': 1, 'receptionDate': -1}")
})
@Getter
@Setter
public class Accreditation {

    @Id
    private String id;

    private BigDecimal amount;

    @Indexed
    private String pointOfSaleId;

    @Indexed
    private Instant receptionDate;
    private String pointOfSaleName;

    public Accreditation() {}

    public Accreditation(BigDecimal amount, String pointOfSaleId, Instant receptionDate, String pointOfSaleName) {
        this.amount = amount;
        this.pointOfSaleId = pointOfSaleId;
        this.receptionDate = receptionDate;
        this.pointOfSaleName = pointOfSaleName;
    }
}