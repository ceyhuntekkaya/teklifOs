package tr.teklifos.quote.domain;

public enum QuoteStateMachine {
    DRAFT,
    PENDING_APPROVAL,
    APPROVED,
    REJECTED,
    SENT,
    ACCEPTED,
    EXPIRED,
    CANCELLED
}
