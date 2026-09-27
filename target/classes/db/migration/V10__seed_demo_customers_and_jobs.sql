-- V10: Demo data — customers + fabrication_jobs, transcribed exactly from the
-- source workbook's 18 sample vehicles (6 per section) so the ERP starts up
-- already demonstrating real, recognizable data. This migration (and V11/V12)
-- is optional seed/demo data, separate from the core V1-V9 schema+reference plan
-- in the architecture spec — delete V10-V12 for a clean production start.

INSERT INTO customers (name, registration_no, phone, address) VALUES
  ('Kisumu Fuels Ltd', 'PIN-P05198213X', '0722-100100', 'Kibos Rd, Kisumu'),
  ('Nairobi Water Co.', 'PIN-P05123456A', '0722-100200', 'Industrial Area, Nairobi'),
  ('Brookside Dairies', 'PIN-P05445566B', '0722-100300', 'Ruiru, Kiambu'),
  ('Total Energies Kenya', 'PIN-P05778899C', '0722-100400', 'Mombasa Rd, Nairobi'),
  ('Rubis Energy Kenya', 'PIN-P05990011D', '0722-100500', 'Nakuru Highway'),
  ('Vivo Energy Kenya', 'PIN-P05223344E', '0722-100600', 'Athi River, Machakos'),
  ('Modern Coast Express', 'PIN-P06112233A', '0733-200100', 'Ronald Ngala St, Nairobi'),
  ('Easy Coach Ltd', 'PIN-P06223344B', '0733-200200', 'Kisumu Bus Park'),
  ('St. Mary''s School', 'PIN-P06334455C', '0733-200300', 'Karen, Nairobi'),
  ('City Shuttle Services', 'PIN-P06445566D', '0733-200400', 'Westlands, Nairobi'),
  ('Guardian Coaches', 'PIN-P06556677E', '0733-200500', 'Eldoret Town'),
  ('Mash Poa Sacco', 'PIN-P06667788F', '0733-200600', 'Nyeri Town'),
  ('Mombasa Builders Ltd', 'PIN-P07112233A', '0700-300100', 'Changamwe, Mombasa'),
  ('Kenya Quarry Co.', 'PIN-P07223344B', '0700-300200', 'Machakos'),
  ('Bamburi Cement', 'PIN-P07334455C', '0700-300300', 'Bamburi, Mombasa'),
  ('China Road & Bridge Corp', 'PIN-P07445566D', '0700-300400', 'Naivasha'),
  ('County Govt of Kiambu', 'PIN-P07556677E', '0700-300500', 'Kiambu Town'),
  ('Athi River Mining', 'PIN-P07667788F', '0700-300600', 'Athi River, Machakos');

-- Tanker (section_id = 1)
INSERT INTO fabrication_jobs
  (vehicle_id, job_card_no, job_card_date, section_id, body_type_id, vehicle_model,
   planned_start_date, planned_production_days, planned_finishing_days,
   current_stage_id, stage_entered_at, internal_store_date, release_date, contract_amount,
   customer_id, customer_name_snapshot, customer_reg_snapshot, customer_phone_snapshot, customer_address_snapshot,
   created_by_user_id)
VALUES
  ('TK-001','JC-1041','2026-09-22',1,(SELECT id FROM body_types WHERE section_id=1 AND code='MONOBLOCK_STANDARD'),'10,000L Fuel Tanker',
   '2026-09-25',14,6,(SELECT id FROM fabrication_stages WHERE code='NOT_STARTED'),'2026-09-22 00:00:00',NULL,NULL,NULL,
   (SELECT id FROM customers WHERE registration_no='PIN-P05198213X'),'Kisumu Fuels Ltd','PIN-P05198213X','0722-100100','Kibos Rd, Kisumu',
   (SELECT id FROM users WHERE username='admin')),
  ('TK-002','JC-1032','2026-09-17',1,(SELECT id FROM body_types WHERE section_id=1 AND code='MONOBLOCK_DRAWBAR'),'15,000L Water Tanker',
   '2026-09-17',16,7,(SELECT id FROM fabrication_stages WHERE code='IN_PRODUCTION'),'2026-09-17 00:00:00',NULL,NULL,480000,
   (SELECT id FROM customers WHERE registration_no='PIN-P05123456A'),'Nairobi Water Co.','PIN-P05123456A','0722-100200','Industrial Area, Nairobi',
   (SELECT id FROM users WHERE username='admin')),
  ('TK-003','JC-1019','2026-09-04',1,(SELECT id FROM body_types WHERE section_id=1 AND code='DRAWBAR'),'20,000L Milk Tanker',
   '2026-09-04',14,8,(SELECT id FROM fabrication_stages WHERE code='AT_FINISHING'),'2026-09-18 00:00:00',NULL,NULL,420000,
   (SELECT id FROM customers WHERE registration_no='PIN-P05445566B'),'Brookside Dairies','PIN-P05445566B','0722-100300','Ruiru, Kiambu',
   (SELECT id FROM users WHERE username='admin')),
  ('TK-004','JC-1005','2026-08-25',1,(SELECT id FROM body_types WHERE section_id=1 AND code='BODY_BOX'),'8,000L LPG Tanker',
   '2026-08-25',12,7,(SELECT id FROM fabrication_stages WHERE code='AT_DELIVERY_STORE'),'2026-09-20 00:00:00','2026-09-20',NULL,NULL,
   (SELECT id FROM customers WHERE registration_no='PIN-P05778899C'),'Total Energies Kenya','PIN-P05778899C','0722-100400','Mombasa Rd, Nairobi',
   (SELECT id FROM users WHERE username='admin')),
  ('TK-005','JC-0988','2026-08-08',1,(SELECT id FROM body_types WHERE section_id=1 AND code='HEAD'),'12,000L Fuel Tanker',
   '2026-08-08',14,6,(SELECT id FROM fabrication_stages WHERE code='AT_DELIVERY_STORE'),'2026-09-16 00:00:00','2026-09-16',NULL,NULL,
   (SELECT id FROM customers WHERE registration_no='PIN-P05990011D'),'Rubis Energy Kenya','PIN-P05990011D','0722-100500','Nakuru Highway',
   (SELECT id FROM users WHERE username='admin')),
  ('TK-006','JC-0975','2026-07-29',1,(SELECT id FROM body_types WHERE section_id=1 AND code='MONOBLOCK_STANDARD'),'10,000L Fuel Tanker',
   '2026-07-29',14,6,(SELECT id FROM fabrication_stages WHERE code='RELEASED'),'2026-09-19 00:00:00','2026-09-19','2026-09-20',NULL,
   (SELECT id FROM customers WHERE registration_no='PIN-P05223344E'),'Vivo Energy Kenya','PIN-P05223344E','0722-100600','Athi River, Machakos',
   (SELECT id FROM users WHERE username='admin'));

