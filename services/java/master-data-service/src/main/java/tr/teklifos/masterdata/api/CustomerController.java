package tr.teklifos.masterdata.api;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/customers")
public class CustomerController {

    @GetMapping
    public List<CustomerSummary> list() {
        return List.of();
    }

    public record CustomerSummary(String code, String name, String status) {}
}
