package tr.teklifos.rfq.api;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import tr.teklifos.rfq.domain.RfqDocumentEntity;
import tr.teklifos.rfq.domain.RfqEntity;
import tr.teklifos.rfq.domain.RfqLineEntity;
import tr.teklifos.rfq.domain.ProductMatchCandidateEntity;

public record RfqDetail(
        UUID id,
        String referenceCode,
        String status,
        String sourceChannel,
        List<DocumentSummary> documents,
        List<LineSummary> lines) {

    public record DocumentSummary(
            UUID id, String fileName, String processingState, String errorMessage) {
        static DocumentSummary from(RfqDocumentEntity d) {
            return new DocumentSummary(
                    d.getId(), d.getFileName(), d.getProcessingState(), d.getErrorMessage());
        }
    }

    public record LineSummary(
            UUID id,
            int lineNumber,
            String rawDescription,
            String rawCustomerSku,
            BigDecimal quantity,
            String matchStatus,
            UUID matchedProductId,
            List<CandidateSummary> candidates) {
        static LineSummary from(
                RfqLineEntity line, List<ProductMatchCandidateEntity> candidates) {
            return new LineSummary(
                    line.getId(),
                    line.getLineNumber(),
                    line.getRawDescription(),
                    line.getRawCustomerSku(),
                    line.getQuantity(),
                    line.getMatchStatus(),
                    line.getMatchedProductId(),
                    candidates.stream().map(CandidateSummary::from).toList());
        }
    }

    public record CandidateSummary(
            UUID productId, BigDecimal score, String matchMethod, int rankOrder, boolean selected) {
        static CandidateSummary from(ProductMatchCandidateEntity c) {
            return new CandidateSummary(
                    c.getProductId(),
                    c.getScore(),
                    c.getMatchMethod(),
                    c.getRankOrder(),
                    c.isSelected());
        }
    }

    public static RfqDetail of(
            RfqEntity rfq,
            List<RfqDocumentEntity> docs,
            List<RfqLineEntity> lines,
            java.util.Map<UUID, List<ProductMatchCandidateEntity>> candidatesByLine) {
        return new RfqDetail(
                rfq.getId(),
                rfq.getReferenceCode(),
                rfq.getStatus(),
                rfq.getSourceChannel(),
                docs.stream().map(DocumentSummary::from).toList(),
                lines.stream()
                        .map(
                                l ->
                                        LineSummary.from(
                                                l,
                                                candidatesByLine.getOrDefault(l.getId(), List.of())))
                        .toList());
    }
}
