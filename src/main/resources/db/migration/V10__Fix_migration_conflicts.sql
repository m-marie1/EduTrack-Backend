-- Fix migration conflicts and ensure safe application of changes

-- Make all column operations conditional to avoid errors
DO $$
BEGIN
    -- Safe column rename operations - only if source column exists and target doesn't
    BEGIN
        IF EXISTS (SELECT 1 FROM information_schema.columns 
                  WHERE table_name = 'professor_requests' AND column_name = 'justification') 
           AND NOT EXISTS (SELECT 1 FROM information_schema.columns 
                          WHERE table_name = 'professor_requests' AND column_name = 'additional_info') THEN
            ALTER TABLE professor_requests RENAME COLUMN justification TO additional_info;
        END IF;
    EXCEPTION WHEN OTHERS THEN
        RAISE NOTICE 'Skipping justification rename: %', SQLERRM;
    END;
    
    BEGIN
        IF EXISTS (SELECT 1 FROM information_schema.columns 
                  WHERE table_name = 'professor_requests' AND column_name = 'submitted_at') 
           AND NOT EXISTS (SELECT 1 FROM information_schema.columns 
                          WHERE table_name = 'professor_requests' AND column_name = 'request_date') THEN
            ALTER TABLE professor_requests RENAME COLUMN submitted_at TO request_date;
        END IF;
    EXCEPTION WHEN OTHERS THEN
        RAISE NOTICE 'Skipping submitted_at rename: %', SQLERRM;
    END;
    
    BEGIN
        IF EXISTS (SELECT 1 FROM information_schema.columns 
                  WHERE table_name = 'professor_requests' AND column_name = 'processed_at') 
           AND NOT EXISTS (SELECT 1 FROM information_schema.columns 
                          WHERE table_name = 'professor_requests' AND column_name = 'review_date') THEN
            ALTER TABLE professor_requests RENAME COLUMN processed_at TO review_date;
        END IF;
    EXCEPTION WHEN OTHERS THEN
        RAISE NOTICE 'Skipping processed_at rename: %', SQLERRM;
    END;
    
    BEGIN
        IF EXISTS (SELECT 1 FROM information_schema.columns 
                  WHERE table_name = 'professor_requests' AND column_name = 'processor_id') 
           AND NOT EXISTS (SELECT 1 FROM information_schema.columns 
                          WHERE table_name = 'professor_requests' AND column_name = 'reviewed_by') THEN
            ALTER TABLE professor_requests RENAME COLUMN processor_id TO reviewed_by;
        END IF;
    EXCEPTION WHEN OTHERS THEN
        RAISE NOTICE 'Skipping processor_id rename: %', SQLERRM;
    END;
    
    BEGIN
        IF EXISTS (SELECT 1 FROM information_schema.columns 
                  WHERE table_name = 'professor_requests' AND column_name = 'id_document_url') 
           AND NOT EXISTS (SELECT 1 FROM information_schema.columns 
                          WHERE table_name = 'professor_requests' AND column_name = 'id_image_url') THEN
            ALTER TABLE professor_requests RENAME COLUMN id_document_url TO id_image_url;
        END IF;
    EXCEPTION WHEN OTHERS THEN
        RAISE NOTICE 'Skipping id_document_url rename: %', SQLERRM;
    END;
    
    -- Safe column add operations - only if column doesn't exist
    BEGIN
        IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                      WHERE table_name = 'professor_requests' AND column_name = 'rejection_reason') THEN
            ALTER TABLE professor_requests ADD COLUMN rejection_reason TEXT;
        END IF;
    EXCEPTION WHEN OTHERS THEN
        RAISE NOTICE 'Skipping rejection_reason add: %', SQLERRM;
    END;
    
    BEGIN
        IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                      WHERE table_name = 'professor_requests' AND column_name = 'id_image_url') THEN
            ALTER TABLE professor_requests ADD COLUMN id_image_url VARCHAR(255) DEFAULT '';
        END IF;
    EXCEPTION WHEN OTHERS THEN
        RAISE NOTICE 'Skipping id_image_url add: %', SQLERRM;
    END;
    
    -- Fix column types to ensure they match entity requirements
    BEGIN
        ALTER TABLE professor_requests ALTER COLUMN reviewed_by TYPE VARCHAR(255);
    EXCEPTION WHEN OTHERS THEN
        RAISE NOTICE 'Skipping reviewed_by type change: %', SQLERRM;
    END;
    
    -- Fix special column names with order
    BEGIN
        IF EXISTS (SELECT 1 FROM information_schema.columns 
                  WHERE table_name = 'questions' AND column_name = 'question_order') THEN
            ALTER TABLE questions RENAME COLUMN question_order TO "order";
        END IF;
    EXCEPTION WHEN OTHERS THEN
        RAISE NOTICE 'Skipping question_order rename: %', SQLERRM;
    END;
    
    BEGIN
        IF EXISTS (SELECT 1 FROM information_schema.columns 
                  WHERE table_name = 'question_options' AND column_name = 'option_order') THEN
            ALTER TABLE question_options RENAME COLUMN option_order TO "order";
        END IF;
    EXCEPTION WHEN OTHERS THEN
        RAISE NOTICE 'Skipping option_order rename: %', SQLERRM;
    END;
    
    -- Ensure text columns have proper types
    BEGIN
        ALTER TABLE assignment_submissions ALTER COLUMN notes TYPE TEXT;
    EXCEPTION WHEN OTHERS THEN
        RAISE NOTICE 'Skipping notes type change: %', SQLERRM;
    END;
    
    BEGIN
        ALTER TABLE assignment_submissions ALTER COLUMN feedback TYPE TEXT;
    EXCEPTION WHEN OTHERS THEN
        RAISE NOTICE 'Skipping feedback type change: %', SQLERRM;
    END;
    
    BEGIN
        ALTER TABLE quiz_answers ALTER COLUMN text_answer TYPE TEXT;
    EXCEPTION WHEN OTHERS THEN
        RAISE NOTICE 'Skipping text_answer type change: %', SQLERRM;
    END;
END $$; 