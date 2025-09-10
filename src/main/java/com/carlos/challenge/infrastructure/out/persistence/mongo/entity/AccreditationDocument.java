package com.carlos.challenge.infrastructure.out.persistence.mongo.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.math.BigDecimal;
import java.time.Instant;

@Document(collection = "acreditacionesV2")
@CompoundIndexes({
        @CompoundIndex(name = "pos_and_date_idx", def = "{'pointOfSaleId': 1, 'receptionDate': 1}")
})
public class AccreditationDocument {

    @Id
    private String id;

    private BigDecimal amount;

    @Indexed
    private String pointOfSaleId;

    private String pointOfSaleName;

    @Indexed
    private Instant receptionDate;

    public AccreditationDocument() {}

    public AccreditationDocument(String id, BigDecimal amount, String pointOfSaleId, String pointOfSaleName, Instant receptionDate) {
        this.id = id;
        this.amount = amount;
        this.pointOfSaleId = pointOfSaleId;
        this.pointOfSaleName = pointOfSaleName;
        this.receptionDate = receptionDate;
    }

    public String getId() { return id; }
    public BigDecimal getAmount() { return amount; }
    public String getPointOfSaleId() { return pointOfSaleId; }
    public String getPointOfSaleName() { return pointOfSaleName; }
    public Instant getReceptionDate() { return receptionDate; }

    public void setId(String id) { this.id = id; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public void setPointOfSaleId(String pointOfSaleId) { this.pointOfSaleId = pointOfSaleId; }
    public void setPointOfSaleName(String pointOfSaleName) { this.pointOfSaleName = pointOfSaleName; }
    public void setReceptionDate(Instant receptionDate) { this.receptionDate = receptionDate; }
}
