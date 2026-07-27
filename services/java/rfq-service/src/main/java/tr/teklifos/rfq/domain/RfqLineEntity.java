package tr.teklifos.rfq.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "rfq_line")
@Getter
@Setter
public class RfqLineEntity {

    @Id private UUID id = UUID.randomUUID();

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "rfq_id", nullable = false)
    private UUID rfqId;

    @Column(name = "line_number", nullable = false)
    private int lineNumber;

    @Column(name = "raw_description")
    private String rawDescription;

    @Column(name = "raw_customer_sku", length = 128)
    private String rawCustomerSku;

    @Column(precision = 20, scale = 4)
    private BigDecimal quantity;

    @Column(name = "unit_code", length = 16)
    private String unitCode;

    @Column(name = "match_status", nullable = false, length = 32)
    private String matchStatus = "UNMATCHED";

    @Column(name = "matched_product_id")
    private UUID matchedProductId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();
}
