package tr.teklifos.masterdata.api;

import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/imports")
public class ImportJobController {

    @GetMapping
    public List<ImportJobSummary> list() {
        return List.of();
    }

    public record ImportJobSummary(UUID id, String importType, String status) {}
}
