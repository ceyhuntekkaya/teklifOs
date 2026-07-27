package tr.teklifos.shared.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import tr.teklifos.shared.tenant.TenantContext;

public class InternalServiceAuthenticationFilter extends OncePerRequestFilter {

    public static final String HEADER_KEY = "X-TeklifOS-Internal-Key";
    public static final String HEADER_TENANT = "X-TeklifOS-Tenant-Id";

    private final TeklifosInternalProperties properties;

    public InternalServiceAuthenticationFilter(TeklifosInternalProperties properties) {
        this.properties = properties;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String configured = properties.getApiKey();
        String provided = request.getHeader(HEADER_KEY);
        String tenantHeader = request.getHeader(HEADER_TENANT);
        if (configured != null
                && !configured.isBlank()
                && configured.equals(provided)
                && tenantHeader != null
                && !tenantHeader.isBlank()) {
            UUID tenantId = UUID.fromString(tenantHeader);
            var authorities =
                    List.of(
                            new SimpleGrantedAuthority("rfq:read"),
                            new SimpleGrantedAuthority("rfq:write"),
                            new SimpleGrantedAuthority("catalog:manage"));
            var auth =
                    new UsernamePasswordAuthenticationToken("internal-service", null, authorities);
            SecurityContextHolder.getContext().setAuthentication(auth);
            TenantContext.set(tenantId, tenantId);
            try {
                filterChain.doFilter(request, response);
            } finally {
                TenantContext.clear();
                SecurityContextHolder.clearContext();
            }
            return;
        }
        filterChain.doFilter(request, response);
    }
}
