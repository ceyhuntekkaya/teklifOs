ALTER TABLE rfq_document
    ADD COLUMN IF NOT EXISTS error_message TEXT,
    ADD COLUMN IF NOT EXISTS extracted_preview JSONB,
    ADD COLUMN IF NOT EXISTS page_count INT;

CREATE TABLE processing_job (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    rfq_id UUID NOT NULL REFERENCES rfq(id) ON DELETE CASCADE,
    rfq_document_id UUID REFERENCES rfq_document(id) ON DELETE CASCADE,
    stage VARCHAR(64) NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'RUNNING',
    attempt INT NOT NULL DEFAULT 1,
    error_message TEXT,
    correlation_id VARCHAR(64),
    started_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    finished_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_processing_job_rfq ON processing_job(rfq_id, created_at DESC);

CREATE TABLE rfq_event (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    rfq_id UUID NOT NULL REFERENCES rfq(id) ON DELETE CASCADE,
    event_type VARCHAR(128) NOT NULL,
    payload JSONB,
    actor_user_id UUID,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_rfq_event_rfq ON rfq_event(rfq_id, created_at DESC);

ALTER TABLE rfq ENABLE ROW LEVEL SECURITY;
ALTER TABLE rfq_document ENABLE ROW LEVEL SECURITY;

CREATE POLICY tenant_isolation_rfq ON rfq
    USING (tenant_id = current_setting('app.tenant_id', true)::uuid);

CREATE POLICY tenant_isolation_rfq_document ON rfq_document
    USING (tenant_id = current_setting('app.tenant_id', true)::uuid);
