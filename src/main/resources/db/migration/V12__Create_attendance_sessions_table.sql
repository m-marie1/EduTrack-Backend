-- Create the table for storing attendance session details
CREATE TABLE attendance_sessions (
    id BIGSERIAL PRIMARY KEY,
    course_id BIGINT NOT NULL,
    professor_id BIGINT NOT NULL,
    verification_code VARCHAR(10) NOT NULL, -- Adjusted size based on CODE_LENGTH
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    expires_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,

    -- Foreign key constraints
    CONSTRAINT fk_attendance_session_course FOREIGN KEY (course_id) REFERENCES courses (id) ON DELETE CASCADE,
    CONSTRAINT fk_attendance_session_professor FOREIGN KEY (professor_id) REFERENCES users (id) ON DELETE CASCADE,

    -- Ensure verification codes are unique while active and not expired (within a reasonable timeframe)
    -- A global unique constraint might be too strict if codes are reused much later.
    -- Let's start with a simple unique constraint on the code itself for now.
    -- Consider refining this if code collisions become an issue across inactive/expired sessions.
     CONSTRAINT uq_attendance_session_code UNIQUE (verification_code)
);

-- Indexes for performance
CREATE INDEX idx_attendance_session_lookup ON attendance_sessions (course_id, verification_code, active, expires_at);
CREATE INDEX idx_attendance_session_cleanup ON attendance_sessions (active, expires_at);
CREATE INDEX idx_attendance_session_course_active ON attendance_sessions (course_id, active, expires_at); -- For finding active sessions for a course

-- Remove old network/verification columns from attendance_records if they exist
-- (Adding ALTER TABLE statements defensively in case they were missed)
ALTER TABLE attendance_records DROP COLUMN IF EXISTS network_identifier;
ALTER TABLE attendance_records DROP COLUMN IF EXISTS verification_method;