package tr.teklifos.identity.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "permission")
@Getter
@Setter
public class PermissionEntity {

    @Id private UUID id;

    @Column(nullable = false, unique = true, length = 128)
    private String code;

    private String description;
}
