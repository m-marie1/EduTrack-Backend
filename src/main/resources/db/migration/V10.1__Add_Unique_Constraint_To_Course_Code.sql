-- Add UNIQUE constraint to course_code column in courses table
-- This is required for the ON CONFLICT clause used in V11

ALTER TABLE courses
ADD CONSTRAINT uk_courses_course_code UNIQUE (course_code);