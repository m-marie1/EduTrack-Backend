-- Rename id_document_url to id_image_url if it exists
ALTER TABLE professor_requests DROP COLUMN IF EXISTS id_image_url;
ALTER TABLE professor_requests RENAME COLUMN id_document_url TO id_image_url; 