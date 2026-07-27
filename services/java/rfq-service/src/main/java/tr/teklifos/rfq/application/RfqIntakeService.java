package tr.teklifos.rfq.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.Year;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import tr.teklifos.rfq.domain.DocumentProcessingSaga;
import tr.teklifos.rfq.domain.ProcessingJobEntity;
import tr.teklifos.rfq.domain.RfqDocumentEntity;
import tr.teklifos.rfq.domain.RfqEntity;
import tr.teklifos.rfq.infrastructure.ProcessingJobRepository;
import tr.teklifos.rfq.infrastructure.RfqDocumentRepository;
import tr.teklifos.rfq.infrastructure.RfqRepository;
import tr.teklifos.shared.messaging.TeklifOsEvents;
import tr.teklifos.shared.tenant.TenantContext;

@Service
public class RfqIntakeService {

    private static final long MAX_FILE_BYTES = 25L * 1024 * 1024;

    private final RfqRepository rfqRepository;
    private final RfqDocumentRepository documentRepository;
    private final ProcessingJobRepository jobRepository;
    private final DocumentStorageService storageService;
    private final RfqEventPublisher eventPublisher;
    private final RfqStatusBroadcaster statusBroadcaster;

    public RfqIntakeService(
            RfqRepository rfqRepository,
            RfqDocumentRepository documentRepository,
            ProcessingJobRepository jobRepository,
            DocumentStorageService storageService,
            RfqEventPublisher eventPublisher,
            RfqStatusBroadcaster statusBroadcaster) {
        this.rfqRepository = rfqRepository;
        this.documentRepository = documentRepository;
        this.jobRepository = jobRepository;
        this.storageService = storageService;
        this.eventPublisher = eventPublisher;
        this.statusBroadcaster = statusBroadcaster;
    }

    @Transactional
    public RfqEntity createManualUpload(List<MultipartFile> files) {
        UUID tenantId = TenantContext.requireTenantId();
        UUID userId = TenantContext.getUserId().orElse(null);

        RfqEntity rfq = new RfqEntity();
        rfq.setTenantId(tenantId);
        rfq.setReferenceCode(nextReference(tenantId));
        rfq.setSourceChannel("MANUAL_UPLOAD");
        rfq.setStatus(DocumentProcessingSaga.RECEIVED.name());
        rfqRepository.save(rfq);

        for (MultipartFile file : files) {
            ingestFile(rfq, file, userId);
        }
        statusBroadcaster.broadcast(tenantId, rfq.getId(), rfq.getStatus());
        return rfq;
    }

    @Transactional
    public RfqDocumentEntity ingestFile(RfqEntity rfq, MultipartFile file, UUID uploadedBy) {
        if (file.getSize() > MAX_FILE_BYTES) {
            throw new IllegalArgumentException("File too large");
        }
        UUID tenantId = rfq.getTenantId();
        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to read upload", e);
        }
        String checksum = sha256(bytes);
        documentRepository
                .findByTenantIdAndChecksumSha256(tenantId, checksum)
                .ifPresent(
                        d -> {
                            throw new IllegalStateException(
                                    "Duplicate document already uploaded");
                        });

        RfqDocumentEntity doc = new RfqDocumentEntity();
        doc.setTenantId(tenantId);
        doc.setRfqId(rfq.getId());
        doc.setFileName(file.getOriginalFilename() != null ? file.getOriginalFilename() : "file");
        doc.setContentType(
                file.getContentType() != null ? file.getContentType() : "application/octet-stream");
        doc.setFileSizeBytes(file.getSize());
        doc.setChecksumSha256(checksum);
        doc.setUploadedBy(uploadedBy);
        doc.setProcessingState(DocumentProcessingSaga.RECEIVED.name());
        documentRepository.save(doc);

        String key = storageService.originalKey(tenantId, rfq.getId(), doc.getId(), doc.getFileName());
        doc.setStorageKey(key);
        try {
            storageService.putOriginal(
                    key, new java.io.ByteArrayInputStream(bytes), bytes.length, doc.getContentType());
        } catch (Exception e) {
            throw new IllegalStateException("Storage upload failed", e);
        }
        documentRepository.save(doc);

        startJob(tenantId, rfq.getId(), doc.getId(), DocumentProcessingSaga.RECEIVED.name());

        Map<String, Object> payload =
                Map.of(
                        "messageId",
                        UUID.randomUUID().toString(),
                        "tenantId",
                        tenantId.toString(),
                        "rfqId",
                        rfq.getId().toString(),
                        "documentId",
                        doc.getId().toString(),
                        "storageKey",
                        key,
                        "fileName",
                        doc.getFileName(),
                        "contentType",
                        doc.getContentType(),
                        "checksumSha256",
                        checksum);

        eventPublisher.publishDocumentEvent(
                tenantId,
                rfq.getId(),
                doc.getId(),
                TeklifOsEvents.RFQ_DOCUMENT_RECEIVED,
                payload,
                uploadedBy);

        statusBroadcaster.broadcast(tenantId, rfq.getId(), doc.getProcessingState());
        return doc;
    }

    private void startJob(UUID tenantId, UUID rfqId, UUID documentId, String stage) {
        ProcessingJobEntity job = new ProcessingJobEntity();
        job.setTenantId(tenantId);
        job.setRfqId(rfqId);
        job.setRfqDocumentId(documentId);
        job.setStage(stage);
        job.setStatus("RUNNING");
        jobRepository.save(job);
    }

    private String nextReference(UUID tenantId) {
        String year = String.valueOf(Year.now().getValue());
        long count = rfqRepository.findByTenantIdOrderByReceivedAtDesc(tenantId).size() + 1;
        return "RFQ-" + year + "-" + String.format("%05d", count);
    }

    private static String sha256(byte[] data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(data));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
