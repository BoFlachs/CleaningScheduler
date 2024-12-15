-- @block
-- All person information for Person A
SELECT P.person_name AS name, 
    W.week_number AS weekNumber, 
    AA.availability_minutes AS availability
FROM Persons AS P
JOIN AvailabilityAssignments AS AA ON P.id = AA.person_id
JOIN Weeks AS W ON W.id = AA.week_id
WHERE P.person_name = "JavaPerson";

-- @block 
-- All task information for Task 1
 SELECT T.task_name AS name,
                    T.costs AS costs,
                    P.person_name AS preferredAssignee,
                    WA.week_number AS availabilityWeekNumber, 
                    AA.availability_minutes AS availability,
                    T.is_preferred_fixed AS isPreferredFixed,
                    W.week_number AS weekNumber,
                    T.is_repeated AS isRepeated,
                    T.min_repeat_interval AS minRepeatInterval,
                    T.max_repeat_interval AS maxRepeatInterval
                FROM Tasks AS T
                JOIN Persons AS P ON T.preferred_person_id = P.id
                JOIN AvailabilityAssignments AS AA ON P.id = AA.person_id
                JOIN Weeks AS W ON W.id = T.last_done_at
                JOIN Weeks AS WA ON WA.id = AA.week_id
                WHERE T.task_name = "JavaTask";

-- @block
-- All information for schedule created at 2024-11-15
SELECT S.created_at AS createdAt, 
    W.week_number AS weekNumber, 
    T.task_name AS taskName, 
    P.person_name AS personName
FROM TaskAssignments AS TA 
JOIN Weeks AS W ON W.id = TA.week_id
JOIN Tasks AS T ON T.id = TA.task_id
JOIN Persons AS P ON P.id = TA.person_id
JOIN Schedules AS S ON S.id = TA.schedule_id
WHERE S.created_at = "2024-11-21 13:33:49"
ORDER BY W.week_number;

-- @block
-- Change task 
UPDATE Tasks
SET costs = 30 
WHERE task_name = "Task 1";

-- @block
-- Insert person 
INSERT INTO Persons(person_name)
VALUES ("Person Naam");

SET @last_person_id = LAST_INSERT_ID();

INSERT INTO AvailabilityAssignments(person_id, availability_minutes, week_id)
VALUES (@last_person_id, 
    45, 
    (SELECT id
    FROM Weeks
    WHERE week_number = 52 
    LIMIT 1));

-- @block
-- Insert task
INSERT INTO Tasks (task_name, costs, preferred_person_id, is_preferred_fixed,
    last_done_at, is_repeated, min_repeat_interval, max_repeat_interval)
VALUES (
    "New task", 
    35,
    (SELECT id FROM Persons WHERE person_name = "Person Naam" LIMIT 1),
    false, 
    (SELECT id FROM Weeks WHERE week_number = 42 LIMIT 1),
    false, 0, 0
  );

-- @block
-- Insert schedule
INSERT INTO Schedules(created_at)
VALUES ("2020-1-12 00:00:00");

SET @latest_schedule_id = LAST_INSERT_ID();

INSERT INTO TaskAssignments (week_id, task_id, person_id, schedule_id)
VALUES (
    (SELECT id FROM Weeks WHERE week_number = 12 LIMIT 1),
    (SELECT id FROM Tasks WHERE task_name = "New Task" LIMIT 1),
    (SELECT id FROM Persons WHERE person_name = "Person Naam" LIMIT 1),
    @latest_schedule_id
  );