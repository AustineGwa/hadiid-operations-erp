-- V12: Demo labor & payments — contractors, workers, assignments, payments,
-- transcribed exactly from the workbook's 'Labor & Payments' tab.

INSERT INTO contractors (name, registration_no, phone) VALUES
  ('Otieno Fabricators Ltd', 'ID-22841190', '0722-555001'),
  ('Kariuki Welders', 'ID-19883452', '0711-444100'),
  ('Mwangi Body Builders', 'ID-21093384', '0700-321004'),
  ('Kamau Interiors', 'ID-20112298', '0701-778800'),
  ('Njoroge Welding Works', 'ID-19004432', '0723-909090'),
  ('Wanjiru Finishers', 'ID-18774421', '0712-334455');

INSERT INTO workers (name, id_number, phone, contractor_id) VALUES
  ('James Otieno', 'ID-22841190', '0722-555001', (SELECT id FROM contractors WHERE registration_no='ID-22841190')),
  ('Paul Mwangi', 'ID-24471203', '0733-555002', (SELECT id FROM contractors WHERE registration_no='ID-22841190')),
  ('Samuel Kariuki', 'ID-19883452', '0711-444100', (SELECT id FROM contractors WHERE registration_no='ID-19883452')),
  ('Peter Mwangi', 'ID-21093384', '0700-321004', (SELECT id FROM contractors WHERE registration_no='ID-21093384')),
  ('Grace Achieng', 'ID-23412209', '0710-321005', (SELECT id FROM contractors WHERE registration_no='ID-21093384')),
  ('Daniel Kamau', 'ID-20112298', '0701-778800', (SELECT id FROM contractors WHERE registration_no='ID-20112298')),
  ('Francis Njoroge', 'ID-19004432', '0723-909090', (SELECT id FROM contractors WHERE registration_no='ID-19004432')),
  ('Brian Kiptoo', 'ID-25501187', '0741-909091', (SELECT id FROM contractors WHERE registration_no='ID-19004432')),
  ('Lucy Wanjiru', 'ID-18774421', '0712-334455', (SELECT id FROM contractors WHERE registration_no='ID-18774421'));

INSERT INTO job_worker_assignments (job_id, stage_id, worker_id, agreed_amount) VALUES
  ((SELECT id FROM fabrication_jobs WHERE vehicle_id='TK-002'), (SELECT id FROM fabrication_stages WHERE code='IN_PRODUCTION'), (SELECT id FROM workers WHERE id_number='ID-22841190' AND name='James Otieno'), 140000),
  ((SELECT id FROM fabrication_jobs WHERE vehicle_id='TK-002'), (SELECT id FROM fabrication_stages WHERE code='IN_PRODUCTION'), (SELECT id FROM workers WHERE id_number='ID-24471203'), 130000),
  ((SELECT id FROM fabrication_jobs WHERE vehicle_id='TK-003'), (SELECT id FROM fabrication_stages WHERE code='AT_FINISHING'), (SELECT id FROM workers WHERE id_number='ID-19883452'), 95000),
  ((SELECT id FROM fabrication_jobs WHERE vehicle_id='BS-002'), (SELECT id FROM fabrication_stages WHERE code='IN_PRODUCTION'), (SELECT id FROM workers WHERE id_number='ID-21093384'), 180000),
  ((SELECT id FROM fabrication_jobs WHERE vehicle_id='BS-002'), (SELECT id FROM fabrication_stages WHERE code='IN_PRODUCTION'), (SELECT id FROM workers WHERE id_number='ID-23412209'), 160000),
  ((SELECT id FROM fabrication_jobs WHERE vehicle_id='BS-003'), (SELECT id FROM fabrication_stages WHERE code='AT_FINISHING'), (SELECT id FROM workers WHERE id_number='ID-20112298'), 130000),
  ((SELECT id FROM fabrication_jobs WHERE vehicle_id='TP-002'), (SELECT id FROM fabrication_stages WHERE code='IN_PRODUCTION'), (SELECT id FROM workers WHERE id_number='ID-19004432'), 100000),
  ((SELECT id FROM fabrication_jobs WHERE vehicle_id='TP-002'), (SELECT id FROM fabrication_stages WHERE code='IN_PRODUCTION'), (SELECT id FROM workers WHERE id_number='ID-25501187'), 90000),
  ((SELECT id FROM fabrication_jobs WHERE vehicle_id='TP-003'), (SELECT id FROM fabrication_stages WHERE code='AT_FINISHING'), (SELECT id FROM workers WHERE id_number='ID-18774421'), 70000);

