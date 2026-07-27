package tr.teklifos.masterdata.application.matching;

import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tr.teklifos.masterdata.application.SkuNormalizer;
import tr.teklifos.masterdata.application.TextNormalizer;
import tr.teklifos.masterdata.domain.CustomerProductAliasEntity;
import tr.teklifos.masterdata.domain.ProductAliasRow;
import tr.teklifos.masterdata.infrastructure.CustomerProductAliasRepository;
import tr.teklifos.masterdata.infrastructure.ProductAliasRepository;
import tr.teklifos.shared.tenant.TenantContext;

@Service
public class AliasLearningService {

    private final ProductAliasRepository productAliasRepository;
    private final CustomerProductAliasRepository customerProductAliasRepository;
    private final TextNormalizer textNormalizer;
    private final SkuNormalizer skuNormalizer;

    public AliasLearningService(
            ProductAliasRepository productAliasRepository,
            CustomerProductAliasRepository customerProductAliasRepository,
            TextNormalizer textNormalizer,
            SkuNormalizer skuNormalizer) {
        this.productAliasRepository = productAliasRepository;
        this.customerProductAliasRepository = customerProductAliasRepository;
        this.textNormalizer = textNormalizer;
        this.skuNormalizer = skuNormalizer;
    }

    @Transactional
    public void learnFromCorrection(LearnAliasCommand cmd) {
        UUID tenantId = TenantContext.requireTenantId();
        if (cmd.customerId() != null && cmd.customerSku() != null && !cmd.customerSku().isBlank()) {
            String norm = skuNormalizer.normalize(cmd.customerSku());
            CustomerProductAliasEntity row = new CustomerProductAliasEntity();
            row.setTenantId(tenantId);
            row.setCustomerId(cmd.customerId());
            row.setProductId(cmd.productId());
            row.setCustomerSku(cmd.customerSku().trim());
            row.setNormalizedCustomerSku(norm);
            customerProductAliasRepository.save(row);
            return;
        }
        if (cmd.aliasText() != null && !cmd.aliasText().isBlank()) {
            ProductAliasRow row = new ProductAliasRow();
            row.setTenantId(tenantId);
            row.setProductId(cmd.productId());
            row.setAliasText(cmd.aliasText().trim());
            row.setNormalizedAlias(textNormalizer.normalize(cmd.aliasText()));
            row.setSource("user_correction");
            productAliasRepository.save(row);
        }
    }

    public record LearnAliasCommand(
            UUID productId, UUID customerId, String customerSku, String aliasText) {}
}
