package tr.teklifos.rfq.application;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tr.teklifos.rfq.domain.DocumentProcessingSaga;
import tr.teklifos.rfq.domain.ProcessingJobEntity;
import tr.teklifos.rfq.domain.RfqDocumentEntity;
import tr.teklifos.rfq.domain.RfqEntity;
import tr.teklifos.rfq.infrastructure.ProcessingJobRepository;
import tr.teklifos.rfq.infrastructure.RfqDocumentRepository;
import tr.teklifos.rfq.infrastructure.RfqRepository;
import tr.teklifos.rfq.infrastructure.persistence.RfqProcessedMessage;
import tr.teklifos.rfq.infrastructure.ProcessedMessageRepository;

@Service
public class DocumentProcessingOrchestrator {

    private final RfqRepository rfqRepository;
    private final RfqDocumentRepository documentRepository;
    private final ProcessingJobRepository jobRepository;
    private final ProcessedMessageRepository processedMessageRepository;
    private final RfqStatusBroadcaster broadcaster;
    private final ObjectMapper objectMapper;

    public DocumentProcessingOrchestrator(
            RfqRepository rfqRepository,
            RfqDocumentRepository documentRepository,
            ProcessingJobRepository jobRepository,
            ProcessedMessageRepository processedMessageRepository,
            RfqStatusBroadcaster broadcaster,
            ObjectMapper objectMapper) {
        this.rfqRepository = rfqRepository;
        this.documentRepository = documentRepository;
        this.jobRepository = jobRepository;
        this.processedMessageRepository = processedMessageRepository;
        this.broadcaster = broadcaster;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public void handlePipelineMessage(String messageId, String consumer, String body) {
        RfqProcessedMessage.ProcessedMessageId id = new RfqProcessedMessage.ProcessedMessageId();
        id.setMessageId(messageId);
        id.setConsumer(consumer);
        if (processedMessageRepository.existsById(id)) {
            return;
        }
        try {
            JsonNode root = objectMapper.readTree(body);
            if (root.hasNonNull("messageId")) {
                messageId = root.get("messageId").asText();
                id.setMessageId(messageId);
                if (processedMessageRepository.existsById(id)) {
                    return;
                }
            }
            UUID tenantId = UUID.fromString(root.get("tenantId").asText());
            UUID documentId = UUID.fromString(root.get("documentId").asText());
            String stage = root.get("stage").asText();
            String error = root.hasNonNull("error") ? root.get("error").asText() : null;
            String preview =
                    root.has("extractedPreview") ? root.get("extractedPreview").toString() : null;

            RfqDocumentEntity doc =
                    documentRepository
                            .findByTenantIdAndId(tenantId, documentId)
                            .orElseThrow();

            DocumentProcessingSaga target = DocumentProcessingSaga.valueOf(stage);
            DocumentProcessingSaga current = DocumentProcessingSaga.valueOf(doc.getProcessingState());
            if (!current.canTransitionTo(target) && current != target) {
                throw new IllegalStateException("Invalid transition " + current + " -> " + target);
            }

            doc.setProcessingState(target.name());
            if (error != null) {
                doc.setErrorMessage(error);
                doc.setProcessingState(DocumentProcessingSaga.FAILED.name());
            }
            if (preview != null) {
                doc.setExtractedPreview(preview);
            }
            documentRepository.save(doc);

            completeJob(doc, target.name(), error);

            RfqEntity rfq =
                    rfqRepository
                            .findByTenantIdAndId(tenantId, doc.getRfqId())
                            .orElseThrow();
            maybeAdvanceRfq(rfq);
            broadcaster.broadcast(tenantId, rfq.getId(), doc.getProcessingState());

            RfqProcessedMessage pm = new RfqProcessedMessage();
            pm.setMessageId(messageId);
            pm.setConsumer(consumer);
            processedMessageRepository.save(pm);
        } catch (Exception e) {
            throw new IllegalStateException("Pipeline message handling failed", e);
        }
    }

    private void completeJob(RfqDocumentEntity doc, String stage, String error) {
        ProcessingJobEntity job = new ProcessingJobEntity();
        job.setTenantId(doc.getTenantId());
        job.setRfqId(doc.getRfqId());
        job.setRfqDocumentId(doc.getId());
        job.setStage(stage);
        job.setStatus(error == null ? "SUCCEEDED" : "FAILED");
        job.setErrorMessage(error);
        job.setFinishedAt(Instant.now());
        jobRepository.save(job);
    }

    private void maybeAdvanceRfq(RfqEntity rfq) {
        List<RfqDocumentEntity> docs = documentRepository.findByRfqIdOrderByUploadedAtAsc(rfq.getId());
        boolean allExtracted =
                !docs.isEmpty()
                        && docs.stream()
                                .allMatch(
                                        d ->
                                                DocumentProcessingSaga.EXTRACTED
                                                                .name()
                                                                .equals(d.getProcessingState())
                                                        || DocumentProcessingSaga.READY_FOR_REVIEW
                                                                .name()
                                                                .equals(d.getProcessingState()));
        if (allExtracted) {
            docs.forEach(
                    d -> {
                        if (DocumentProcessingSaga.EXTRACTED.name().equals(d.getProcessingState())) {
                            d.setProcessingState(DocumentProcessingSaga.READY_FOR_REVIEW.name());
                            documentRepository.save(d);
                        }
                    });
            rfq.setStatus(DocumentProcessingSaga.READY_FOR_REVIEW.name());
            rfq.setUpdatedAt(Instant.now());
            rfqRepository.save(rfq);
        }
    }
}
