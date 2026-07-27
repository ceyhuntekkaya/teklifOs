package tr.teklifos.identity.api;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tr.teklifos.identity.domain.AuditEventEntity;
import tr.teklifos.identity.infrastructure.AuditEventRepository;
import tr.teklifos.shared.tenant.TenantContext;

@RestController
@RequestMapping("/api/v1/audit")
public class AuditController {

    private final AuditEventRepository repository;

    public AuditController(AuditEventRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('audit:read')")
    public List<AuditDto> recent() {
        UUID tenantId = TenantContext.requireTenantId();
        return repository
                .findByTenantIdOrderByCreatedAtDesc(tenantId, PageRequest.of(0, 100))
                .stream()
                .map(AuditDto::from)
                .toList();
    }

    public record AuditDto(
            UUID id, String action, String resourceType, UUID resourceId, Instant createdAt) {
        static AuditDto from(AuditEventEntity e) {
            return new AuditDto(
                    e.getId(),
                    e.getAction(),
                    e.getResourceType(),
                    e.getResourceId(),
                    e.getCreatedAt());
        }
    }
}
