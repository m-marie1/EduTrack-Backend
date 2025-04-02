-- Enhanced migration script to fix any database state inconsistencies
-- This script is designed to be idempotent and run safely regardless of database state

-- Create a function to run statements safely, ignoring failures
CREATE OR REPLACE FUNCTION execute_safely(query TEXT) RETURNS void AS $$
BEGIN
    EXECUTE query;
EXCEPTION WHEN OTHERS THEN
    RAISE NOTICE 'Statement failed: %', SQLERRM;
END;
$$ LANGUAGE plpgsql;

-- Ensure the professor_requests table has all necessary columns with correct names
DO $$
BEGIN
    -- Add columns if they don't exist
    PERFORM execute_safely('ALTER TABLE professor_requests ADD COLUMN IF NOT EXISTS id_image_url VARCHAR(255)');
    PERFORM execute_safely('ALTER TABLE professor_requests ADD COLUMN IF NOT EXISTS additional_info TEXT');
    PERFORM execute_safely('ALTER TABLE professor_requests ADD COLUMN IF NOT EXISTS rejection_reason TEXT');
    PERFORM execute_safely('ALTER TABLE professor_requests ADD COLUMN IF NOT EXISTS request_date TIMESTAMP');
    PERFORM execute_safely('ALTER TABLE professor_requests ADD COLUMN IF NOT EXISTS review_date TIMESTAMP');
    PERFORM execute_safely('ALTER TABLE professor_requests ADD COLUMN IF NOT EXISTS reviewed_by VARCHAR(255)');
    
    -- Safely rename columns if old names exist
    IF EXISTS (SELECT 1 FROM information_schema.columns 
              WHERE table_name = 'professor_requests' AND column_name = 'justification'
              AND NOT EXISTS (SELECT 1 FROM information_schema.columns 
                             WHERE table_name = 'professor_requests' AND column_name = 'additional_info')) THEN
        PERFORM execute_safely('ALTER TABLE professor_requests RENAME COLUMN justification TO additional_info');
    END IF;
    
    IF EXISTS (SELECT 1 FROM information_schema.columns 
              WHERE table_name = 'professor_requests' AND column_name = 'submitted_at'
              AND NOT EXISTS (SELECT 1 FROM information_schema.columns 
                             WHERE table_name = 'professor_requests' AND column_name = 'request_date')) THEN
        PERFORM execute_safely('ALTER TABLE professor_requests RENAME COLUMN submitted_at TO request_date');
    END IF;
    
    IF EXISTS (SELECT 1 FROM information_schema.columns 
              WHERE table_name = 'professor_requests' AND column_name = 'processed_at'
              AND NOT EXISTS (SELECT 1 FROM information_schema.columns 
                             WHERE table_name = 'professor_requests' AND column_name = 'review_date')) THEN
        PERFORM execute_safely('ALTER TABLE professor_requests RENAME COLUMN processed_at TO review_date');
    END IF;
    
    IF EXISTS (SELECT 1 FROM information_schema.columns 
              WHERE table_name = 'professor_requests' AND column_name = 'processor_id'
              AND NOT EXISTS (SELECT 1 FROM information_schema.columns 
                             WHERE table_name = 'professor_requests' AND column_name = 'reviewed_by')) THEN
        PERFORM execute_safely('ALTER TABLE professor_requests RENAME COLUMN processor_id TO reviewed_by');
    END IF;
    
    IF EXISTS (SELECT 1 FROM information_schema.columns 
              WHERE table_name = 'professor_requests' AND column_name = 'id_document_url'
              AND NOT EXISTS (SELECT 1 FROM information_schema.columns 
                             WHERE table_name = 'professor_requests' AND column_name = 'id_image_url')) THEN
        PERFORM execute_safely('ALTER TABLE professor_requests RENAME COLUMN id_document_url TO id_image_url');
    END IF;
    
    -- Fix column types
    PERFORM execute_safely('ALTER TABLE professor_requests ALTER COLUMN reviewed_by TYPE VARCHAR(255)');
    
    -- Fix special column names with order
    IF EXISTS (SELECT 1 FROM information_schema.columns 
              WHERE table_name = 'questions' AND column_name = 'question_order') THEN
        PERFORM execute_safely('ALTER TABLE questions RENAME COLUMN question_order TO "order"');
    END IF;
    
    IF EXISTS (SELECT 1 FROM information_schema.columns 
              WHERE table_name = 'question_options' AND column_name = 'option_order') THEN
        PERFORM execute_safely('ALTER TABLE question_options RENAME COLUMN option_order TO "order"');
    END IF;
    
    -- Ensure text columns have proper types
    PERFORM execute_safely('ALTER TABLE assignment_submissions ALTER COLUMN notes TYPE TEXT');
    PERFORM execute_safely('ALTER TABLE assignment_submissions ALTER COLUMN feedback TYPE TEXT');
    PERFORM execute_safely('ALTER TABLE quiz_answers ALTER COLUMN text_answer TYPE TEXT');
END $$; 