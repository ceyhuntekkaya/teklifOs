package tr.teklifos.masterdata.infrastructure;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import tr.teklifos.masterdata.domain.CustomerProductAliasEntity;

public interface CustomerProductAliasRepository
        extends JpaRepository<CustomerProductAliasEntity, UUID> {

    List<CustomerProductAliasEntity> findByTenantIdAndCustomerIdAndNormalizedCustomerSku(
            UUID tenantId, UUID customerId, String normalizedCustomerSku);
}
