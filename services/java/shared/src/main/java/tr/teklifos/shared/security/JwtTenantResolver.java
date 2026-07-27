package tr.teklifos.shared.security;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
public class JwtTenantResolver {

    public static final String CLAIM_TENANT = "tenant_id";
    public static final String CLAIM_PERMISSIONS = "permissions";

    public Collection<GrantedAuthority> authorities(Jwt jwt) {
        List<String> perms = jwt.getClaimAsStringList(CLAIM_PERMISSIONS);
        if (perms == null) {
            return List.of();
        }
        return perms.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toSet());
    }
}
