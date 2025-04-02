-- Conditionally reset sequences to avoid ID conflicts on fresh deployments

-- Create a function to safely reset sequence values
CREATE OR REPLACE FUNCTION reset_sequence_if_needed(seq_name text, table_name text, column_name text) 
RETURNS void AS $$
DECLARE
  max_id bigint;
BEGIN
  EXECUTE format('SELECT COALESCE(MAX(%I), 0) + 1 FROM %I', column_name, table_name) INTO max_id;
  EXECUTE format('SELECT setval(%L, %L, false)', seq_name, max_id);
END;
$$ LANGUAGE plpgsql;

-- Reset sequences for all major tables
SELECT reset_sequence_if_needed('users_id_seq', 'users', 'id');
SELECT reset_sequence_if_needed('professor_requests_id_seq', 'professor_requests', 'id');
SELECT reset_sequence_if_needed('courses_id_seq', 'courses', 'id');
SELECT reset_sequence_if_needed('attendance_records_id_seq', 'attendance_records', 'id');
SELECT reset_sequence_if_needed('quizzes_id_seq', 'quizzes', 'id');
SELECT reset_sequence_if_needed('questions_id_seq', 'questions', 'id');
SELECT reset_sequence_if_needed('question_options_id_seq', 'question_options', 'id');
SELECT reset_sequence_if_needed('quiz_attempts_id_seq', 'quiz_attempts', 'id');
SELECT reset_sequence_if_needed('quiz_answers_id_seq', 'quiz_answers', 'id');
SELECT reset_sequence_if_needed('assignments_id_seq', 'assignments', 'id');
SELECT reset_sequence_if_needed('assignment_submissions_id_seq', 'assignment_submissions', 'id'); 