-- Job-card-level contract amounts already set on fabrication_jobs in V10 (§B.5:
-- Balance/% Paid are computed against contract_amount, never against the sum
-- of these worker agreed amounts).

INSERT INTO labor_payments (assignment_id, amount_paid, date_paid, payment_method_id, paid_by_user_id, reference_note) VALUES
  ((SELECT id FROM job_worker_assignments WHERE job_id=(SELECT id FROM fabrication_jobs WHERE vehicle_id='TK-002') AND worker_id=(SELECT id FROM workers WHERE id_number='ID-22841190' AND name='James Otieno')), 140000, '2026-09-19', (SELECT id FROM payment_methods WHERE code='BANK_TRANSFER'), (SELECT id FROM users WHERE username='gnyambura'), NULL),
  ((SELECT id FROM job_worker_assignments WHERE job_id=(SELECT id FROM fabrication_jobs WHERE vehicle_id='TK-002') AND worker_id=(SELECT id FROM workers WHERE id_number='ID-24471203')), 80000, '2026-09-21', (SELECT id FROM payment_methods WHERE code='MOBILE_MONEY'), (SELECT id FROM users WHERE username='gnyambura'), NULL),
  ((SELECT id FROM job_worker_assignments WHERE job_id=(SELECT id FROM fabrication_jobs WHERE vehicle_id='TK-003') AND worker_id=(SELECT id FROM workers WHERE id_number='ID-19883452')), 95000, '2026-09-20', (SELECT id FROM payment_methods WHERE code='CASH'), (SELECT id FROM users WHERE username='gnyambura'), NULL),
  ((SELECT id FROM job_worker_assignments WHERE job_id=(SELECT id FROM fabrication_jobs WHERE vehicle_id='BS-002') AND worker_id=(SELECT id FROM workers WHERE id_number='ID-21093384')), 120000, '2026-09-18', (SELECT id FROM payment_methods WHERE code='BANK_TRANSFER'), (SELECT id FROM users WHERE username='gnyambura'), NULL),
  ((SELECT id FROM job_worker_assignments WHERE job_id=(SELECT id FROM fabrication_jobs WHERE vehicle_id='BS-002') AND worker_id=(SELECT id FROM workers WHERE id_number='ID-23412209')), 160000, '2026-09-21', (SELECT id FROM payment_methods WHERE code='MOBILE_MONEY'), (SELECT id FROM users WHERE username='gnyambura'), NULL),
  ((SELECT id FROM job_worker_assignments WHERE job_id=(SELECT id FROM fabrication_jobs WHERE vehicle_id='BS-003') AND worker_id=(SELECT id FROM workers WHERE id_number='ID-20112298')), 65000, '2026-09-21', (SELECT id FROM payment_methods WHERE code='CHEQUE'), (SELECT id FROM users WHERE username='gnyambura'), NULL),
  ((SELECT id FROM job_worker_assignments WHERE job_id=(SELECT id FROM fabrication_jobs WHERE vehicle_id='TP-002') AND worker_id=(SELECT id FROM workers WHERE id_number='ID-19004432')), 100000, '2026-09-17', (SELECT id FROM payment_methods WHERE code='BANK_TRANSFER'), (SELECT id FROM users WHERE username='gnyambura'), NULL),
  ((SELECT id FROM job_worker_assignments WHERE job_id=(SELECT id FROM fabrication_jobs WHERE vehicle_id='TP-002') AND worker_id=(SELECT id FROM workers WHERE id_number='ID-25501187')), 45000, '2026-09-20', (SELECT id FROM payment_methods WHERE code='CASH'), (SELECT id FROM users WHERE username='gnyambura'), NULL),
  ((SELECT id FROM job_worker_assignments WHERE job_id=(SELECT id FROM fabrication_jobs WHERE vehicle_id='TP-003') AND worker_id=(SELECT id FROM workers WHERE id_number='ID-18774421')), 70000, '2026-09-21', (SELECT id FROM payment_methods WHERE code='MOBILE_MONEY'), (SELECT id FROM users WHERE username='gnyambura'), NULL);
