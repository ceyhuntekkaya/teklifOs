package tr.teklifos.rfq.infrastructure;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import tr.teklifos.rfq.domain.RfqDocumentEntity;

public interface RfqDocumentRepository extends JpaRepository<RfqDocumentEntity, UUID> {

    List<RfqDocumentEntity> findByRfqIdOrderByUploadedAtAsc(UUID rfqId);

    Optional<RfqDocumentEntity> findByTenantIdAndId(UUID tenantId, UUID id);

    Optional<RfqDocumentEntity> findByTenantIdAndChecksumSha256(UUID tenantId, String checksum);
}
