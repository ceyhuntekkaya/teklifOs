package tr.teklifos.shared.tenant;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public final class TenantContext {

    private static final ThreadLocal<Holder> LOCAL = new ThreadLocal<>();

    private TenantContext() {}

    public static void set(UUID tenantId, UUID userId) {
        LOCAL.set(new Holder(tenantId, userId));
    }

    public static void clear() {
        LOCAL.remove();
    }

    public static void runWith(UUID tenantId, UUID userId, Runnable runnable) {
        Holder previous = LOCAL.get();
        LOCAL.set(new Holder(tenantId, userId));
        try {
            runnable.run();
        } finally {
            if (previous == null) {
                LOCAL.remove();
            } else {
                LOCAL.set(previous);
            }
        }
    }

    public static Optional<UUID> getTenantId() {
        Holder h = LOCAL.get();
        return h == null ? Optional.empty() : Optional.of(h.tenantId());
    }

    public static UUID requireTenantId() {
        return getTenantId().orElseThrow(() -> new IllegalStateException("Tenant context is not set"));
    }

    public static Optional<UUID> getUserId() {
        Holder h = LOCAL.get();
        if (h == null || h.userId() == null) {
            return Optional.empty();
        }
        return Optional.of(h.userId());
    }

    public record Holder(UUID tenantId, UUID userId) {
        public Holder {
            Objects.requireNonNull(tenantId, "tenantId");
        }
    }
}
