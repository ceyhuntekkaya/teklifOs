package tr.teklifos.rfq.application;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tr.teklifos.rfq.api.RfqDetail;
import tr.teklifos.rfq.domain.ProductMatchCandidateEntity;
import tr.teklifos.rfq.domain.RfqDocumentEntity;
import tr.teklifos.rfq.domain.RfqEntity;
import tr.teklifos.rfq.domain.RfqLineEntity;
import tr.teklifos.rfq.infrastructure.ProductMatchCandidateRepository;
import tr.teklifos.rfq.infrastructure.RfqDocumentRepository;
import tr.teklifos.rfq.infrastructure.RfqLineRepository;
import tr.teklifos.rfq.infrastructure.RfqRepository;
import tr.teklifos.shared.tenant.TenantContext;

@Service
public class RfqQueryService {

    private final RfqRepository rfqRepository;
    private final RfqDocumentRepository documentRepository;
    private final RfqLineRepository lineRepository;
    private final ProductMatchCandidateRepository candidateRepository;

    public RfqQueryService(
            RfqRepository rfqRepository,
            RfqDocumentRepository documentRepository,
            RfqLineRepository lineRepository,
            ProductMatchCandidateRepository candidateRepository) {
        this.rfqRepository = rfqRepository;
        this.documentRepository = documentRepository;
        this.lineRepository = lineRepository;
        this.candidateRepository = candidateRepository;
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
        List<RfqLineEntity> lines = lineRepository.findByRfqIdOrderByLineNumberAsc(rfqId);
        Map<UUID, List<ProductMatchCandidateEntity>> byLine = new HashMap<>();
        for (RfqLineEntity line : lines) {
            byLine.put(
                    line.getId(),
                    candidateRepository.findByRfqLineIdOrderByRankOrderAsc(line.getId()));
        }
        return RfqDetail.of(rfq, docs, lines, byLine);
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
