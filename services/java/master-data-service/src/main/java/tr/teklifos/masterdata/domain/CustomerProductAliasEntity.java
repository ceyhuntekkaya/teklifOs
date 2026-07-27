package tr.teklifos.masterdata.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "customer_product_alias")
@Getter
@Setter
public class CustomerProductAliasEntity {

    @Id private UUID id = UUID.randomUUID();

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "customer_sku", nullable = false, length = 128)
    private String customerSku;

    @Column(name = "normalized_customer_sku", nullable = false, length = 128)
    private String normalizedCustomerSku;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();
}
