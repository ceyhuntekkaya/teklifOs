INSERT INTO permission (id, code, description) VALUES
    (gen_random_uuid(), 'rfq:read', 'View RFQs'),
    (gen_random_uuid(), 'rfq:write', 'Edit RFQs'),
    (gen_random_uuid(), 'rfq:assign', 'Assign RFQs'),
    (gen_random_uuid(), 'quote:create', 'Create quotes'),
    (gen_random_uuid(), 'quote:send', 'Send quotes'),
    (gen_random_uuid(), 'quote:approve', 'Approve quotes'),
    (gen_random_uuid(), 'pricing:override', 'Override pricing limits'),
    (gen_random_uuid(), 'catalog:manage', 'Manage catalog'),
    (gen_random_uuid(), 'settings:manage', 'Manage settings'),
    (gen_random_uuid(), 'audit:read', 'View audit log');
