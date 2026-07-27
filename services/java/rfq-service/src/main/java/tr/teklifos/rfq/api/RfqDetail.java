package tr.teklifos.rfq.api;

import java.util.List;
import java.util.UUID;
import tr.teklifos.rfq.domain.RfqDocumentEntity;
import tr.teklifos.rfq.domain.RfqEntity;

public record RfqDetail(
        UUID id,
        String referenceCode,
        String status,
        String sourceChannel,
        List<DocumentSummary> documents) {

    public record DocumentSummary(
            UUID id, String fileName, String processingState, String errorMessage) {
        static DocumentSummary from(RfqDocumentEntity d) {
            return new DocumentSummary(
                    d.getId(), d.getFileName(), d.getProcessingState(), d.getErrorMessage());
        }
    }

    public static RfqDetail of(RfqEntity rfq, List<RfqDocumentEntity> docs) {
        return new RfqDetail(
                rfq.getId(),
                rfq.getReferenceCode(),
                rfq.getStatus(),
                rfq.getSourceChannel(),
                docs.stream().map(DocumentSummary::from).toList());
    }
}
