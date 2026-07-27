package tr.teklifos.identity.infrastructure;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tr.teklifos.identity.domain.RoleEntity;

public interface RoleRepository extends JpaRepository<RoleEntity, UUID> {

    @Modifying
    @Query(
            value =
                    "insert into role_permission(role_id, permission_id) values (:roleId, :permissionId) on conflict do nothing",
            nativeQuery = true)
    void grantPermission(@Param("roleId") UUID roleId, @Param("permissionId") UUID permissionId);
}
