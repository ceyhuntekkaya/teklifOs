package tr.teklifos.shared.messaging;

public final class TeklifOsEvents {

    public static final String EXCHANGE = "teklifos.events";

    public static final String RFQ_DOCUMENT_RECEIVED = "rfq.document.received";
    public static final String RFQ_DOCUMENT_SCANNED = "rfq.document.scanned";
    public static final String RFQ_DOCUMENT_PARSED = "rfq.document.parsed";
    public static final String RFQ_DOCUMENT_OCR_REQUESTED = "rfq.document.ocr.requested";
    public static final String RFQ_DOCUMENT_OCR_COMPLETED = "rfq.document.ocr.completed";
    public static final String RFQ_DOCUMENT_EXTRACTED = "rfq.document.extracted";

    public static final String QUEUE_DOCUMENT_PROCESS = "rfq.document.process";
    public static final String QUEUE_OCR_PROCESS = "rfq.document.ocr";
    public static final String QUEUE_RFQ_PIPELINE = "rfq.pipeline.updates";

    private TeklifOsEvents() {}
}
