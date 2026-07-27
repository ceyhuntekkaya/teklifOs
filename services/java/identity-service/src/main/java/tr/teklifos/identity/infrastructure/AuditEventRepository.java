package tr.teklifos.identity.infrastructure;

import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import tr.teklifos.identity.domain.AuditEventEntity;

public interface AuditEventRepository extends JpaRepository<AuditEventEntity, UUID> {

    List<AuditEventEntity> findByTenantIdOrderByCreatedAtDesc(UUID tenantId, Pageable pageable);
}
