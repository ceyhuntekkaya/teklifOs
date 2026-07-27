package tr.teklifos.rfq.infrastructure.messaging;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tr.teklifos.shared.messaging.TeklifOsEvents;

@Configuration
public class RabbitTopologyConfig {

    @Bean
    TopicExchange teklifosEventsExchange() {
        return new TopicExchange(TeklifOsEvents.EXCHANGE, true, false);
    }

    @Bean
    Queue documentProcessQueue() {
        return QueueBuilder.durable(TeklifOsEvents.QUEUE_DOCUMENT_PROCESS).build();
    }

    @Bean
    Binding documentReceivedBinding(Queue documentProcessQueue, TopicExchange teklifosEventsExchange) {
        return BindingBuilder.bind(documentProcessQueue)
                .to(teklifosEventsExchange)
                .with(TeklifOsEvents.RFQ_DOCUMENT_RECEIVED);
    }

    @Bean
    Queue ocrProcessQueue() {
        return QueueBuilder.durable(TeklifOsEvents.QUEUE_OCR_PROCESS).build();
    }

    @Bean
    Binding ocrRequestedBinding(Queue ocrProcessQueue, TopicExchange teklifosEventsExchange) {
        return BindingBuilder.bind(ocrProcessQueue)
                .to(teklifosEventsExchange)
                .with(TeklifOsEvents.RFQ_DOCUMENT_OCR_REQUESTED);
    }

    @Bean
    Queue rfqPipelineQueue() {
        return QueueBuilder.durable(TeklifOsEvents.QUEUE_RFQ_PIPELINE).build();
    }

    @Bean
    Binding pipelineExtractedBinding(Queue rfqPipelineQueue, TopicExchange teklifosEventsExchange) {
        return BindingBuilder.bind(rfqPipelineQueue)
                .to(teklifosEventsExchange)
                .with(TeklifOsEvents.RFQ_DOCUMENT_EXTRACTED);
    }

    @Bean
    Binding pipelineScannedBinding(Queue rfqPipelineQueue, TopicExchange teklifosEventsExchange) {
        return BindingBuilder.bind(rfqPipelineQueue)
                .to(teklifosEventsExchange)
                .with(TeklifOsEvents.RFQ_DOCUMENT_SCANNED);
    }

    @Bean
    Binding pipelineParsedBinding(Queue rfqPipelineQueue, TopicExchange teklifosEventsExchange) {
        return BindingBuilder.bind(rfqPipelineQueue)
                .to(teklifosEventsExchange)
                .with(TeklifOsEvents.RFQ_DOCUMENT_PARSED);
    }

    @Bean
    Binding pipelineOcrCompletedBinding(Queue rfqPipelineQueue, TopicExchange teklifosEventsExchange) {
        return BindingBuilder.bind(rfqPipelineQueue)
                .to(teklifosEventsExchange)
                .with(TeklifOsEvents.RFQ_DOCUMENT_OCR_COMPLETED);
    }
}
