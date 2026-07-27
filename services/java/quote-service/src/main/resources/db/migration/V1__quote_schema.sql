CREATE TABLE quote (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    quote_number VARCHAR(64) NOT NULL,
    customer_id UUID NOT NULL,
    rfq_id UUID,
    status VARCHAR(32) NOT NULL DEFAULT 'DRAFT',
    currency_code CHAR(3) NOT NULL DEFAULT 'TRY',
    current_version INT NOT NULL DEFAULT 1,
    valid_until DATE,
    created_by UUID,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (tenant_id, quote_number)
);

CREATE INDEX idx_quote_tenant_status ON quote(tenant_id, status);

CREATE TABLE quote_version (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    quote_id UUID NOT NULL REFERENCES quote(id) ON DELETE CASCADE,
    version_number INT NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'DRAFT',
    subtotal NUMERIC(20, 4) NOT NULL DEFAULT 0,
    discount_total NUMERIC(20, 4) NOT NULL DEFAULT 0,
    tax_total NUMERIC(20, 4) NOT NULL DEFAULT 0,
    grand_total NUMERIC(20, 4) NOT NULL DEFAULT 0,
    fx_rate NUMERIC(20, 8),
    fx_rate_date DATE,
    notes TEXT,
    created_by UUID,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (quote_id, version_number)
);

CREATE TABLE quote_line (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    quote_version_id UUID NOT NULL REFERENCES quote_version(id) ON DELETE CASCADE,
    line_number INT NOT NULL,
    product_id UUID NOT NULL,
    description TEXT,
    quantity NUMERIC(20, 4) NOT NULL,
    unit_code VARCHAR(16) NOT NULL DEFAULT 'EA',
    unit_price NUMERIC(20, 4) NOT NULL,
    discount_percent NUMERIC(8, 4) NOT NULL DEFAULT 0,
    line_total NUMERIC(20, 4) NOT NULL,
    price_snapshot JSONB NOT NULL DEFAULT '{}',
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (quote_version_id, line_number)
);

CREATE TABLE approval_request (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    quote_id UUID NOT NULL REFERENCES quote(id) ON DELETE CASCADE,
    quote_version_id UUID NOT NULL REFERENCES quote_version(id) ON DELETE CASCADE,
    status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    reason_code VARCHAR(64),
    reason_detail TEXT,
    requested_by UUID,
    decided_by UUID,
    decided_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_approval_request_quote ON approval_request(quote_id, status);