-- Bus (section_id = 2)
INSERT INTO fabrication_jobs
  (vehicle_id, job_card_no, job_card_date, section_id, body_type_id, vehicle_model,
   planned_start_date, planned_production_days, planned_finishing_days,
   current_stage_id, stage_entered_at, internal_store_date, release_date, contract_amount,
   customer_id, customer_name_snapshot, customer_reg_snapshot, customer_phone_snapshot, customer_address_snapshot,
   created_by_user_id)
VALUES
  ('BS-001','JC-2041','2026-09-22',2,(SELECT id FROM body_types WHERE section_id=2 AND code='SHUTTLE'),'33-Seater Shuttle Bus',
   '2026-09-24',18,8,(SELECT id FROM fabrication_stages WHERE code='NOT_STARTED'),'2026-09-22 00:00:00',NULL,NULL,NULL,
   (SELECT id FROM customers WHERE registration_no='PIN-P06112233A'),'Modern Coast Express','PIN-P06112233A','0733-200100','Ronald Ngala St, Nairobi',
   (SELECT id FROM users WHERE username='admin')),
  ('BS-002','JC-2032','2026-09-15',2,(SELECT id FROM body_types WHERE section_id=2 AND code='COACH'),'62-Seater Coach',
   '2026-09-15',20,9,(SELECT id FROM fabrication_stages WHERE code='IN_PRODUCTION'),'2026-09-15 00:00:00',NULL,NULL,650000,
   (SELECT id FROM customers WHERE registration_no='PIN-P06223344B'),'Easy Coach Ltd','PIN-P06223344B','0733-200200','Kisumu Bus Park',
   (SELECT id FROM users WHERE username='admin')),
  ('BS-003','JC-2019','2026-09-02',2,(SELECT id FROM body_types WHERE section_id=2 AND code='SCHOOL_BUS'),'45-Seater School Bus',
   '2026-09-02',18,9,(SELECT id FROM fabrication_stages WHERE code='AT_FINISHING'),'2026-09-16 00:00:00',NULL,NULL,600000,
   (SELECT id FROM customers WHERE registration_no='PIN-P06334455C'),'St. Mary''s School','PIN-P06334455C','0733-200300','Karen, Nairobi',
   (SELECT id FROM users WHERE username='admin')),
  ('BS-004','JC-2005','2026-08-23',2,(SELECT id FROM body_types WHERE section_id=2 AND code='MINIBUS'),'14-Seater Minibus',
   '2026-08-23',12,6,(SELECT id FROM fabrication_stages WHERE code='AT_DELIVERY_STORE'),'2026-09-20 00:00:00','2026-09-20',NULL,NULL,
   (SELECT id FROM customers WHERE registration_no='PIN-P06445566D'),'City Shuttle Services','PIN-P06445566D','0733-200400','Westlands, Nairobi',
   (SELECT id FROM users WHERE username='admin')),
  ('BS-005','JC-1988','2026-08-03',2,(SELECT id FROM body_types WHERE section_id=2 AND code='SHUTTLE'),'33-Seater Shuttle Bus',
   '2026-08-03',18,8,(SELECT id FROM fabrication_stages WHERE code='AT_DELIVERY_STORE'),'2026-09-14 00:00:00','2026-09-14',NULL,NULL,
   (SELECT id FROM customers WHERE registration_no='PIN-P06556677E'),'Guardian Coaches','PIN-P06556677E','0733-200500','Eldoret Town',
   (SELECT id FROM users WHERE username='admin')),
  ('BS-006','JC-1975','2026-07-24',2,(SELECT id FROM body_types WHERE section_id=2 AND code='COACH'),'62-Seater Coach',
   '2026-07-24',20,9,(SELECT id FROM fabrication_stages WHERE code='RELEASED'),'2026-09-18 00:00:00','2026-09-18','2026-09-19',NULL,
   (SELECT id FROM customers WHERE registration_no='PIN-P06667788F'),'Mash Poa Sacco','PIN-P06667788F','0733-200600','Nyeri Town',
   (SELECT id FROM users WHERE username='admin'));

