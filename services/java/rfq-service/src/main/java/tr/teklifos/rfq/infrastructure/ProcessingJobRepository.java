package tr.teklifos.rfq.infrastructure;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import tr.teklifos.rfq.domain.ProcessingJobEntity;

public interface ProcessingJobRepository extends JpaRepository<ProcessingJobEntity, UUID> {}
