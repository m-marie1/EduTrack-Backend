-- Fix the reviewed_by column type (change from BIGINT to VARCHAR)
-- First drop the foreign key constraint
ALTER TABLE professor_requests DROP CONSTRAINT IF EXISTS fk_professor_requests_processor;

-- Then change the column type
ALTER TABLE professor_requests 
  ALTER COLUMN reviewed_by TYPE VARCHAR(255) USING reviewed_by::VARCHAR; 