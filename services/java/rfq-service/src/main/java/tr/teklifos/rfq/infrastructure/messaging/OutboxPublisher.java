package tr.teklifos.rfq.infrastructure.messaging;

import java.time.Instant;
import java.util.List;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tr.teklifos.rfq.infrastructure.OutboxEventRepository;
import tr.teklifos.shared.messaging.TeklifOsEvents;
import tr.teklifos.rfq.infrastructure.persistence.RfqOutboxEvent;

@Component
public class OutboxPublisher {

    private final OutboxEventRepository repository;
    private final RabbitTemplate rabbitTemplate;

    public OutboxPublisher(OutboxEventRepository repository, RabbitTemplate rabbitTemplate) {
        this.repository = repository;
        this.rabbitTemplate = rabbitTemplate;
    }

    @Scheduled(fixedDelay = 2000)
    @Transactional
    public void publishPending() {
        List<RfqOutboxEvent> batch = repository.findUnpublished();
        for (RfqOutboxEvent event : batch) {
            rabbitTemplate.convertAndSend(
                    TeklifOsEvents.EXCHANGE, event.getEventType(), event.getPayload());
            event.setPublishedAt(Instant.now());
            repository.save(event);
        }
    }
}
