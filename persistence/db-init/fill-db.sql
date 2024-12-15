-- @block
INSERT INTO Persons(person_name)
VALUES ("Person A"),
    ("Person B"),
    ("Person C");


-- @block
INSERT INTO AvailabilityAssignments(person_id, week_id, availability_minutes)
VALUES (1, 30, 60), (1, 31, 30), (1, 32, 90),
    (2, 30, 45), (2, 31, 20), (2, 32, 60),
    (3, 30, 60), (3, 31, 15), (3, 32, 30);

-- @block
INSERT INTO Tasks(task_name, costs, preferred_person_id, is_preferred_fixed, last_done_at,
    is_repeated, min_repeat_interval, max_repeat_interval)
VALUES ("Task 1", 20, 1, true, 29, true, 1, 5),
    ("Task 2", 60, 2, true, 28, false, 0, 0),
    ("Task 3", 30, 1, false, 28, false, 0, 0);

-- @block
INSERT INTO Schedules(created_at)
VALUES ("2024-11-15 00:00:00"), ("2023-8-3 00:00:00");

-- @block
INSERT INTO TaskAssignments (week_id, task_id, person_id, schedule_id)
VALUES (30, 1, 1, 1),
    (31, 2, 3, 1),
    (31, 1, 2, 1),
    (32, 1, 2, 1),
    (30, 1, 1, 2),
    (31, 2, 3, 2);