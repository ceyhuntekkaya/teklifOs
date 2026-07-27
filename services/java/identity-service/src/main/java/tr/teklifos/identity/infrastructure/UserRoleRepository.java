package tr.teklifos.identity.infrastructure;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRoleRepository extends JpaRepository<tr.teklifos.identity.domain.RoleEntity, UUID> {

    @Modifying
    @Query(
            value = "insert into user_role(user_id, role_id) values (:userId, :roleId) on conflict do nothing",
            nativeQuery = true)
    void assign(@Param("userId") UUID userId, @Param("roleId") UUID roleId);
}
