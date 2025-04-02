-- Rename columns in professor_requests table to match entity field names
ALTER TABLE professor_requests RENAME COLUMN justification TO additional_info;
ALTER TABLE professor_requests RENAME COLUMN submitted_at TO request_date;
ALTER TABLE professor_requests RENAME COLUMN processed_at TO review_date;
ALTER TABLE professor_requests RENAME COLUMN processor_id TO reviewed_by;
ALTER TABLE professor_requests ADD COLUMN IF NOT EXISTS rejection_reason TEXT; 