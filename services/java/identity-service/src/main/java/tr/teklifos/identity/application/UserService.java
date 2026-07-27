package tr.teklifos.identity.application;

import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tr.teklifos.identity.domain.AppUserEntity;
import tr.teklifos.identity.infrastructure.AppUserRepository;
import tr.teklifos.shared.tenant.TenantContext;

@Service
public class UserService {

    private final AppUserRepository repository;

    public UserService(AppUserRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<AppUserEntity> listUsers() {
        UUID tenantId = TenantContext.requireTenantId();
        return repository.findAll().stream().filter(u -> u.getTenantId().equals(tenantId)).toList();
    }
}
