package tr.teklifos.rfq.infrastructure;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import tr.teklifos.rfq.domain.RfqLineEntity;

public interface RfqLineRepository extends JpaRepository<RfqLineEntity, UUID> {

    List<RfqLineEntity> findByRfqIdOrderByLineNumberAsc(UUID rfqId);

    Optional<RfqLineEntity> findByRfqIdAndLineNumber(UUID rfqId, int lineNumber);
}
