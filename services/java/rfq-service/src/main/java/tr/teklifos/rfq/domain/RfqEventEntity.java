package tr.teklifos.rfq.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "rfq_event")
@Getter
@Setter
public class RfqEventEntity {

    @Id private UUID id = UUID.randomUUID();

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "rfq_id", nullable = false)
    private UUID rfqId;

    @Column(name = "event_type", nullable = false, length = 128)
    private String eventType;

    @Column(columnDefinition = "jsonb")
    private String payload;

    @Column(name = "actor_user_id")
    private UUID actorUserId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();
}
