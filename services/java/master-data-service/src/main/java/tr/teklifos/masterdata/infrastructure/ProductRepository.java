package tr.teklifos.masterdata.infrastructure;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tr.teklifos.masterdata.domain.ProductEntity;

public interface ProductRepository extends JpaRepository<ProductEntity, UUID> {

    List<ProductEntity> findTop5ByTenantIdAndStatusAndSkuIgnoreCase(
            UUID tenantId, String status, String sku);

    List<ProductEntity> findByTenantIdAndStatus(UUID tenantId, String status);

    @Query(
            value =
                    """
            SELECT p.id, p.sku,
                   GREATEST(
                     similarity(p.name, :q),
                     similarity(p.sku, :q)
                   ) AS score
            FROM product p
            WHERE p.tenant_id = :tenantId
              AND p.status = 'ACTIVE'
              AND (p.name % :q OR p.sku % :q OR similarity(p.name, :q) > 0.25)
            ORDER BY score DESC
            LIMIT :limit
            """,
            nativeQuery = true)
    List<Object[]> searchTrigram(
            @Param("tenantId") UUID tenantId, @Param("q") String q, @Param("limit") int limit);
}
