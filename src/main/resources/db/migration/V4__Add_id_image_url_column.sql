-- Add missing id_image_url column to professor_requests table
ALTER TABLE professor_requests ADD COLUMN IF NOT EXISTS id_image_url VARCHAR(255) NOT NULL DEFAULT ''; 