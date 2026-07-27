package tr.teklifos.masterdata.api;

import java.util.UUID;

public record MatchCandidateBody(UUID productId, String sku, double score, String source) {}
