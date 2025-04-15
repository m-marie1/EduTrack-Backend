-- Create table for tracking attendance reset per professor per course
CREATE TABLE IF NOT EXISTS course_attendance_resets (
    id BIGSERIAL PRIMARY KEY,
    professor_id BIGINT NOT NULL,
    course_id BIGINT NOT NULL,
    reset_timestamp TIMESTAMP NOT NULL,
    CONSTRAINT fk_reset_professor FOREIGN KEY (professor_id) REFERENCES users(id),
    CONSTRAINT fk_reset_course FOREIGN KEY (course_id) REFERENCES courses(id),
    CONSTRAINT uq_prof_course UNIQUE (professor_id, course_id)
);