-- Fix special column names and data types

-- Handle special column names that need to be quoted in PostgreSQL
ALTER TABLE questions RENAME COLUMN question_order TO "order";
ALTER TABLE question_options RENAME COLUMN option_order TO "order";

-- Ensure text columns have proper type
ALTER TABLE assignment_submissions ALTER COLUMN notes TYPE TEXT;
ALTER TABLE assignment_submissions ALTER COLUMN feedback TYPE TEXT;
ALTER TABLE quiz_answers ALTER COLUMN text_answer TYPE TEXT; 