package tr.teklifos.pricing.api;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tr.teklifos.pricing.application.PriceResolver;

@RestController
@RequestMapping("/api/v1/pricing")
public class PricingController {

    private final PriceResolver priceResolver;

    public PricingController(PriceResolver priceResolver) {
        this.priceResolver = priceResolver;
    }

    @PostMapping("/calculate")
    public CalculatePriceResponse calculate(@RequestBody CalculatePriceRequest body) {
        var resolution =
                priceResolver.resolve(
                        new PriceResolver.PriceResolutionRequest(
                                body.tenantId(),
                                body.customerId(),
                                body.productId(),
                                body.quantity(),
                                body.currencyCode()));
        return new CalculatePriceResponse(
                resolution.unitPrice(),
                resolution.currencyCode(),
                resolution.priceSource(),
                resolution.approvalRequired());
    }

    public record CalculatePriceRequest(
            java.util.UUID tenantId,
            java.util.UUID customerId,
            java.util.UUID productId,
            java.math.BigDecimal quantity,
            String currencyCode) {}

    public record CalculatePriceResponse(
            java.math.BigDecimal unitPrice,
            String currencyCode,
            String priceSource,
            boolean approvalRequired) {}
}
