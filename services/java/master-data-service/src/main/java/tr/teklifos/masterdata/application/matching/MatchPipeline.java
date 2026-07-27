package tr.teklifos.masterdata.application.matching;

import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class MatchPipeline {

    private final ExactCodeMatcher exactCodeMatcher;
    private final NormalizedMatcher normalizedMatcher;
    private final CustomerAliasMatcher customerAliasMatcher;

    public MatchPipeline(
            ExactCodeMatcher exactCodeMatcher,
            NormalizedMatcher normalizedMatcher,
            CustomerAliasMatcher customerAliasMatcher) {
        this.exactCodeMatcher = exactCodeMatcher;
        this.normalizedMatcher = normalizedMatcher;
        this.customerAliasMatcher = customerAliasMatcher;
    }

    public MatchResult match(MatchRequest request) {
        List<MatchCandidate> candidates = exactCodeMatcher.match(request);
        if (candidates.isEmpty()) {
            candidates = customerAliasMatcher.match(request);
        }
        if (candidates.isEmpty()) {
            candidates = normalizedMatcher.match(request);
        }
        return ConfidencePolicy.classify(candidates);
    }
}
