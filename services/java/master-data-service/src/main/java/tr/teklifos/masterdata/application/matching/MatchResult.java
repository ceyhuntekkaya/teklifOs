package tr.teklifos.masterdata.application.matching;

import java.util.List;

public record MatchResult(MatchStatus status, List<MatchCandidate> candidates) {}
