package tr.teklifos.rfq.infrastructure;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import tr.teklifos.rfq.domain.RfqEntity;

public interface RfqRepository extends JpaRepository<RfqEntity, UUID> {

    List<RfqEntity> findByTenantIdOrderByReceivedAtDesc(UUID tenantId);

    Optional<RfqEntity> findByTenantIdAndId(UUID tenantId, UUID id);
}
