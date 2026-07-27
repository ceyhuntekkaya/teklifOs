package tr.teklifos.shared.persistence;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;
import javax.sql.DataSource;
import org.springframework.stereotype.Component;
import tr.teklifos.shared.tenant.TenantContext;

@Component
public class TenantConnectionPreparer {

    public void setTenantOnConnection(Connection connection) throws SQLException {
        TenantContext.getTenantId()
                .ifPresent(
                        tenantId -> {
                            try (Statement st = connection.createStatement()) {
                                st.execute(
                                        "SET LOCAL app.tenant_id = '"
                                                + tenantId.toString().replace("'", "''")
                                                + "'");
                            } catch (SQLException e) {
                                throw new RuntimeException(e);
                            }
                        });
    }

    public void setTenant(DataSource dataSource, UUID tenantId) {
        try (Connection c = dataSource.getConnection();
                Statement st = c.createStatement()) {
            st.execute("SET app.tenant_id = '" + tenantId.toString().replace("'", "''") + "'");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
