package tr.teklifos.identity.config;

import jakarta.annotation.PostConstruct;
import java.util.UUID;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tr.teklifos.identity.domain.AppUserEntity;
import tr.teklifos.identity.domain.RoleEntity;
import tr.teklifos.identity.domain.TenantEntity;
import tr.teklifos.identity.infrastructure.AppUserRepository;
import tr.teklifos.identity.infrastructure.PermissionRepository;
import tr.teklifos.identity.infrastructure.RoleRepository;
import tr.teklifos.identity.infrastructure.TenantRepository;
import tr.teklifos.identity.infrastructure.UserRoleRepository;

@Component
@Profile("!test")
public class DevBootstrap {

    private final TenantRepository tenantRepository;
    private final AppUserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;

    public DevBootstrap(
            TenantRepository tenantRepository,
            AppUserRepository userRepository,
            RoleRepository roleRepository,
            PermissionRepository permissionRepository,
            UserRoleRepository userRoleRepository,
            PasswordEncoder passwordEncoder) {
        this.tenantRepository = tenantRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.userRoleRepository = userRoleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostConstruct
    @Transactional
    public void seed() {
        if (tenantRepository.findBySlug("demo").isPresent()) {
            return;
        }
        TenantEntity tenant = new TenantEntity();
        tenant.setName("Demo Distribütör");
        tenant.setSlug("demo");
        tenantRepository.save(tenant);

        RoleEntity owner = new RoleEntity();
        owner.setTenantId(tenant.getId());
        owner.setCode("OWNER");
        owner.setName("Owner");
        owner.setSystemRole(true);
        roleRepository.save(owner);

        permissionRepository.findAll().forEach(p -> roleRepository.grantPermission(owner.getId(), p.getId()));

        AppUserEntity admin = new AppUserEntity();
        admin.setTenantId(tenant.getId());
        admin.setEmail("admin@demo.local");
        admin.setFullName("Demo Admin");
        admin.setPasswordHash(passwordEncoder.encode("Demo1234!"));
        userRepository.save(admin);

        userRoleRepository.assign(admin.getId(), owner.getId());
    }
}
