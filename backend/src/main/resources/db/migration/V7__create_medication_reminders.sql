DROP TABLE IF EXISTS medication_reminders;

CREATE TABLE medication_reminders (
    id BIGSERIAL PRIMARY KEY,
    patient_profile_id BIGINT NOT NULL REFERENCES patient_profiles(id) ON DELETE CASCADE,
    appointment_id BIGINT NOT NULL REFERENCES appointments(id) ON DELETE CASCADE,
    medication_name VARCHAR(255) NOT NULL,
    dosage VARCHAR(255),
    frequency VARCHAR(255),
    reminder_time TIME,
    start_date DATE,
    end_date DATE,
    active BOOLEAN DEFAULT TRUE NOT NULL,
    last_notified_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_med_reminders_due ON medication_reminders(active, start_date, end_date, reminder_time);
