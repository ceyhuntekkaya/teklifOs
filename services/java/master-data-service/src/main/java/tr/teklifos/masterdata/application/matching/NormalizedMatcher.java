package tr.teklifos.masterdata.application.matching;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;
import tr.teklifos.masterdata.application.SkuNormalizer;
import tr.teklifos.masterdata.application.TextNormalizer;
import tr.teklifos.masterdata.domain.ProductAliasRow;
import tr.teklifos.masterdata.domain.ProductEntity;
import tr.teklifos.masterdata.infrastructure.ProductAliasRepository;
import tr.teklifos.masterdata.infrastructure.ProductRepository;

@Component
class NormalizedMatcher {

    private final ProductAliasRepository productAliasRepository;
    private final ProductRepository productRepository;
    private final TextNormalizer textNormalizer;
    private final SkuNormalizer skuNormalizer;

    NormalizedMatcher(
            ProductAliasRepository productAliasRepository,
            ProductRepository productRepository,
            TextNormalizer textNormalizer,
            SkuNormalizer skuNormalizer) {
        this.productAliasRepository = productAliasRepository;
        this.productRepository = productRepository;
        this.textNormalizer = textNormalizer;
        this.skuNormalizer = skuNormalizer;
    }

    List<MatchCandidate> match(UUID tenantId, MatchRequest request) {
        String query = request.rawText();
        if (query == null || query.isBlank()) {
            query = request.customerCode();
        }
        if (query == null || query.isBlank()) {
            return List.of();
        }
        List<MatchCandidate> out = new ArrayList<>();
        String normalized = textNormalizer.normalize(query);
        if (request.customerCode() != null && !request.customerCode().isBlank()) {
            String skuNorm = skuNormalizer.normalize(request.customerCode());
            for (ProductAliasRow alias :
                    productAliasRepository.findByTenantAndNormalized(tenantId, skuNorm)) {
                productRepository
                        .findById(alias.getProductId())
                        .ifPresent(
                                p ->
                                        out.add(
                                                new MatchCandidate(
                                                        p.getId(), p.getSku(), 0.96, "PRODUCT_ALIAS")));
            }
            if (!out.isEmpty()) {
                return out;
            }
        }
        for (ProductAliasRow alias :
                productAliasRepository.findByTenantAndNormalized(tenantId, normalized)) {
            productRepository
                    .findById(alias.getProductId())
                    .ifPresent(
                            p ->
                                    out.add(
                                            new MatchCandidate(
                                                    p.getId(), p.getSku(), 0.96, "PRODUCT_ALIAS")));
        }
        if (!out.isEmpty()) {
            return out;
        }
        for (ProductEntity p : productRepository.findByTenantIdAndStatus(tenantId, "ACTIVE")) {
            if (textNormalizer.normalize(p.getName()).contains(normalized)
                    || normalized.contains(textNormalizer.normalize(p.getSku()))) {
                out.add(new MatchCandidate(p.getId(), p.getSku(), 0.85, "NORMALIZED_TEXT"));
            }
        }
        return out.stream().limit(5).toList();
    }
}
