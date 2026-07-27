package tr.teklifos.shared.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "teklifos.internal")
public class TeklifosInternalProperties {

    /** Shared secret for service-to-service calls (X-TeklifOS-Internal-Key). */
    private String apiKey = "dev-internal-key";

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }
}
