-- Safe renaming of columns in professor_requests table to match entity field names
DO $$
BEGIN
  -- Only rename justification to additional_info if the column exists and additional_info doesn't
  IF EXISTS (SELECT 1 FROM information_schema.columns 
            WHERE table_name = 'professor_requests' AND column_name = 'justification')
     AND NOT EXISTS (SELECT 1 FROM information_schema.columns 
                    WHERE table_name = 'professor_requests' AND column_name = 'additional_info')
  THEN
    ALTER TABLE professor_requests RENAME COLUMN justification TO additional_info;
  END IF;

  -- Only rename submitted_at to request_date if the column exists and request_date doesn't
  IF EXISTS (SELECT 1 FROM information_schema.columns 
            WHERE table_name = 'professor_requests' AND column_name = 'submitted_at')
     AND NOT EXISTS (SELECT 1 FROM information_schema.columns 
                    WHERE table_name = 'professor_requests' AND column_name = 'request_date')
  THEN
    ALTER TABLE professor_requests RENAME COLUMN submitted_at TO request_date;
  END IF;

  -- Only rename processed_at to review_date if the column exists and review_date doesn't
  IF EXISTS (SELECT 1 FROM information_schema.columns 
            WHERE table_name = 'professor_requests' AND column_name = 'processed_at')
     AND NOT EXISTS (SELECT 1 FROM information_schema.columns 
                    WHERE table_name = 'professor_requests' AND column_name = 'review_date')
  THEN
    ALTER TABLE professor_requests RENAME COLUMN processed_at TO review_date;
  END IF;

  -- Only rename processor_id to reviewed_by if the column exists and reviewed_by doesn't
  IF EXISTS (SELECT 1 FROM information_schema.columns 
            WHERE table_name = 'professor_requests' AND column_name = 'processor_id')
     AND NOT EXISTS (SELECT 1 FROM information_schema.columns 
                    WHERE table_name = 'professor_requests' AND column_name = 'reviewed_by')
  THEN
    ALTER TABLE professor_requests RENAME COLUMN processor_id TO reviewed_by;
  END IF;

  -- Add rejection_reason if it doesn't exist
  IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                WHERE table_name = 'professor_requests' AND column_name = 'rejection_reason')
  THEN
    ALTER TABLE professor_requests ADD COLUMN rejection_reason TEXT;
  END IF;
END $$; 