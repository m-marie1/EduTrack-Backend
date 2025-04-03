-- This migration ensures professor_requests table exists and has the additional_info column
DO $$
BEGIN
    -- Check if the professor_requests table exists
    IF NOT EXISTS (
        SELECT FROM pg_tables 
        WHERE schemaname = 'public' 
        AND tablename = 'professor_requests'
    ) THEN
        -- Create the professor_requests table if it doesn't exist
        CREATE TABLE professor_requests (
            id BIGSERIAL PRIMARY KEY,
            full_name VARCHAR(255) NOT NULL,
            email VARCHAR(255) NOT NULL,
            department VARCHAR(255) NOT NULL,
            justification TEXT,
            status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
            submitted_at TIMESTAMP NOT NULL,
            processed_at TIMESTAMP,
            id_document_url VARCHAR(255),
            processor_id BIGINT,
            additional_info TEXT
        );
    ELSE
        -- Check if additional_info column exists
        IF NOT EXISTS (
            SELECT FROM information_schema.columns 
            WHERE table_schema = 'public' 
            AND table_name = 'professor_requests' 
            AND column_name = 'additional_info'
        ) THEN
            -- Add the column if it doesn't exist
            ALTER TABLE professor_requests ADD COLUMN additional_info TEXT;
        END IF;
    END IF;
END $$; 