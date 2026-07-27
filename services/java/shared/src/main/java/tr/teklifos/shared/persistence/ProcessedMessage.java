package tr.teklifos.shared.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "processed_message")
@IdClass(ProcessedMessage.ProcessedMessageId.class)
@Getter
@Setter
public class ProcessedMessage {

    @Id
    @Column(name = "message_id", nullable = false, length = 128)
    private String messageId;

    @Id
    @Column(name = "consumer", nullable = false, length = 128)
    private String consumer;

    @Column(name = "processed_at", nullable = false)
    private Instant processedAt = Instant.now();

    @Getter
    @Setter
    public static class ProcessedMessageId implements Serializable {
        private String messageId;
        private String consumer;
    }
}
