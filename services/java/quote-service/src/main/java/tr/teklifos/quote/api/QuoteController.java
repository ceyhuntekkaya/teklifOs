package tr.teklifos.quote.api;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tr.teklifos.quote.domain.QuoteStateMachine;

@RestController
@RequestMapping("/api/v1/quotes")
public class QuoteController {

    @GetMapping
    public List<QuoteSummary> list() {
        return List.of();
    }

    public record QuoteSummary(String quoteNumber, QuoteStateMachine status) {}
}
