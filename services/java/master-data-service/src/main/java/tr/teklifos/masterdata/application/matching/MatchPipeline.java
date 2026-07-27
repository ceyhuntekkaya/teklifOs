package tr.teklifos.masterdata.application.matching;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Component;
import tr.teklifos.shared.tenant.TenantContext;

@Component
public class MatchPipeline {

    private final ExactCodeMatcher exactCodeMatcher;
    private final CustomerAliasMatcher customerAliasMatcher;
    private final NormalizedMatcher normalizedMatcher;
    private final TrigramMatcher trigramMatcher;
    private final VectorMatcher vectorMatcher;
    private final AiRerankClient aiRerankClient;
    private final ConfidencePolicy confidencePolicy;

    public MatchPipeline(
            ExactCodeMatcher exactCodeMatcher,
            CustomerAliasMatcher customerAliasMatcher,
            NormalizedMatcher normalizedMatcher,
            TrigramMatcher trigramMatcher,
            VectorMatcher vectorMatcher,
            AiRerankClient aiRerankClient,
            ConfidencePolicy confidencePolicy) {
        this.exactCodeMatcher = exactCodeMatcher;
        this.customerAliasMatcher = customerAliasMatcher;
        this.normalizedMatcher = normalizedMatcher;
        this.trigramMatcher = trigramMatcher;
        this.vectorMatcher = vectorMatcher;
        this.aiRerankClient = aiRerankClient;
        this.confidencePolicy = confidencePolicy;
    }

    public MatchResult match(MatchRequest request) {
        UUID tenantId = TenantContext.requireTenantId();
        List<MatchCandidate> merged = new ArrayList<>();
        merged.addAll(exactCodeMatcher.match(tenantId, request));
        if (merged.isEmpty()) {
            merged.addAll(customerAliasMatcher.match(tenantId, request));
        }
        if (merged.isEmpty()) {
            merged.addAll(normalizedMatcher.match(tenantId, request));
        }
        if (merged.isEmpty()) {
            merged.addAll(trigramMatcher.match(tenantId, request));
        }
        if (merged.isEmpty()) {
            merged.addAll(vectorMatcher.match(tenantId, request));
        }
        merged = dedupe(merged);
        merged = aiRerankClient.rerankIfAvailable(request, merged);
        merged.sort(Comparator.comparingDouble(MatchCandidate::score).reversed());
        return confidencePolicy.classify(merged);
    }

    private static List<MatchCandidate> dedupe(List<MatchCandidate> input) {
        Map<UUID, MatchCandidate> best = new LinkedHashMap<>();
        for (MatchCandidate c : input) {
            best.merge(
                    c.productId(),
                    c,
                    (a, b) -> a.score() >= b.score() ? a : b);
        }
        return best.values().stream().toList();
    }
}
