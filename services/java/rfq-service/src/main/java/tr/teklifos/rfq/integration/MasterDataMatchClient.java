package tr.teklifos.rfq.integration;

import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import tr.teklifos.shared.security.InternalServiceAuthenticationFilter;

@Component
public class MasterDataMatchClient {

    private final RestClient restClient;
    private final String internalApiKey;

    public MasterDataMatchClient(
            @Value("${teklifos.master-data.base-url:http://localhost:8082}") String baseUrl,
            @Value("${teklifos.internal.api-key:dev-internal-key}") String internalApiKey) {
        this.internalApiKey = internalApiKey;
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    public RemoteMatchResult match(UUID tenantId, RemoteMatchRequest request) {
        return restClient
                .post()
                .uri("/api/v1/matching/match")
                .contentType(MediaType.APPLICATION_JSON)
                .header(InternalServiceAuthenticationFilter.HEADER_KEY, internalApiKey)
                .header(InternalServiceAuthenticationFilter.HEADER_TENANT, tenantId.toString())
                .body(request)
                .retrieve()
                .body(RemoteMatchResult.class);
    }

    public record RemoteMatchRequest(String rawText, String customerCode, UUID customerId) {}

    public record RemoteMatchCandidate(UUID productId, String sku, double score, String source) {}

    public record RemoteMatchResult(String status, java.util.List<RemoteMatchCandidate> candidates) {}
}
