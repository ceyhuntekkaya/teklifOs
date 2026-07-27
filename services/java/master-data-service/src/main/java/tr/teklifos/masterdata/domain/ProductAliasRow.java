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
@Table(name = "product_alias")
@Getter
@Setter
public class ProductAliasRow {

    @Id private UUID id = UUID.randomUUID();

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "alias_text", nullable = false, length = 512)
    private String aliasText;

    @Column(name = "normalized_alias", nullable = false, length = 512)
    private String normalizedAlias;

    @Column(length = 64)
    private String source;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();
}
