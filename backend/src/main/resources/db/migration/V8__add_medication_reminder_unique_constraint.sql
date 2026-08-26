ALTER TABLE medication_reminders
ADD CONSTRAINT uq_appointment_medication_time UNIQUE (appointment_id, medication_name, reminder_time);
