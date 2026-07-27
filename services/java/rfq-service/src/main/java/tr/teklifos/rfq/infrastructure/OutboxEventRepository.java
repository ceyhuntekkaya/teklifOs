package tr.teklifos.rfq.infrastructure;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import tr.teklifos.rfq.infrastructure.persistence.RfqOutboxEvent;

public interface OutboxEventRepository extends JpaRepository<RfqOutboxEvent, UUID> {

    @Query("select e from RfqOutboxEvent e where e.publishedAt is null order by e.createdAt asc")
    List<RfqOutboxEvent> findUnpublished();
}
