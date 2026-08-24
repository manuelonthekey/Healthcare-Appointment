CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE doctor_profiles (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT UNIQUE NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    specialization VARCHAR(255) NOT NULL,
    slot_duration_mins INT NOT NULL DEFAULT 30,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING'
);
CREATE INDEX idx_doctor_specialization ON doctor_profiles(specialization);

CREATE TABLE doctor_availabilities (
    id BIGSERIAL PRIMARY KEY,
    doctor_profile_id BIGINT NOT NULL REFERENCES doctor_profiles(id) ON DELETE CASCADE,
    day_of_week INT NOT NULL, 
    start_time TIME NOT NULL,
    end_time TIME NOT NULL
);

CREATE TABLE doctor_leaves (
    id BIGSERIAL PRIMARY KEY,
    doctor_profile_id BIGINT NOT NULL REFERENCES doctor_profiles(id) ON DELETE CASCADE,
    leave_date DATE NOT NULL
);
CREATE INDEX idx_doctor_leaves ON doctor_leaves(doctor_profile_id, leave_date);

CREATE TABLE patient_profiles (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT UNIQUE NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    phone VARCHAR(50),
    dob DATE
);

CREATE TABLE appointments (
    id BIGSERIAL PRIMARY KEY,
    doctor_profile_id BIGINT NOT NULL REFERENCES doctor_profiles(id) ON DELETE CASCADE,
    patient_profile_id BIGINT NOT NULL REFERENCES patient_profiles(id) ON DELETE CASCADE,
    appointment_datetime TIMESTAMP NOT NULL,
    status VARCHAR(50) NOT NULL,
    symptoms TEXT,
    CONSTRAINT uq_doctor_appointment UNIQUE (doctor_profile_id, appointment_datetime)
);
CREATE INDEX idx_appointments_datetime ON appointments(doctor_profile_id, appointment_datetime);

CREATE TABLE consultations (
    id BIGSERIAL PRIMARY KEY,
    appointment_id BIGINT UNIQUE NOT NULL REFERENCES appointments(id) ON DELETE CASCADE,
    pre_visit_summary TEXT, 
    post_visit_notes TEXT,
    post_visit_summary TEXT
);

CREATE TABLE medication_reminders (
    id BIGSERIAL PRIMARY KEY,
    patient_profile_id BIGINT NOT NULL REFERENCES patient_profiles(id) ON DELETE CASCADE,
    consultation_id BIGINT NOT NULL REFERENCES consultations(id) ON DELETE CASCADE,
    medication_name VARCHAR(255) NOT NULL,
    frequency VARCHAR(255) NOT NULL,
    next_reminder_time TIMESTAMP NOT NULL
);
