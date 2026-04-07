-- V13: Seed default admin user
-- Password: Admin@1234 (BCrypt hash, strength=12)

INSERT INTO users (id, email, password_hash, full_name, role, is_active)
VALUES (
    'a0000000-0000-0000-0000-000000000001',
    'admin@placementportal.edu',
    '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewdBPj4oU3sBdJtW',
    'System Administrator',
    'ADMIN',
    1
);

INSERT INTO system_configs (config_key, config_value, description)
VALUES
    ('portal.name',              'Campus Placement Portal',  'Display name of the portal'),
    ('portal.max_resume_count',  '5',                        'Maximum resumes a student can upload'),
    ('portal.placement_year',    '2025',                     'Current active placement year'),
    ('portal.cgpa_scale',        '10',                       'CGPA scale (10 or 4)'),
    ('email.notifications',      'true',                     'Enable/disable email notifications'),
    ('websocket.notifications',  'true',                     'Enable/disable WebSocket notifications');
