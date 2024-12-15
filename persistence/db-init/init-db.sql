-- @block
CREATE DATABASE IF NOT EXISTS cleaning_scheduler;
USE cleaning_scheduler;

-- @block
CREATE TABLE IF NOT EXISTS Persons(
    id INT NOT NULL AUTO_INCREMENT,
    person_name VARCHAR(255) NOT NULL,
    PRIMARY KEY(id)
);
-- @block
CREATE TABLE IF NOT EXISTS Weeks(
    id INT NOT NULL AUTO_INCREMENT,
    week_number INT NOT NULL,
    PRIMARY KEY(id)
);
CREATE PROCEDURE InsertWeeks()
BEGIN
    DECLARE weekNumber INT DEFAULT 1;
    
    WHILE weekNumber <= 52 DO
        INSERT INTO Weeks(week_number) VALUES (weekNumber);
        SET weekNumber = weekNumber + 1;
    END WHILE;
END;

CALL InsertWeeks();

-- @block
CREATE TABLE IF NOT EXISTS AvailabilityAssignments(
    id INT NOT NULL AUTO_INCREMENT,
    person_id INT NOT NULL,
    week_id INT NOT NULL,
    availability_minutes INT NOT NULL,
    PRIMARY KEY(id),
    FOREIGN KEY(person_id) REFERENCES Persons(id) ON DELETE CASCADE,
    FOREIGN KEY(week_id) REFERENCES Weeks(id)
);


-- @block
CREATE TABLE IF NOT EXISTS Tasks(
    id INT NOT NULL AUTO_INCREMENT,
    task_name VARCHAR(255) NOT NULL,
    costs INT NOT NULL,
    preferred_person_id INT NOT NULL,
    is_preferred_fixed BOOLEAN NOT NULL,
    last_done_at INT NOT NULL,
    is_repeated BOOLEAN NOT NULL,
    min_repeat_interval INT NOT NULL,
    max_repeat_interval INT NOT NULL,
    PRIMARY KEY(id),
    FOREIGN KEY(preferred_person_id) REFERENCES Persons(id),
    FOREIGN KEY(last_done_at) REFERENCES Weeks(id)
);

-- @block
CREATE TABLE IF NOT EXISTS Schedules(
    id INT NOT NULL AUTO_INCREMENT,
    created_at DATETIME NOT NULL,
    PRIMARY KEY(id)
);

-- @block
CREATE TABLE IF NOT EXISTS TaskAssignments( 
    id INT NOT NULL AUTO_INCREMENT,
    schedule_id INT NOT NULL,
    week_id INT NOT NULL,
    task_id INT NOT NULL,
    person_id INT NOT NULL,
    PRIMARY KEY(id),
    FOREIGN KEY(task_id) REFERENCES Tasks(id),
    FOREIGN KEY(person_id) REFERENCES Persons(id),
    FOREIGN KEY(week_id) REFERENCES Weeks(id),
    FOREIGN KEY(schedule_id) REFERENCES Schedules(id) ON DELETE CASCADE
);