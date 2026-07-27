package tr.teklifos.identity.application;

import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import tr.teklifos.identity.domain.AppUserEntity;
import tr.teklifos.identity.infrastructure.AppUserRepository;
import tr.teklifos.identity.infrastructure.TenantRepository;

@Service
public class AuthenticationService {

    private static final int MAX_ATTEMPTS = 5;

    private final AppUserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    public AuthenticationService(
            AppUserRepository userRepository,
            TenantRepository tenantRepository,
            PasswordEncoder passwordEncoder,
            TokenService tokenService) {
        this.userRepository = userRepository;
        this.tenantRepository = tenantRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
    }

    @Transactional
    public TokenService.TokenPair login(String email, String password, String tenantSlug) {
        AppUserEntity user;
        if (tenantSlug != null && !tenantSlug.isBlank()) {
            var tenant =
                    tenantRepository
                            .findBySlug(tenantSlug)
                            .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));
            user =
                    userRepository
                            .findByTenantIdAndEmailIgnoreCase(tenant.getId(), email)
                            .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));
        } else {
            user =
                    userRepository.findAllByEmailIgnoreCase(email).stream()
                            .findFirst()
                            .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));
        }

        if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(Instant.now())) {
            throw new IllegalStateException("Account locked");
        }

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            user.setFailedLoginAttempts(user.getFailedLoginAttempts() + 1);
            if (user.getFailedLoginAttempts() >= MAX_ATTEMPTS) {
                user.setLockedUntil(Instant.now().plusSeconds(900));
            }
            userRepository.save(user);
            throw new IllegalArgumentException("Invalid credentials");
        }

        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
        userRepository.save(user);
        return tokenService.issueTokens(user);
    }
}
