package tr.teklifos.masterdata.infrastructure;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tr.teklifos.masterdata.domain.ProductAliasRow;

public interface ProductAliasRepository extends JpaRepository<ProductAliasRow, UUID> {

    @Query(
            """
            SELECT a FROM ProductAliasRow a
            WHERE a.tenantId = :tenantId AND a.normalizedAlias = :normalized
            """)
    List<ProductAliasRow> findByTenantAndNormalized(
            @Param("tenantId") UUID tenantId, @Param("normalized") String normalized);
}
