package tr.teklifos.masterdata.application.matching;

import java.util.List;
import java.util.UUID;

record MatchRequest(String rawText, String customerCode, UUID customerId) {}

record MatchCandidate(UUID productId, String sku, double score, String source) {}

record MatchResult(MatchStatus status, List<MatchCandidate> candidates) {}

enum MatchStatus {
    AUTO,
    SUGGESTED,
    AMBIGUOUS,
    UNMATCHED,
    MANUAL
}
