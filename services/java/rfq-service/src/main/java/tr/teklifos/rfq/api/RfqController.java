package tr.teklifos.rfq.api;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import tr.teklifos.rfq.application.DocumentStorageService;
import tr.teklifos.rfq.application.RfqIntakeService;
import tr.teklifos.rfq.application.RfqQueryService;
import tr.teklifos.rfq.application.RfqStatusBroadcaster;
import tr.teklifos.rfq.domain.RfqDocumentEntity;
import tr.teklifos.rfq.domain.RfqEntity;
import tr.teklifos.shared.tenant.TenantContext;

@RestController
@RequestMapping("/api/v1/rfqs")
public class RfqController {

    private final RfqIntakeService intakeService;
    private final RfqQueryService queryService;
    private final DocumentStorageService storageService;
    private final RfqStatusBroadcaster broadcaster;

    public RfqController(
            RfqIntakeService intakeService,
            RfqQueryService queryService,
            DocumentStorageService storageService,
            RfqStatusBroadcaster broadcaster) {
        this.intakeService = intakeService;
        this.queryService = queryService;
        this.storageService = storageService;
        this.broadcaster = broadcaster;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('rfq:read')")
    public List<RfqSummary> list(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String source) {
        return queryService.list(status, source).stream().map(RfqSummary::from).toList();
    }

    @GetMapping("/{rfqId}")
    @PreAuthorize("hasAuthority('rfq:read')")
    public RfqDetail get(@PathVariable UUID rfqId) {
        return queryService.getDetail(rfqId);
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.ACCEPTED)
    @PreAuthorize("hasAuthority('rfq:write')")
    public UploadResponse upload(@RequestParam("files") List<MultipartFile> files) {
        RfqEntity rfq = intakeService.createManualUpload(files);
        return new UploadResponse(rfq.getId(), rfq.getReferenceCode(), rfq.getStatus());
    }

    @PostMapping("/{rfqId}/documents")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @PreAuthorize("hasAuthority('rfq:write')")
    public DocumentUploadResponse uploadDocument(
            @PathVariable UUID rfqId, @RequestParam("file") MultipartFile file) {
        RfqEntity rfq = queryService.requireRfq(rfqId);
        RfqDocumentEntity doc = intakeService.ingestFile(rfq, file, TenantContext.getUserId().orElse(null));
        return new DocumentUploadResponse(doc.getId(), doc.getFileName(), doc.getProcessingState());
    }

    @GetMapping("/{rfqId}/documents/{documentId}/download-url")
    @PreAuthorize("hasAuthority('rfq:read')")
    public DownloadUrlResponse downloadUrl(@PathVariable UUID rfqId, @PathVariable UUID documentId)
            throws Exception {
        RfqDocumentEntity doc = queryService.requireDocument(rfqId, documentId);
        String url = storageService.presignedGetUrl(doc.getStorageKey());
        return new DownloadUrlResponse(url);
    }

    @GetMapping("/{rfqId}/events/stream")
    @PreAuthorize("hasAuthority('rfq:read')")
    public SseEmitter stream(@PathVariable UUID rfqId) {
        UUID tenantId = TenantContext.requireTenantId();
        queryService.requireRfq(rfqId);
        return broadcaster.subscribe(tenantId, rfqId);
    }

    public record RfqSummary(
            UUID id,
            String referenceCode,
            String status,
            String sourceChannel,
            Instant receivedAt) {
        static RfqSummary from(RfqEntity e) {
            return new RfqSummary(
                    e.getId(),
                    e.getReferenceCode(),
                    e.getStatus(),
                    e.getSourceChannel(),
                    e.getReceivedAt());
        }
    }

    public record UploadResponse(UUID rfqId, String referenceCode, String status) {}

    public record DocumentUploadResponse(UUID documentId, String fileName, String processingState) {}

    public record DownloadUrlResponse(String url) {}
}
