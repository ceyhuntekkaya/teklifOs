package tr.teklifos.masterdata.application.matching;

import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class ConfidencePolicy {

    public MatchResult classify(List<MatchCandidate> candidates) {
        if (candidates.isEmpty()) {
            return new MatchResult(MatchStatus.UNMATCHED, List.of());
        }
        if (candidates.getFirst().score() >= 0.95) {
            return new MatchResult(MatchStatus.AUTO, candidates);
        }
        if (candidates.size() >= 2
                && Math.abs(candidates.get(0).score() - candidates.get(1).score()) < 0.03) {
            return new MatchResult(MatchStatus.AMBIGUOUS, candidates);
        }
        if (candidates.getFirst().score() < 0.5) {
            return new MatchResult(MatchStatus.UNMATCHED, candidates);
        }
        return new MatchResult(MatchStatus.SUGGESTED, candidates);
    }
}
