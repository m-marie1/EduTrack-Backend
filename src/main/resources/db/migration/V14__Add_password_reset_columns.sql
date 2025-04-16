-- Add password reset columns to users table
ALTER TABLE users
ADD COLUMN IF NOT EXISTS reset_code VARCHAR(255),
ADD COLUMN IF NOT EXISTS reset_code_expiry TIMESTAMP;