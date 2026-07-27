package tr.teklifos.rfq.infrastructure.messaging;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import tr.teklifos.rfq.application.DocumentProcessingOrchestrator;
import tr.teklifos.shared.messaging.TeklifOsEvents;

@Component
public class PipelineUpdateListener {

    private static final String CONSUMER = "rfq-service-pipeline";

    private final DocumentProcessingOrchestrator orchestrator;

    public PipelineUpdateListener(DocumentProcessingOrchestrator orchestrator) {
        this.orchestrator = orchestrator;
    }

    @RabbitListener(queues = TeklifOsEvents.QUEUE_RFQ_PIPELINE)
    public void onPipelineUpdate(String body) {
        String messageId = java.util.UUID.randomUUID().toString();
        orchestrator.handlePipelineMessage(messageId, CONSUMER, body);
    }
}
