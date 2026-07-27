package tr.teklifos.identity.infrastructure;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tr.teklifos.identity.domain.AppUserEntity;

public interface AppUserRepository extends JpaRepository<AppUserEntity, UUID> {

    @Query("select u from AppUserEntity u where lower(u.email) = lower(:email)")
    List<AppUserEntity> findAllByEmailIgnoreCase(@Param("email") String email);

    Optional<AppUserEntity> findByTenantIdAndEmailIgnoreCase(UUID tenantId, String email);
}
