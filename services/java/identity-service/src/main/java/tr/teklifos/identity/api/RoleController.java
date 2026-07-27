package tr.teklifos.identity.api;

import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tr.teklifos.identity.domain.RoleEntity;
import tr.teklifos.identity.infrastructure.RoleRepository;
import tr.teklifos.shared.tenant.TenantContext;

@RestController
@RequestMapping("/api/v1/roles")
public class RoleController {

    private final RoleRepository roleRepository;

    public RoleController(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('settings:manage')")
    public List<RoleDto> list() {
        UUID tenantId = TenantContext.requireTenantId();
        return roleRepository.findAll().stream()
                .filter(r -> r.getTenantId().equals(tenantId))
                .map(RoleDto::from)
                .toList();
    }

    public record RoleDto(UUID id, String code, String name) {
        static RoleDto from(RoleEntity r) {
            return new RoleDto(r.getId(), r.getCode(), r.getName());
        }
    }
}
