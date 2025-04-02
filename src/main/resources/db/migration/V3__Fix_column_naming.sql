-- Fix column naming inconsistencies between entity model and database schema

-- Add missing additional_info column to professor_requests table
ALTER TABLE professor_requests ADD COLUMN IF NOT EXISTS additional_info TEXT;

-- Fix any other potential column naming issues based on Java naming conventions vs SQL conventions
-- No other issues found in initial review 