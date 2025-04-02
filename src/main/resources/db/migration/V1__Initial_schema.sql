-- Initial schema creation for the Attendance System

-- USERS table
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    student_id VARCHAR(255),
    role VARCHAR(20) NOT NULL,
    email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    verification_code VARCHAR(255),
    professor_request_id BIGINT
);

-- PROFESSOR REQUESTS table
CREATE TABLE IF NOT EXISTS professor_requests (
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
    CONSTRAINT fk_professor_requests_processor FOREIGN KEY (processor_id) REFERENCES users(id)
);

-- Add foreign key to USERS table after PROFESSOR_REQUESTS is created
ALTER TABLE users ADD CONSTRAINT fk_users_professor_request FOREIGN KEY (professor_request_id) REFERENCES professor_requests(id);

-- COURSES table
CREATE TABLE IF NOT EXISTS courses (
    id BIGSERIAL PRIMARY KEY,
    course_code VARCHAR(50) NOT NULL,
    course_name VARCHAR(255) NOT NULL,
    description TEXT,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL
);

-- COURSE DAYS table (for the days of the week when course meets)
CREATE TABLE IF NOT EXISTS course_days (
    course_id BIGINT NOT NULL,
    day_of_week VARCHAR(20) NOT NULL,
    PRIMARY KEY (course_id, day_of_week),
    CONSTRAINT fk_course_days_course FOREIGN KEY (course_id) REFERENCES courses(id)
);

-- USER_COURSES join table for Many-to-Many relationship
CREATE TABLE IF NOT EXISTS user_courses (
    user_id BIGINT NOT NULL,
    course_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, course_id),
    CONSTRAINT fk_user_courses_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_user_courses_course FOREIGN KEY (course_id) REFERENCES courses(id)
);

-- ATTENDANCE_RECORDS table
CREATE TABLE IF NOT EXISTS attendance_records (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    course_id BIGINT NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    verified BOOLEAN NOT NULL,
    network_identifier VARCHAR(255),
    verification_method VARCHAR(50),
    CONSTRAINT fk_attendance_records_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_attendance_records_course FOREIGN KEY (course_id) REFERENCES courses(id)
);

-- QUIZZES table
CREATE TABLE IF NOT EXISTS quizzes (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    start_date TIMESTAMP NOT NULL,
    end_date TIMESTAMP NOT NULL,
    duration_minutes INTEGER NOT NULL,
    course_id BIGINT NOT NULL,
    creator_id BIGINT NOT NULL,
    CONSTRAINT fk_quizzes_course FOREIGN KEY (course_id) REFERENCES courses(id),
    CONSTRAINT fk_quizzes_creator FOREIGN KEY (creator_id) REFERENCES users(id)
);

-- QUESTIONS table
CREATE TABLE IF NOT EXISTS questions (
    id BIGSERIAL PRIMARY KEY,
    quiz_id BIGINT NOT NULL,
    text TEXT NOT NULL,
    image_url VARCHAR(255),
    type VARCHAR(20) NOT NULL,
    points INTEGER DEFAULT 1,
    question_order INTEGER,
    correct_answer TEXT,
    CONSTRAINT fk_questions_quiz FOREIGN KEY (quiz_id) REFERENCES quizzes(id)
);

-- QUESTION_OPTIONS table
CREATE TABLE IF NOT EXISTS question_options (
    id BIGSERIAL PRIMARY KEY,
    question_id BIGINT NOT NULL,
    text TEXT NOT NULL,
    correct BOOLEAN NOT NULL,
    option_order INTEGER,
    CONSTRAINT fk_question_options_question FOREIGN KEY (question_id) REFERENCES questions(id)
);

-- QUIZ_ATTEMPTS table
CREATE TABLE IF NOT EXISTS quiz_attempts (
    id BIGSERIAL PRIMARY KEY,
    quiz_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP,
    completed BOOLEAN NOT NULL DEFAULT FALSE,
    score INTEGER,
    max_score INTEGER,
    CONSTRAINT fk_quiz_attempts_quiz FOREIGN KEY (quiz_id) REFERENCES quizzes(id),
    CONSTRAINT fk_quiz_attempts_student FOREIGN KEY (student_id) REFERENCES users(id)
);

