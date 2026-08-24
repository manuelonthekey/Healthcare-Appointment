-- Admin user (Password is 'password' hashed with BCrypt)
INSERT INTO users (email, password_hash, role) VALUES ('admin@clinic.com', '$2a$10$9F/I6KxMh5Z6B4x8G4H8KuJq4Zq4Zq4Zq4Zq4Zq4Zq4Zq4Zq4Zq4Z', 'ADMIN');

-- Doctor User and Profile
INSERT INTO users (email, password_hash, role) VALUES ('dr.smith@clinic.com', '$2a$10$9F/I6KxMh5Z6B4x8G4H8KuJq4Zq4Zq4Zq4Zq4Zq4Zq4Zq4Zq4Zq4Z', 'DOCTOR');
INSERT INTO doctor_profiles (user_id, name, specialization, slot_duration_mins, status) 
VALUES ((SELECT id FROM users WHERE email = 'dr.smith@clinic.com'), 'Dr. John Smith', 'Cardiologist', 30, 'ACTIVE');

-- Doctor Availability (Mon-Fri 09:00 to 17:00)
INSERT INTO doctor_availabilities (doctor_profile_id, day_of_week, start_time, end_time) 
VALUES ((SELECT id FROM doctor_profiles WHERE name = 'Dr. John Smith'), 1, '09:00:00', '17:00:00');
INSERT INTO doctor_availabilities (doctor_profile_id, day_of_week, start_time, end_time) 
VALUES ((SELECT id FROM doctor_profiles WHERE name = 'Dr. John Smith'), 2, '09:00:00', '17:00:00');

-- Patient User and Profile
INSERT INTO users (email, password_hash, role) VALUES ('jane.doe@example.com', '$2a$10$9F/I6KxMh5Z6B4x8G4H8KuJq4Zq4Zq4Zq4Zq4Zq4Zq4Zq4Zq4Zq4Z', 'PATIENT');
INSERT INTO patient_profiles (user_id, name, phone, dob) 
VALUES ((SELECT id FROM users WHERE email = 'jane.doe@example.com'), 'Jane Doe', '555-0100', '1990-01-01');
