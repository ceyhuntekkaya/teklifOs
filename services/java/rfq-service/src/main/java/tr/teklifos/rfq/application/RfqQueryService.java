package tr.teklifos.rfq.application;

import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tr.teklifos.rfq.api.RfqDetail;
import tr.teklifos.rfq.domain.RfqDocumentEntity;
import tr.teklifos.rfq.domain.RfqEntity;
import tr.teklifos.rfq.infrastructure.RfqDocumentRepository;
import tr.teklifos.rfq.infrastructure.RfqRepository;
import tr.teklifos.shared.tenant.TenantContext;

@Service
public class RfqQueryService {

    private final RfqRepository rfqRepository;
    private final RfqDocumentRepository documentRepository;

    public RfqQueryService(RfqRepository rfqRepository, RfqDocumentRepository documentRepository) {
        this.rfqRepository = rfqRepository;
        this.documentRepository = documentRepository;
    }

    @Transactional(readOnly = true)
    public List<RfqEntity> list(String status, String source) {
        UUID tenantId = TenantContext.requireTenantId();
        return rfqRepository.findByTenantIdOrderByReceivedAtDesc(tenantId).stream()
                .filter(r -> status == null || status.isBlank() || status.equals(r.getStatus()))
                .filter(
                        r ->
                                source == null
                                        || source.isBlank()
                                        || source.equals(r.getSourceChannel()))
                .toList();
    }

    @Transactional(readOnly = true)
    public RfqDetail getDetail(UUID rfqId) {
        RfqEntity rfq = requireRfq(rfqId);
        List<RfqDocumentEntity> docs = documentRepository.findByRfqIdOrderByUploadedAtAsc(rfqId);
        return RfqDetail.of(rfq, docs);
    }

    public RfqEntity requireRfq(UUID rfqId) {
        UUID tenantId = TenantContext.requireTenantId();
        return rfqRepository
                .findByTenantIdAndId(tenantId, rfqId)
                .orElseThrow(() -> new java.util.NoSuchElementException("RFQ not found"));
    }

    public RfqDocumentEntity requireDocument(UUID rfqId, UUID documentId) {
        UUID tenantId = TenantContext.requireTenantId();
        RfqDocumentEntity doc =
                documentRepository
                        .findByTenantIdAndId(tenantId, documentId)
                        .orElseThrow(() -> new java.util.NoSuchElementException("Document not found"));
        if (!doc.getRfqId().equals(rfqId)) {
            throw new java.util.NoSuchElementException("Document not found");
        }
        return doc;
    }
}
