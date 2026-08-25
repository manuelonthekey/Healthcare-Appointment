ALTER TABLE appointments ADD COLUMN clinical_notes TEXT;
ALTER TABLE appointments ADD COLUMN ai_summary TEXT;
ALTER TABLE appointments ADD COLUMN completed_at TIMESTAMP;
