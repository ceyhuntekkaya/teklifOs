package tr.teklifos.masterdata.api;

import java.util.List;
import tr.teklifos.masterdata.application.matching.MatchStatus;

public record MatchResultBody(MatchStatus status, List<MatchCandidateBody> candidates) {}
