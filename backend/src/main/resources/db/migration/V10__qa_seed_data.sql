-- Additional seed data for QA

-- 1. Create a dummy Admin
INSERT INTO users (email, password_hash, role) VALUES ('qa_admin@clinic.com', '$2a$12$4n7TupZMpCkSgFirC4kGPO3qPWXHl1oLV2ukF4GSYBE7Qrq4yC1NG', 'ADMIN');

-- 2. Create multiple dummy Doctors
INSERT INTO users (email, password_hash, role) VALUES ('dr.active@clinic.com', '$2a$12$4n7TupZMpCkSgFirC4kGPO3qPWXHl1oLV2ukF4GSYBE7Qrq4yC1NG', 'DOCTOR');
INSERT INTO doctor_profiles (user_id, name, specialization, slot_duration_mins, status) 
VALUES ((SELECT id FROM users WHERE email = 'dr.active@clinic.com'), 'Dr. Alice Active', 'Neurologist', 30, 'ACTIVE');
INSERT INTO doctor_availabilities (doctor_profile_id, day_of_week, start_time, end_time) 
VALUES ((SELECT id FROM doctor_profiles WHERE name = 'Dr. Alice Active'), 1, '09:00:00', '17:00:00');
INSERT INTO doctor_availabilities (doctor_profile_id, day_of_week, start_time, end_time) 
VALUES ((SELECT id FROM doctor_profiles WHERE name = 'Dr. Alice Active'), 2, '09:00:00', '17:00:00');

INSERT INTO users (email, password_hash, role) VALUES ('dr.pending@clinic.com', '$2a$12$4n7TupZMpCkSgFirC4kGPO3qPWXHl1oLV2ukF4GSYBE7Qrq4yC1NG', 'DOCTOR');
INSERT INTO doctor_profiles (user_id, name, specialization, slot_duration_mins, status) 
VALUES ((SELECT id FROM users WHERE email = 'dr.pending@clinic.com'), 'Dr. Bob Pending', 'Dermatologist', 20, 'PENDING');

INSERT INTO users (email, password_hash, role) VALUES ('dr.onleave@clinic.com', '$2a$12$4n7TupZMpCkSgFirC4kGPO3qPWXHl1oLV2ukF4GSYBE7Qrq4yC1NG', 'DOCTOR');
INSERT INTO doctor_profiles (user_id, name, specialization, slot_duration_mins, status) 
VALUES ((SELECT id FROM users WHERE email = 'dr.onleave@clinic.com'), 'Dr. Charlie Onleave', 'Pediatrician', 15, 'ACTIVE');
INSERT INTO doctor_availabilities (doctor_profile_id, day_of_week, start_time, end_time) 
VALUES ((SELECT id FROM doctor_profiles WHERE name = 'Dr. Charlie Onleave'), 1, '09:00:00', '17:00:00');
INSERT INTO doctor_leaves (doctor_profile_id, leave_date)
VALUES ((SELECT id FROM doctor_profiles WHERE name = 'Dr. Charlie Onleave'), CURRENT_DATE);

-- 3. Create multiple dummy Patients
INSERT INTO users (email, password_hash, role) VALUES ('qa.patient@example.com', '$2a$12$4n7TupZMpCkSgFirC4kGPO3qPWXHl1oLV2ukF4GSYBE7Qrq4yC1NG', 'PATIENT');
INSERT INTO patient_profiles (user_id, name, phone, dob) 
VALUES ((SELECT id FROM users WHERE email = 'qa.patient@example.com'), 'QA Patient', '555-0101', '1985-05-05');

INSERT INTO users (email, password_hash, role) VALUES ('another.patient@example.com', '$2a$12$4n7TupZMpCkSgFirC4kGPO3qPWXHl1oLV2ukF4GSYBE7Qrq4yC1NG', 'PATIENT');
INSERT INTO patient_profiles (user_id, name, phone, dob) 
VALUES ((SELECT id FROM users WHERE email = 'another.patient@example.com'), 'Another Patient', '555-0102', '1992-02-02');

-- 4. Create past appointments attached to a doctor (dr.smith@clinic.com from V3)
INSERT INTO appointments (doctor_profile_id, patient_profile_id, appointment_datetime, status, symptoms, ai_summary)
VALUES 
((SELECT id FROM doctor_profiles WHERE name = 'Dr. John Smith'), (SELECT id FROM patient_profiles WHERE name = 'QA Patient'), DATEADD('DAY', -2, CURRENT_TIMESTAMP), 'COMPLETED', 'Headache and fever', '{"summary": "Patient presented with mild fever.", "medications": ["Paracetamol"], "notes": "Rest recommended."}'),
((SELECT id FROM doctor_profiles WHERE name = 'Dr. John Smith'), (SELECT id FROM patient_profiles WHERE name = 'Another Patient'), DATEADD('DAY', -5, CURRENT_TIMESTAMP), 'COMPLETED', 'Routine checkup', '{"summary": "All vitals normal.", "medications": [], "notes": "No action needed."}');
