package tr.teklifos.rfq.domain;

/** Document-level processing states for the intake pipeline (Faz 4). */
public enum DocumentProcessingSaga {
    RECEIVED,
    SCANNED,
    PARSED,
    OCR,
    EXTRACTED,
    MATCHED,
    PRICED,
    READY_FOR_REVIEW,
    FAILED;

    public boolean canTransitionTo(DocumentProcessingSaga next) {
        if (this == FAILED || next == FAILED) {
            return true;
        }
        return switch (this) {
            case RECEIVED -> next == SCANNED || next == FAILED;
            case SCANNED -> next == PARSED || next == OCR || next == FAILED;
            case PARSED -> next == EXTRACTED || next == OCR || next == FAILED;
            case OCR -> next == PARSED || next == EXTRACTED || next == FAILED;
            case EXTRACTED -> next == MATCHED || next == READY_FOR_REVIEW || next == FAILED;
            case MATCHED -> next == PRICED || next == FAILED;
            case PRICED -> next == READY_FOR_REVIEW || next == FAILED;
            case READY_FOR_REVIEW -> false;
            case FAILED -> next == RECEIVED;
        };
    }
}
