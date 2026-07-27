package tr.teklifos.masterdata.application.matching;

import java.util.UUID;

public record MatchRequest(String rawText, String customerCode, UUID customerId) {}
