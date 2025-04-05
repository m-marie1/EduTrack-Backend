-- Update all courses to be available 24 hours a day for testing purposes
UPDATE courses SET start_time = '00:00:00', end_time = '23:59:59' WHERE 1=1;

-- Add a note in the course descriptions
UPDATE courses SET description = CONCAT(description, ' (Available 24/7 for testing)') WHERE description NOT LIKE '%Available 24/7%'; 