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
@Table(name = "product_match_candidate")
@Getter
@Setter
public class ProductMatchCandidateEntity {

    @Id private UUID id = UUID.randomUUID();

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "rfq_line_id", nullable = false)
    private UUID rfqLineId;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(nullable = false, precision = 8, scale = 4)
    private BigDecimal score;

    @Column(name = "match_method", nullable = false, length = 64)
    private String matchMethod;

    @Column(name = "rank_order", nullable = false)
    private int rankOrder = 1;

    @Column(nullable = false)
    private boolean selected;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();
}
