package tr.teklifos.masterdata.api;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

    @GetMapping
    public List<ProductSummary> list() {
        return List.of();
    }

    public record ProductSummary(String sku, String name, String status) {}
}
