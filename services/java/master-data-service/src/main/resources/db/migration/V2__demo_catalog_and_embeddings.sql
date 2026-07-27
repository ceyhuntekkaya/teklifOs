-- Demo catalog for matching harness (tenant id aligned with identity DevBootstrap)
INSERT INTO manufacturer (id, tenant_id, code, name)
VALUES (
    'b1000001-0000-4000-8000-000000000001',
    'a1b2c3d4-e5f6-4789-a012-3456789abcde',
    'SIEMENS',
    'Siemens'
) ON CONFLICT DO NOTHING;

INSERT INTO customer (id, tenant_id, code, name, currency_code)
VALUES (
    'c1000001-0000-4000-8000-000000000001',
    'a1b2c3d4-e5f6-4789-a012-3456789abcde',
    'DEMO-CUST',
    'Demo Müşteri A.Ş.',
    'TRY'
) ON CONFLICT DO NOTHING;

INSERT INTO product (id, tenant_id, sku, name, manufacturer_id, unit_code)
VALUES
    (
        'd1000001-0000-4000-8000-000000000001',
        'a1b2c3d4-e5f6-4789-a012-3456789abcde',
        '3RV2011-1GA10',
        'Siemens motor koruma rölesi 4,5-6,3 A',
        'b1000001-0000-4000-8000-000000000001',
        'EA'
    ),
    (
        'd1000002-0000-4000-8000-000000000002',
        'a1b2c3d4-e5f6-4789-a012-3456789abcde',
        'HYD-000873',
        'Hidrolik pompa 25 bar',
        NULL,
        'EA'
    )
ON CONFLICT DO NOTHING;

INSERT INTO product_alias (id, tenant_id, product_id, alias_text, normalized_alias, source)
VALUES (
    'e1000001-0000-4000-8000-000000000001',
    'a1b2c3d4-e5f6-4789-a012-3456789abcde',
    'd1000001-0000-4000-8000-000000000001',
    '3RV2011 1GA10',
    '3rv20111ga10',
    'seed'
) ON CONFLICT DO NOTHING;

INSERT INTO customer_product_alias (
    id, tenant_id, customer_id, product_id, customer_sku, normalized_customer_sku
)
VALUES (
    'f1000001-0000-4000-8000-000000000001',
    'a1b2c3d4-e5f6-4789-a012-3456789abcde',
    'c1000001-0000-4000-8000-000000000001',
    'd1000002-0000-4000-8000-000000000002',
    'POMPA-25',
    'pompa25'
) ON CONFLICT DO NOTHING;

CREATE TABLE IF NOT EXISTS product_embedding (
    product_id UUID PRIMARY KEY REFERENCES product(id) ON DELETE CASCADE,
    tenant_id UUID NOT NULL,
    embedding vector(384),
    model VARCHAR(64) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_product_embedding_tenant ON product_embedding(tenant_id);
