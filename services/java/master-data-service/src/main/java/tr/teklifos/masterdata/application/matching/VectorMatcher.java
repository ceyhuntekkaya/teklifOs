package tr.teklifos.masterdata.application.matching;

import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
class VectorMatcher {

    List<MatchCandidate> match(UUID tenantId, MatchRequest request) {
        // Embeddings populated asynchronously via ai-service; pipeline skips when empty.
        return List.of();
    }
}
