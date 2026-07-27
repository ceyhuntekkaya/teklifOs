package tr.teklifos.masterdata.application.matching;

import java.util.List;
import java.util.UUID;

public record MatchCandidate(UUID productId, String sku, double score, String source) {}
