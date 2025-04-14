-- Seed initial Computer Science courses with 24/7 availability

DO $$
DECLARE
    course_id_var BIGINT;
    day_record TEXT;
    all_days TEXT[] := '{"MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY"}';
BEGIN
    -- Insert Course 1: Data Structures and Algorithms
    INSERT INTO courses (course_code, course_name, description, start_time, end_time)
    VALUES ('CS201', 'Data Structures and Algorithms', 'Fundamental data structures (lists, trees, graphs) and algorithm analysis.', '00:00:00', '23:59:59')
    ON CONFLICT (course_code) DO NOTHING
    RETURNING id INTO course_id_var; -- Get the ID of the inserted/existing course

    -- Insert days only if the course was newly inserted or found
    IF course_id_var IS NOT NULL THEN
        FOREACH day_record IN ARRAY all_days
        LOOP
            INSERT INTO course_days (course_id, day_of_week) VALUES (course_id_var, day_record)
            ON CONFLICT (course_id, day_of_week) DO NOTHING;
        END LOOP;
    END IF;

    -- Insert Course 2: Object-Oriented Programming
    INSERT INTO courses (course_code, course_name, description, start_time, end_time)
    VALUES ('CS210', 'Object-Oriented Programming', 'Principles of OOP using Java, including inheritance, polymorphism, and design patterns.', '00:00:00', '23:59:59')
    ON CONFLICT (course_code) DO NOTHING
    RETURNING id INTO course_id_var;

    IF course_id_var IS NOT NULL THEN
        FOREACH day_record IN ARRAY all_days
        LOOP
            INSERT INTO course_days (course_id, day_of_week) VALUES (course_id_var, day_record)
            ON CONFLICT (course_id, day_of_week) DO NOTHING;
        END LOOP;
    END IF;

    -- Insert Course 3: Database Systems
    INSERT INTO courses (course_code, course_name, description, start_time, end_time)
    VALUES ('CS305', 'Database Systems', 'Relational database design, SQL, normalization, and transaction management.', '00:00:00', '23:59:59')
    ON CONFLICT (course_code) DO NOTHING
    RETURNING id INTO course_id_var;

    IF course_id_var IS NOT NULL THEN
        FOREACH day_record IN ARRAY all_days
        LOOP
            INSERT INTO course_days (course_id, day_of_week) VALUES (course_id_var, day_record)
            ON CONFLICT (course_id, day_of_week) DO NOTHING;
        END LOOP;
    END IF;

    -- Insert Course 4: Operating Systems
    INSERT INTO courses (course_code, course_name, description, start_time, end_time)
    VALUES ('CS350', 'Operating Systems', 'Core concepts of operating systems: processes, threads, memory management, file systems.', '00:00:00', '23:59:59')
    ON CONFLICT (course_code) DO NOTHING
    RETURNING id INTO course_id_var;

    IF course_id_var IS NOT NULL THEN
        FOREACH day_record IN ARRAY all_days
        LOOP
            INSERT INTO course_days (course_id, day_of_week) VALUES (course_id_var, day_record)
            ON CONFLICT (course_id, day_of_week) DO NOTHING;
        END LOOP;
    END IF;

    -- Insert Course 5: Artificial Intelligence
    INSERT INTO courses (course_code, course_name, description, start_time, end_time)
    VALUES ('CS460', 'Artificial Intelligence', 'Introduction to AI concepts, search algorithms, machine learning basics, and knowledge representation.', '00:00:00', '23:59:59')
    ON CONFLICT (course_code) DO NOTHING
    RETURNING id INTO course_id_var;

    IF course_id_var IS NOT NULL THEN
        FOREACH day_record IN ARRAY all_days
        LOOP
            INSERT INTO course_days (course_id, day_of_week) VALUES (course_id_var, day_record)
            ON CONFLICT (course_id, day_of_week) DO NOTHING;
        END LOOP;
    END IF;

END $$;