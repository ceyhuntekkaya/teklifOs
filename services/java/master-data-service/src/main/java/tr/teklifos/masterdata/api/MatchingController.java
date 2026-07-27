package tr.teklifos.masterdata.api;

import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tr.teklifos.masterdata.application.matching.AliasLearningService;
import tr.teklifos.masterdata.application.matching.MatchPipeline;
import tr.teklifos.masterdata.application.matching.MatchRequest;
import tr.teklifos.masterdata.application.matching.MatchResult;

@RestController
@RequestMapping("/api/v1/matching")
public class MatchingController {

    private final MatchPipeline matchPipeline;
    private final AliasLearningService aliasLearningService;

    public MatchingController(MatchPipeline matchPipeline, AliasLearningService aliasLearningService) {
        this.matchPipeline = matchPipeline;
        this.aliasLearningService = aliasLearningService;
    }

    @PostMapping("/match")
    @PreAuthorize("hasAuthority('rfq:read') or hasAuthority('catalog:manage')")
    public MatchResultBody match(@RequestBody MatchRequestBody request) {
        MatchResult result =
                matchPipeline.match(
                        new MatchRequest(
                                request.rawText(), request.customerCode(), request.customerId()));
        return new MatchResultBody(
                result.status(),
                result.candidates().stream()
                        .map(
                                c ->
                                        new MatchCandidateBody(
                                                c.productId(), c.sku(), c.score(), c.source()))
                        .toList());
    }

    @PostMapping("/aliases/learn")
    @PreAuthorize("hasAuthority('catalog:manage')")
    public void learn(@RequestBody LearnAliasBody body) {
        aliasLearningService.learnFromCorrection(
                new AliasLearningService.LearnAliasCommand(
                        body.productId(), body.customerId(), body.customerSku(), body.aliasText()));
    }

    public record LearnAliasBody(
            UUID productId, UUID customerId, String customerSku, String aliasText) {}
}
