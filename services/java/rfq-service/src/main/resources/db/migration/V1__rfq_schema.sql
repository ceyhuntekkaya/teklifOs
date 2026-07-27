CREATE TABLE rfq (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    reference_code VARCHAR(64) NOT NULL,
    customer_id UUID,
    status VARCHAR(32) NOT NULL DEFAULT 'RECEIVED',
    source_channel VARCHAR(64),
    received_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    due_at TIMESTAMPTZ,
    assigned_to UUID,
    notes TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (tenant_id, reference_code)
);

CREATE INDEX idx_rfq_tenant_status ON rfq(tenant_id, status);

CREATE TABLE rfq_document (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    rfq_id UUID NOT NULL REFERENCES rfq(id) ON DELETE CASCADE,
    file_name VARCHAR(512) NOT NULL,
    content_type VARCHAR(128) NOT NULL,
    storage_key VARCHAR(1024) NOT NULL,
    file_size_bytes BIGINT,
    processing_state VARCHAR(64) NOT NULL DEFAULT 'RECEIVED',
    checksum_sha256 VARCHAR(64),
    uploaded_by UUID,
    uploaded_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_rfq_document_rfq ON rfq_document(rfq_id);

CREATE TABLE rfq_line (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    rfq_id UUID NOT NULL REFERENCES rfq(id) ON DELETE CASCADE,
    line_number INT NOT NULL,
    raw_description TEXT,
    raw_customer_sku VARCHAR(128),
    quantity NUMERIC(20, 4),
    unit_code VARCHAR(16),
    requested_delivery_date DATE,
    match_status VARCHAR(32) NOT NULL DEFAULT 'UNMATCHED',
    matched_product_id UUID,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (rfq_id, line_number)
);

CREATE TABLE product_match_candidate (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    rfq_line_id UUID NOT NULL REFERENCES rfq_line(id) ON DELETE CASCADE,
    product_id UUID NOT NULL,
    score NUMERIC(8, 4) NOT NULL,
    match_method VARCHAR(64) NOT NULL,
    rank_order INT NOT NULL DEFAULT 1,
    selected BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_match_candidate_line ON product_match_candidate(rfq_line_id);
