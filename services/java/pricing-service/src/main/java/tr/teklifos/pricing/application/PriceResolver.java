package tr.teklifos.pricing.application;

import java.math.BigDecimal;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class PriceResolver {

    public PriceResolution resolve(PriceResolutionRequest request) {
        throw new UnsupportedOperationException("Price resolution not implemented yet");
    }

    public record PriceResolutionRequest(
            UUID tenantId,
            UUID customerId,
            UUID productId,
            BigDecimal quantity,
            String currencyCode) {}

    public record PriceResolution(
            BigDecimal unitPrice,
            String currencyCode,
            String priceSource,
            boolean approvalRequired) {}
}
