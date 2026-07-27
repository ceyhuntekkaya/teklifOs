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
@Table(name = "processing_job")
@Getter
@Setter
public class ProcessingJobEntity {

    @Id private UUID id = UUID.randomUUID();

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "rfq_id", nullable = false)
    private UUID rfqId;

    @Column(name = "rfq_document_id")
    private UUID rfqDocumentId;

    @Column(nullable = false, length = 64)
    private String stage;

    @Column(nullable = false, length = 32)
    private String status = "RUNNING";

    @Column(nullable = false)
    private int attempt = 1;

    @Column(name = "error_message")
    private String errorMessage;

    @Column(name = "correlation_id", length = 64)
    private String correlationId;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt = Instant.now();

    @Column(name = "finished_at")
    private Instant finishedAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();
}
