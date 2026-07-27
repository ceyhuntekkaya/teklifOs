package tr.teklifos.masterdata.api;

import java.util.UUID;

public record MatchRequestBody(String rawText, String customerCode, UUID customerId) {}
