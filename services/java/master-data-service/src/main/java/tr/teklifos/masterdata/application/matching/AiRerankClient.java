package tr.teklifos.masterdata.application.matching;

import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class AiRerankClient {

    private final RestClient restClient;
    private final String internalApiKey;

    public AiRerankClient(
            @Value("${teklifos.ai.base-url:http://localhost:9004}") String aiBaseUrl,
            @Value("${teklifos.internal.api-key:dev-internal-key}") String internalApiKey) {
        this.internalApiKey = internalApiKey;
        this.restClient = RestClient.builder().baseUrl(aiBaseUrl).build();
    }

    List<MatchCandidate> rerankIfAvailable(MatchRequest request, List<MatchCandidate> candidates) {
        if (candidates.size() < 2) {
            return candidates;
        }
        try {
            RerankResponse response =
                    restClient
                            .post()
                            .uri("/ai/rerank")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("X-TeklifOS-Internal-Key", internalApiKey)
                            .body(
                                    new RerankRequest(
                                            request.rawText(),
                                            request.customerCode(),
                                            candidates.stream()
                                                    .map(
                                                            c ->
                                                                    new RerankCandidate(
                                                                            c.productId(),
                                                                            c.sku(),
                                                                            c.score(),
                                                                            c.source()))
                                                    .toList()))
                            .retrieve()
                            .body(RerankResponse.class);
            if (response == null || response.candidates() == null) {
                return candidates;
            }
            return response.candidates().stream()
                    .map(c -> new MatchCandidate(c.productId(), c.sku(), c.score(), c.source()))
                    .toList();
        } catch (Exception ignored) {
            return candidates;
        }
    }

    record RerankRequest(String rawText, String customerCode, List<RerankCandidate> candidates) {}

    record RerankCandidate(java.util.UUID productId, String sku, double score, String source) {}

    record RerankResponse(List<RerankCandidate> candidates) {}
}
