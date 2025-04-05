A SQL migration script to update course times:
UPDATE courses SET start_time = '00:00', end_time = '23:59' WHERE 1=1;
