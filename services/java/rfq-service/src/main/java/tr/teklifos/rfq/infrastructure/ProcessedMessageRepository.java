package tr.teklifos.rfq.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import tr.teklifos.rfq.infrastructure.persistence.RfqProcessedMessage;

public interface ProcessedMessageRepository
        extends JpaRepository<RfqProcessedMessage, RfqProcessedMessage.ProcessedMessageId> {}
