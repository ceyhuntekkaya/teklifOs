package tr.teklifos.identity.infrastructure;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tr.teklifos.identity.domain.PermissionEntity;

public interface PermissionRepository extends JpaRepository<PermissionEntity, UUID> {

    @Query(
            value =
                    """
            select p.* from permission p
            join role_permission rp on rp.permission_id = p.id
            join user_role ur on ur.role_id = rp.role_id
            where ur.user_id = :userId
            """,
            nativeQuery = true)
    List<PermissionEntity> findPermissionsForUser(@Param("userId") UUID userId);
}
