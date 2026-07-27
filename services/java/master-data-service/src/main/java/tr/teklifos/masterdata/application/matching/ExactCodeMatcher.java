package tr.teklifos.masterdata.application.matching;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;
import tr.teklifos.masterdata.application.SkuNormalizer;
import tr.teklifos.masterdata.application.TextNormalizer;
import tr.teklifos.masterdata.domain.ProductEntity;
import tr.teklifos.masterdata.infrastructure.ProductRepository;

@Component
class ExactCodeMatcher {

    private final ProductRepository productRepository;
    private final SkuNormalizer skuNormalizer;

    ExactCodeMatcher(ProductRepository productRepository, SkuNormalizer skuNormalizer) {
        this.productRepository = productRepository;
        this.skuNormalizer = skuNormalizer;
    }

    List<MatchCandidate> match(UUID tenantId, MatchRequest request) {
        List<MatchCandidate> out = new ArrayList<>();
        if (request.customerCode() != null && !request.customerCode().isBlank()) {
            String norm = skuNormalizer.normalize(request.customerCode());
            productRepository
                    .findTop5ByTenantIdAndStatusAndSkuIgnoreCase(
                            tenantId, "ACTIVE", request.customerCode())
                    .forEach(p -> out.add(toCandidate(p, 1.0, "EXACT_SKU")));
            if (out.isEmpty()) {
                productRepository
                        .findByTenantIdAndStatus(tenantId, "ACTIVE")
                        .stream()
                        .filter(p -> skuNormalizer.normalize(p.getSku()).equals(norm))
                        .limit(1)
                        .forEach(p -> out.add(toCandidate(p, 0.99, "EXACT_SKU_NORMALIZED")));
            }
        }
        return out;
    }

    private static MatchCandidate toCandidate(ProductEntity p, double score, String source) {
        return new MatchCandidate(p.getId(), p.getSku(), score, source);
    }
}
