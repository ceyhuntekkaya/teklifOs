package tr.teklifos.rfq.application;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tr.teklifos.rfq.domain.DocumentProcessingSaga;
import tr.teklifos.rfq.domain.ProductMatchCandidateEntity;
import tr.teklifos.rfq.domain.RfqDocumentEntity;
import tr.teklifos.rfq.domain.RfqEntity;
import tr.teklifos.rfq.domain.RfqLineEntity;
import tr.teklifos.rfq.infrastructure.ProductMatchCandidateRepository;
import tr.teklifos.rfq.infrastructure.RfqDocumentRepository;
import tr.teklifos.rfq.infrastructure.RfqLineRepository;
import tr.teklifos.rfq.infrastructure.RfqRepository;
import tr.teklifos.rfq.integration.MasterDataMatchClient;

@Service
public class RfqMatchingService {

    private final RfqRepository rfqRepository;
    private final RfqDocumentRepository documentRepository;
    private final RfqLineRepository lineRepository;
    private final ProductMatchCandidateRepository candidateRepository;
    private final MasterDataMatchClient matchClient;
    private final ObjectMapper objectMapper;
    private final RfqStatusBroadcaster broadcaster;

    public RfqMatchingService(
            RfqRepository rfqRepository,
            RfqDocumentRepository documentRepository,
            RfqLineRepository lineRepository,
            ProductMatchCandidateRepository candidateRepository,
            MasterDataMatchClient matchClient,
            ObjectMapper objectMapper,
            RfqStatusBroadcaster broadcaster) {
        this.rfqRepository = rfqRepository;
        this.documentRepository = documentRepository;
        this.lineRepository = lineRepository;
        this.candidateRepository = candidateRepository;
        this.matchClient = matchClient;
        this.objectMapper = objectMapper;
        this.broadcaster = broadcaster;
    }

    @Transactional
    public void onDocumentExtracted(UUID tenantId, UUID rfqId, RfqDocumentEntity doc) {
        List<ExtractedLine> extracted = parseLines(doc.getExtractedPreview());
        if (extracted.isEmpty()) {
            return;
        }
        RfqEntity rfq =
                rfqRepository.findByTenantIdAndId(tenantId, rfqId).orElseThrow();
        int baseLine = lineRepository.findByRfqIdOrderByLineNumberAsc(rfqId).size();
        for (ExtractedLine row : extracted) {
            int lineNo = baseLine + row.lineNumber();
            RfqLineEntity line =
                    lineRepository
                            .findByRfqIdAndLineNumber(rfqId, lineNo)
                            .orElseGet(
                                    () -> {
                                        RfqLineEntity l = new RfqLineEntity();
                                        l.setTenantId(tenantId);
                                        l.setRfqId(rfqId);
                                        l.setLineNumber(lineNo);
                                        return l;
                                    });
            line.setRawDescription(row.rawDescription());
            line.setRawCustomerSku(row.rawCustomerSku());
            line.setQuantity(row.quantity());
            line.setUnitCode(row.unitCode());
            line.setUpdatedAt(Instant.now());
            lineRepository.save(line);

            candidateRepository.deleteByRfqLineId(line.getId());
            MasterDataMatchClient.RemoteMatchResult result =
                    matchClient.match(
                            tenantId,
                            new MasterDataMatchClient.RemoteMatchRequest(
                                    row.rawDescription(),
                                    row.rawCustomerSku(),
                                    rfq.getCustomerId()));
            applyMatchResult(tenantId, line, result);
        }
        doc.setProcessingState(DocumentProcessingSaga.MATCHED.name());
        documentRepository.save(doc);
        rfq.setStatus(DocumentProcessingSaga.MATCHED.name());
        rfq.setUpdatedAt(Instant.now());
        rfqRepository.save(rfq);
        broadcaster.broadcast(tenantId, rfqId, DocumentProcessingSaga.MATCHED.name());
    }

    private void applyMatchResult(
            UUID tenantId, RfqLineEntity line, MasterDataMatchClient.RemoteMatchResult result) {
        if (result == null || result.candidates() == null || result.candidates().isEmpty()) {
            line.setMatchStatus("UNMATCHED");
            line.setMatchedProductId(null);
            lineRepository.save(line);
            return;
        }
        String status = result.status() != null ? result.status() : "UNMATCHED";
        line.setMatchStatus(status);
        if ("AUTO".equals(status)) {
            line.setMatchedProductId(result.candidates().getFirst().productId());
        }
        lineRepository.save(line);
        int rank = 1;
        for (MasterDataMatchClient.RemoteMatchCandidate c : result.candidates()) {
            ProductMatchCandidateEntity pc = new ProductMatchCandidateEntity();
            pc.setTenantId(tenantId);
            pc.setRfqLineId(line.getId());
            pc.setProductId(c.productId());
            pc.setScore(BigDecimal.valueOf(c.score()));
            pc.setMatchMethod(c.source());
            pc.setRankOrder(rank);
            pc.setSelected("AUTO".equals(status) && rank == 1);
            candidateRepository.save(pc);
            rank++;
        }
    }

    private List<ExtractedLine> parseLines(String extractedPreviewJson) {
        if (extractedPreviewJson == null || extractedPreviewJson.isBlank()) {
            return List.of();
        }
        try {
            JsonNode root = objectMapper.readTree(extractedPreviewJson);
            JsonNode items = root.get("lineItems");
            if (items == null || !items.isArray()) {
                return fallbackFromText(root);
            }
            List<ExtractedLine> out = new ArrayList<>();
            for (JsonNode node : items) {
                out.add(
                        new ExtractedLine(
                                node.path("lineNumber").asInt(out.size() + 1),
                                textOrNull(node, "rawDescription"),
                                textOrNull(node, "rawCustomerSku"),
                                decimalOrNull(node, "quantity"),
                                textOrNull(node, "unitCode")));
            }
            return out;
        } catch (Exception e) {
            return List.of();
        }
    }

    private List<ExtractedLine> fallbackFromText(JsonNode root) {
        String text = root.path("textPreview").asText("");
        if (text.isBlank()) {
            return List.of();
        }
        List<ExtractedLine> out = new ArrayList<>();
        int n = 1;
        for (String line : text.split("\n")) {
            if (line.isBlank()) {
                continue;
            }
            out.add(new ExtractedLine(n++, line.trim(), null, null, "EA"));
            if (n > 50) {
                break;
            }
        }
        return out;
    }

    private static String textOrNull(JsonNode node, String field) {
        JsonNode v = node.get(field);
        if (v == null || v.isNull()) {
            return null;
        }
        String s = v.asText();
        return s.isBlank() ? null : s;
    }

    private static BigDecimal decimalOrNull(JsonNode node, String field) {
        JsonNode v = node.get(field);
        if (v == null || v.isNull() || !v.isNumber()) {
            return null;
        }
        return v.decimalValue();
    }

    private record ExtractedLine(
            int lineNumber,
            String rawDescription,
            String rawCustomerSku,
            BigDecimal quantity,
            String unitCode) {}
}
