package tr.teklifos.masterdata.application.matching;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;
import tr.teklifos.masterdata.infrastructure.ProductRepository;

@Component
class TrigramMatcher {

    private final ProductRepository productRepository;

    TrigramMatcher(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    List<MatchCandidate> match(UUID tenantId, MatchRequest request) {
        String q = request.rawText();
        if (q == null || q.isBlank()) {
            q = request.customerCode();
        }
        if (q == null || q.isBlank()) {
            return List.of();
        }
        List<Object[]> rows = productRepository.searchTrigram(tenantId, q, 8);
        List<MatchCandidate> out = new ArrayList<>();
        for (Object[] row : rows) {
            UUID productId = (UUID) row[0];
            String sku = (String) row[1];
            double score = ((Number) row[2]).doubleValue();
            out.add(new MatchCandidate(productId, sku, Math.min(0.94, score), "TRIGRAM"));
        }
        return out;
    }
}
