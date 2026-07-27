package tr.teklifos.rfq.infrastructure;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import tr.teklifos.rfq.domain.ProductMatchCandidateEntity;

public interface ProductMatchCandidateRepository
        extends JpaRepository<ProductMatchCandidateEntity, UUID> {

    List<ProductMatchCandidateEntity> findByRfqLineIdOrderByRankOrderAsc(UUID rfqLineId);

    void deleteByRfqLineId(UUID rfqLineId);
}