-- Tipper (section_id = 3)
INSERT INTO fabrication_jobs
  (vehicle_id, job_card_no, job_card_date, section_id, body_type_id, vehicle_model,
   planned_start_date, planned_production_days, planned_finishing_days,
   current_stage_id, stage_entered_at, internal_store_date, release_date, contract_amount,
   customer_id, customer_name_snapshot, customer_reg_snapshot, customer_phone_snapshot, customer_address_snapshot,
   created_by_user_id)
VALUES
  ('TP-001','JC-3041','2026-09-22',3,(SELECT id FROM body_types WHERE section_id=3 AND code='STANDARD_TIPPER'),'10-Ton Tipper',
   '2026-09-23',10,5,(SELECT id FROM fabrication_stages WHERE code='NOT_STARTED'),'2026-09-22 00:00:00',NULL,NULL,NULL,
   (SELECT id FROM customers WHERE registration_no='PIN-P07112233A'),'Mombasa Builders Ltd','PIN-P07112233A','0700-300100','Changamwe, Mombasa',
   (SELECT id FROM users WHERE username='admin')),
  ('TP-002','JC-3032','2026-09-18',3,(SELECT id FROM body_types WHERE section_id=3 AND code='DRAWBAR_TIPPER'),'20-Ton Tipper',
   '2026-09-18',12,6,(SELECT id FROM fabrication_stages WHERE code='IN_PRODUCTION'),'2026-09-18 00:00:00',NULL,NULL,350000,
   (SELECT id FROM customers WHERE registration_no='PIN-P07223344B'),'Kenya Quarry Co.','PIN-P07223344B','0700-300200','Machakos',
   (SELECT id FROM users WHERE username='admin')),
  ('TP-003','JC-3019','2026-09-10',3,(SELECT id FROM body_types WHERE section_id=3 AND code='SEMI_TRAILER_TIPPER'),'15-Ton Tipper',
   '2026-09-10',10,6,(SELECT id FROM fabrication_stages WHERE code='AT_FINISHING'),'2026-09-19 00:00:00',NULL,NULL,320000,
   (SELECT id FROM customers WHERE registration_no='PIN-P07334455C'),'Bamburi Cement','PIN-P07334455C','0700-300300','Bamburi, Mombasa',
   (SELECT id FROM users WHERE username='admin')),
  ('TP-004','JC-3005','2026-08-31',3,(SELECT id FROM body_types WHERE section_id=3 AND code='SIDE_TIPPER'),'30-Ton Tipper',
   '2026-08-31',14,7,(SELECT id FROM fabrication_stages WHERE code='AT_DELIVERY_STORE'),'2026-09-21 00:00:00','2026-09-21',NULL,NULL,
   (SELECT id FROM customers WHERE registration_no='PIN-P07445566D'),'China Road & Bridge Corp','PIN-P07445566D','0700-300400','Naivasha',
   (SELECT id FROM users WHERE username='admin')),
  ('TP-005','JC-2988','2026-08-18',3,(SELECT id FROM body_types WHERE section_id=3 AND code='STANDARD_TIPPER'),'10-Ton Tipper',
   '2026-08-18',10,5,(SELECT id FROM fabrication_stages WHERE code='AT_DELIVERY_STORE'),'2026-09-17 00:00:00','2026-09-17',NULL,NULL,
   (SELECT id FROM customers WHERE registration_no='PIN-P07556677E'),'County Govt of Kiambu','PIN-P07556677E','0700-300500','Kiambu Town',
   (SELECT id FROM users WHERE username='admin')),
  ('TP-006','JC-2975','2026-08-05',3,(SELECT id FROM body_types WHERE section_id=3 AND code='DRAWBAR_TIPPER'),'20-Ton Tipper',
   '2026-08-05',12,6,(SELECT id FROM fabrication_stages WHERE code='RELEASED'),'2026-09-20 00:00:00','2026-09-20','2026-09-21',NULL,
   (SELECT id FROM customers WHERE registration_no='PIN-P07667788F'),'Athi River Mining','PIN-P07667788F','0700-300600','Athi River, Machakos',
   (SELECT id FROM users WHERE username='admin'));