-- QUIZ_ANSWERS table
CREATE TABLE IF NOT EXISTS quiz_answers (
    id BIGSERIAL PRIMARY KEY,
    attempt_id BIGINT NOT NULL,
    question_id BIGINT NOT NULL,
    selected_option_id BIGINT,
    text_answer TEXT,
    points_awarded INTEGER,
    graded BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_quiz_answers_attempt FOREIGN KEY (attempt_id) REFERENCES quiz_attempts(id),
    CONSTRAINT fk_quiz_answers_question FOREIGN KEY (question_id) REFERENCES questions(id),
    CONSTRAINT fk_quiz_answers_selected_option FOREIGN KEY (selected_option_id) REFERENCES question_options(id)
);

-- ASSIGNMENTS table
CREATE TABLE IF NOT EXISTS assignments (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    course_id BIGINT NOT NULL,
    creator_id BIGINT NOT NULL,
    due_date TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL,
    max_points INTEGER DEFAULT 100,
    CONSTRAINT fk_assignments_course FOREIGN KEY (course_id) REFERENCES courses(id),
    CONSTRAINT fk_assignments_creator FOREIGN KEY (creator_id) REFERENCES users(id)
);

-- ASSIGNMENT_FILES table (stored as embedded elements)
CREATE TABLE IF NOT EXISTS assignment_files (
    assignment_id BIGINT NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_url VARCHAR(255) NOT NULL,
    content_type VARCHAR(255) NOT NULL,
    file_size BIGINT,
    uploaded_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_assignment_files_assignment FOREIGN KEY (assignment_id) REFERENCES assignments(id)
);

-- ASSIGNMENT_SUBMISSIONS table
CREATE TABLE IF NOT EXISTS assignment_submissions (
    id BIGSERIAL PRIMARY KEY,
    assignment_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    notes TEXT,
    submission_date TIMESTAMP NOT NULL,
    graded_date TIMESTAMP,
    score INTEGER,
    feedback TEXT,
    graded BOOLEAN NOT NULL DEFAULT FALSE,
    late BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_assignment_submissions_assignment FOREIGN KEY (assignment_id) REFERENCES assignments(id),
    CONSTRAINT fk_assignment_submissions_student FOREIGN KEY (student_id) REFERENCES users(id)
);

-- SUBMISSION_FILES table (stored as embedded elements)
CREATE TABLE IF NOT EXISTS submission_files (
    submission_id BIGINT NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_url VARCHAR(255) NOT NULL,
    content_type VARCHAR(255) NOT NULL,
    file_size BIGINT,
    uploaded_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_submission_files_submission FOREIGN KEY (submission_id) REFERENCES assignment_submissions(id)
);

-- Create indexes for better performance
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_courses_code ON courses(course_code);
CREATE INDEX idx_attendance_user_course ON attendance_records(user_id, course_id);
CREATE INDEX idx_attendance_timestamp ON attendance_records(timestamp);
CREATE INDEX idx_quizzes_course ON quizzes(course_id);
CREATE INDEX idx_questions_quiz ON questions(quiz_id);
CREATE INDEX idx_quiz_attempts_quiz ON quiz_attempts(quiz_id);
CREATE INDEX idx_quiz_attempts_student ON quiz_attempts(student_id);
CREATE INDEX idx_quiz_answers_attempt ON quiz_answers(attempt_id);
CREATE INDEX idx_assignments_course ON assignments(course_id);
CREATE INDEX idx_assignments_due_date ON assignments(due_date);
CREATE INDEX idx_assignment_submissions_assignment ON assignment_submissions(assignment_id);
CREATE INDEX idx_assignment_submissions_student ON assignment_submissions(student_id); 