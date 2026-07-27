package tr.teklifos.masterdata.application.matching;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;
import tr.teklifos.masterdata.application.SkuNormalizer;
import tr.teklifos.masterdata.domain.CustomerProductAliasEntity;
import tr.teklifos.masterdata.domain.ProductEntity;
import tr.teklifos.masterdata.infrastructure.CustomerProductAliasRepository;
import tr.teklifos.masterdata.infrastructure.ProductRepository;

@Component
class CustomerAliasMatcher {

    private final CustomerProductAliasRepository aliasRepository;
    private final ProductRepository productRepository;
    private final SkuNormalizer skuNormalizer;

    CustomerAliasMatcher(
            CustomerProductAliasRepository aliasRepository,
            ProductRepository productRepository,
            SkuNormalizer skuNormalizer) {
        this.aliasRepository = aliasRepository;
        this.productRepository = productRepository;
        this.skuNormalizer = skuNormalizer;
    }

    List<MatchCandidate> match(UUID tenantId, MatchRequest request) {
        if (request.customerId() == null
                || request.customerCode() == null
                || request.customerCode().isBlank()) {
            return List.of();
        }
        String norm = skuNormalizer.normalize(request.customerCode());
        List<CustomerProductAliasEntity> rows =
                aliasRepository.findByTenantIdAndCustomerIdAndNormalizedCustomerSku(
                        tenantId, request.customerId(), norm);
        List<MatchCandidate> out = new ArrayList<>();
        for (CustomerProductAliasEntity row : rows) {
            productRepository
                    .findById(row.getProductId())
                    .ifPresent(
                            p ->
                                    out.add(
                                            new MatchCandidate(
                                                    p.getId(), p.getSku(), 0.98, "CUSTOMER_ALIAS")));
        }
        return out;
    }
}
