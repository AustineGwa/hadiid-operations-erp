-- V9: Seed reference/system configuration — roles, permissions, role_permissions,
-- vehicle_sections, body_types (dropdown lists), fabrication_stages,
-- stage_transitions (§H.1 Option B: forward-only advance + audited correction),
-- payment_methods, and the single initial admin account. The admin creates every
-- other user from Settings → Users after first login; no other accounts are seeded.

-- Roles (Rule 27 — exactly three top-level roles)
INSERT INTO roles (id, code, label) VALUES
  (1, 'NORMAL_USER', 'Normal User'),
  (2, 'SUPERVISOR', 'Supervisor'),
  (3, 'ADMIN', 'Admin');

-- Permissions (§G)
INSERT INTO permissions (id, code, description) VALUES
  (1,  'JOB_VIEW',           'View fabrication jobs'),
  (2,  'JOB_CREATE',         'Create a new fabrication job'),
  (3,  'JOB_EDIT',           'Edit non-stage job fields'),
  (4,  'JOB_ADVANCE_STAGE',  'Advance a job to the next stage (forward-only)'),
  (5,  'JOB_CORRECT_STAGE',  'Move a job backward or skip a stage, with a mandatory reason'),
  (6,  'JOB_ASSIGN',         'Assign a worker/contractor to a job and stage'),
  (7,  'JOB_APPROVE',        'Reserved: job approval workflow'),
  (8,  'PAYMENT_VIEW',       'View labor payments and balances'),
  (9,  'PAYMENT_CREATE',     'Record a new labor payment'),
  (10, 'PAYMENT_EDIT',       'Edit an existing labor payment record'),
  (11, 'PAYMENT_APPROVE',    'Reserved: payment approval workflow'),
  (12, 'REPORT_VIEW',        'View operational and financial reports/dashboards'),
  (13, 'REPORT_EXPORT',      'Export reports (CSV/PDF)'),
  (14, 'SCHEDULE_VIEW',      'View the production schedule'),
  (15, 'SCHEDULE_EDIT',      'Edit production calendar day-status overrides'),
  (16, 'PLAN_VIEW',          'View production plans and targets'),
  (17, 'PLAN_EDIT',          'Edit capacity targets and weekly budgets'),
  (18, 'USER_VIEW',          'View users'),
  (19, 'USER_CREATE',        'Create a user'),
  (20, 'USER_EDIT',          'Edit a user'),
  (21, 'USER_DISABLE',       'Disable a user'),
  (22, 'SETTINGS_VIEW',      'View reference/master data'),
  (23, 'SETTINGS_EDIT',      'Edit reference/master data'),
  (24, 'AUDIT_VIEW',         'View the audit trail');

-- Role -> Permission mapping (§G matrix)
INSERT INTO role_permissions (role_id, permission_id) VALUES
  (1,1),(1,8),(1,12),(1,14),(1,16), -- NORMAL_USER
  (2,1),(2,2),(2,3),(2,4),(2,5),(2,6),(2,8),(2,9),(2,10),(2,12),(2,13),(2,14),(2,15),(2,16),(2,17),(2,22); -- SUPERVISOR

INSERT INTO role_permissions (role_id, permission_id)
SELECT 3, id FROM permissions; -- ADMIN: all permissions

-- Vehicle sections (Rule 35 — section is a data value)
INSERT INTO vehicle_sections (id, code, label) VALUES
  (1, 'TANKER', 'Tanker'),
  (2, 'BUS', 'Bus'),
  (3, 'TIPPER', 'Tipper');

-- Body types per section (§B.2 / §B.8 dropdown lists — editable without code)
INSERT INTO body_types (section_id, code, label) VALUES
  (1, 'MONOBLOCK_STANDARD', 'Monoblock Standard'),
  (1, 'MONOBLOCK_DRAWBAR', 'Monoblock Drawbar'),
  (1, 'DRAWBAR', 'Drawbar'),
  (1, 'BODY_BOX', 'Body Box'),
  (1, 'HEAD', 'Head'),
  (2, 'SHUTTLE', 'Shuttle'),
  (2, 'COACH', 'Coach'),
  (2, 'MINIBUS', 'Minibus'),
  (2, 'SCHOOL_BUS', 'School Bus'),
  (3, 'STANDARD_TIPPER', 'Standard Tipper'),
  (3, 'DRAWBAR_TIPPER', 'Drawbar Tipper'),
  (3, 'SEMI_TRAILER_TIPPER', 'Semi-Trailer Tipper'),
  (3, 'SIDE_TIPPER', 'Side Tipper');

-- Fabrication stages, in canonical order (Rule 9)
INSERT INTO fabrication_stages (id, code, label, sequence_order) VALUES
  (1, 'NOT_STARTED', 'Not Started', 1),
  (2, 'IN_PRODUCTION', 'In Production', 2),
  (3, 'AT_FINISHING', 'At Finishing Stage', 3),
  (4, 'AT_DELIVERY_STORE', 'At Delivery / In Store', 4),
  (5, 'RELEASED', 'Release to Customer', 5);

-- Stage transition rules (§H.1 Option B, this spec's working assumption — confirm
-- with Hadiid, then edit these rows, not code, if a different option is chosen).
-- Forward-only, one step at a time:
INSERT INTO stage_transitions (from_stage_id, to_stage_id, requires_permission_code, requires_reason) VALUES
  (1, 2, 'JOB_ADVANCE_STAGE', FALSE),
  (2, 3, 'JOB_ADVANCE_STAGE', FALSE),
  (3, 4, 'JOB_ADVANCE_STAGE', FALSE),
  (4, 5, 'JOB_ADVANCE_STAGE', FALSE);
-- Audited corrections: any other stage-to-stage move, always reasoned.
INSERT INTO stage_transitions (from_stage_id, to_stage_id, requires_permission_code, requires_reason) VALUES
  (2, 1, 'JOB_CORRECT_STAGE', TRUE),
  (3, 2, 'JOB_CORRECT_STAGE', TRUE),
  (4, 3, 'JOB_CORRECT_STAGE', TRUE),
  (3, 1, 'JOB_CORRECT_STAGE', TRUE),
  (4, 2, 'JOB_CORRECT_STAGE', TRUE),
  (4, 1, 'JOB_CORRECT_STAGE', TRUE),
  (1, 3, 'JOB_CORRECT_STAGE', TRUE),
  (1, 4, 'JOB_CORRECT_STAGE', TRUE),
  (2, 4, 'JOB_CORRECT_STAGE', TRUE);
-- RELEASED is terminal in this release (§H.3 business decision: no reopening) —
-- no row exists with from_stage_id = 5, so StageTransitionService will refuse
-- any attempt to move a job out of RELEASED, by construction.

-- Payment methods (§B.4 dropdown)
INSERT INTO payment_methods (id, code, label) VALUES
  (1, 'CASH', 'Cash'),
  (2, 'MOBILE_MONEY', 'Mobile Money'),
  (3, 'BANK_TRANSFER', 'Bank Transfer'),
  (4, 'CHEQUE', 'Cheque');

-- Initial admin account — the only user seeded at first boot.
-- Password: ChangeMe123!  (BCrypt hash below) — change immediately after first
-- login. Use Settings → Users (as this admin) to create every other account.
INSERT INTO users (username, full_name, email, phone, password_hash, role_id, status, team) VALUES
  ('admin', 'Admin User', 'admin@hadiid.local', '0722-000000', '$2b$10$8ATX38ZxFYDQYM.lDvUMUeobt3z2qvMGlXck2Sgm82rA.DmK2qJKa', 3, 'ACTIVE', 'Admin');
