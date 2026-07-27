package tr.teklifos.rfq.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tr.teklifos.rfq.domain.RfqEventEntity;
import tr.teklifos.rfq.infrastructure.RfqEventRepository;
import tr.teklifos.shared.messaging.TeklifOsEvents;
import tr.teklifos.rfq.infrastructure.persistence.RfqOutboxEvent;
import tr.teklifos.rfq.infrastructure.OutboxEventRepository;

@Service
public class RfqEventPublisher {

    private final OutboxEventRepository outboxRepository;
    private final RfqEventRepository eventRepository;
    private final ObjectMapper objectMapper;

    public RfqEventPublisher(
            OutboxEventRepository outboxRepository,
            RfqEventRepository eventRepository,
            ObjectMapper objectMapper) {
        this.outboxRepository = outboxRepository;
        this.eventRepository = eventRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public void publishDocumentEvent(
            UUID tenantId,
            UUID rfqId,
            UUID documentId,
            String eventType,
            Map<String, Object> payload,
            UUID actorUserId) {
        String json = toJson(payload);

        RfqEventEntity timeline = new RfqEventEntity();
        timeline.setTenantId(tenantId);
        timeline.setRfqId(rfqId);
        timeline.setEventType(eventType);
        timeline.setPayload(json);
        timeline.setActorUserId(actorUserId);
        eventRepository.save(timeline);

        RfqOutboxEvent outbox = new RfqOutboxEvent();
        outbox.setTenantId(tenantId);
        outbox.setAggregateType("rfq_document");
        outbox.setAggregateId(documentId);
        outbox.setEventType(eventType);
        outbox.setPayload(json);
        outbox.setCreatedAt(Instant.now());
        outboxRepository.save(outbox);
    }

    private String toJson(Map<String, Object> payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }
}
