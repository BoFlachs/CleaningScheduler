package cleaningscheduler.persistence.VanillaSQL;

import cleaningscheduler.domain.*;
import cleaningscheduler.persistence.ISchedulerRepository;
import cleaningscheduler.persistence.exceptions.DeleteNotAllowedException;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static cleaningscheduler.persistence.VanillaSQL.SQLHelper.executeQuery;

public class SQLRepository implements ISchedulerRepository {
   SchedulerFactory factory = new SchedulerFactory();

    @Override
    public void deletePerson(String name) throws DeleteNotAllowedException {
        String query = "DELETE FROM Persons WHERE person_name = ?;";
        try (Connection connection = SQLHelper.getConnection()) {
            try (PreparedStatement personStmt = connection.prepareStatement(query)) {
                personStmt.setString(1, name);
                personStmt.executeUpdate();
            } catch (SQLException e) {
                throw new DeleteNotAllowedException("Some schedule or task is dependent on " + name);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Cannot connect the database!", e);
        }
    }

    @Override
    public void deleteTask(String name) throws DeleteNotAllowedException {
        String query = "DELETE FROM Tasks WHERE task_name = ?;";
        try (Connection connection = SQLHelper.getConnection()) {
            try (PreparedStatement taskStmt = connection.prepareStatement(query)) {
                taskStmt.setString(1, name);
                taskStmt.executeUpdate();
            } catch (SQLException e) {
                throw new DeleteNotAllowedException("Some schedule is dependent on " + name);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Cannot connect the database!", e);
        }
    }

    @Override
    public void deleteSchedule(DateTime createdAt) {
        String query = "DELETE FROM Schedules WHERE created_at = ?;";
        try (Connection connection = SQLHelper.getConnection()) {
            try (PreparedStatement scheduleStmt = connection.prepareStatement(query)) {
                scheduleStmt.setTimestamp(1, new java.sql.Timestamp(createdAt.getMillis()));
                scheduleStmt.executeUpdate();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Cannot connect the database!", e);
        }
    }

    @Override
    public void save(String personName, java.util.Map<Integer, Integer> availabilityAssignment) {
        try (Connection connection = SQLHelper.getConnection()) {
            connection.setAutoCommit(false);

            int personId;
            try (PreparedStatement personStmt = connection.prepareStatement(
                    "INSERT INTO Persons (person_name) VALUES (?)", Statement.RETURN_GENERATED_KEYS)) {
                personStmt.setString(1, personName);
                personStmt.executeUpdate();

                try (ResultSet generatedKeys = personStmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        personId = generatedKeys.getInt(1); // Get auto-generated ID
                    } else {
                        throw new SQLException("Failed to insert person, no ID obtained.");
                    }
                }
            }

            String query = "INSERT INTO AvailabilityAssignments(person_id, availability_minutes, week_id) VALUES" +
                    "(?, ?, (SELECT id FROM Weeks WHERE week_number = ?)),"
                            .repeat(availabilityAssignment.size() - 1)
                    + "(?, ?, (SELECT id FROM Weeks WHERE week_number = ?));";

            try (PreparedStatement availabilityStmt = connection.prepareStatement(query)) {
                AtomicInteger index = new AtomicInteger(1);
                availabilityAssignment.forEach((weekNumber, availability_minutes) -> {
                    try {
                        availabilityStmt.setInt(index.getAndIncrement(), personId);
                        availabilityStmt.setInt(index.getAndIncrement(), availability_minutes);
                        availabilityStmt.setInt(index.getAndIncrement(), weekNumber);
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                });
                availabilityStmt.executeUpdate();
            }

            connection.commit();
        } catch (SQLException e) {
            throw new IllegalStateException("Cannot connect the database!", e);
        }
    }

    @Override
    public void save( String taskName, int costs, String personName, boolean isPreferredFixed, int lastDoneAt,
                      boolean isRepeated, int minRepeatInterval, int maxRepeatInterval) {
        String query = """
                INSERT INTO Tasks (task_name, costs, preferred_person_id, is_preferred_fixed,
                    last_done_at, is_repeated, min_repeat_interval, max_repeat_interval)
                VALUES (?, ?, (SELECT id FROM Persons WHERE person_name = ? LIMIT 1),
                    ? , (SELECT id FROM Weeks WHERE week_number = ? LIMIT 1),
                    ?, ?, ?);""";

        try (Connection connection = SQLHelper.getConnection()) {
            try (PreparedStatement taskStmt = connection.prepareStatement(query)) {
                taskStmt.setString(1, taskName);
                taskStmt.setInt(2, costs);
                taskStmt.setString(3, personName);
                taskStmt.setBoolean(4, isPreferredFixed);
                taskStmt.setInt(5, lastDoneAt);
                taskStmt.setBoolean(6, isRepeated);
                taskStmt.setInt(7, minRepeatInterval);
                taskStmt.setInt(8, maxRepeatInterval);
                taskStmt.executeUpdate();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Cannot connect the database!", e);
        }
    }

    @Override
    public void save(ISchedule schedule) {
        try (Connection connection = SQLHelper.getConnection()) {
            connection.setAutoCommit(false);

            int scheduleID;
            try (PreparedStatement personStmt = connection.prepareStatement(
                    "INSERT INTO Schedules (created_at) VALUES (?)", Statement.RETURN_GENERATED_KEYS)) {
                personStmt.setTimestamp(1, new java.sql.Timestamp(schedule.createdAt().getMillis()));
                personStmt.executeUpdate();

                try (ResultSet generatedKeys = personStmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        scheduleID = generatedKeys.getInt(1); // Get auto-generated ID
                    } else {
                        throw new SQLException("Failed to insert schedule, no ID obtained.");
                    }
                }
            }

            String query = "INSERT INTO TaskAssignments(week_id, task_id, person_id, schedule_id) VALUES" +
                    """
                            ((SELECT id FROM Weeks WHERE week_number = ?),
                            (SELECT id FROM Tasks WHERE task_name = ?),
                            (SELECT id FROM Persons WHERE person_name = ?),  ?);
                            """;

            for (int i = 0; i < schedule.weekList().size(); i++) {
                IWeek week = schedule.weekListAsJava().get(i);
                week.getTaskAssignmentAsJava().forEach((task, person) -> {
                    try (PreparedStatement availabilityStmt = connection.prepareStatement(query)) {
                        try {
                            availabilityStmt.setInt(1, week.weekNumber());
                            availabilityStmt.setString(2, task.name());
                            availabilityStmt.setString(3, person.name());
                            availabilityStmt.setInt(4, scheduleID);
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                        availabilityStmt.executeUpdate();
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                });
            }

            connection.commit();
        } catch (SQLException e) {
            throw new IllegalStateException("Cannot connect the database!", e);
        }
    }

    @Override
    public void changePerson(String personName, String newPersonName, Map<Integer, Integer> availabilityAssignment) {
        try (Connection connection = SQLHelper.getConnection()) {
            connection.setAutoCommit(false);

            try (PreparedStatement personStmt = connection.prepareStatement(
                    "UPDATE Persons SET person_name= ? WHERE person_name = ?;")) {
                personStmt.setString(1, newPersonName);
                personStmt.setString(2, personName);
                personStmt.executeUpdate();
            }

            try (PreparedStatement deleteStmt = connection.prepareStatement(
                    "DELETE FROM AvailabilityAssignments WHERE person_id = " +
                            "(SELECT id FROM Persons WHERE person_name = ?);")) {
                deleteStmt.setString(1, newPersonName);
                deleteStmt.executeUpdate();
            }

            String query = "INSERT INTO AvailabilityAssignments(person_id, availability_minutes, week_id) VALUES" +
                    ("((SELECT id FROM Persons WHERE person_name = ?), " +
                            "?, " +
                            "(SELECT id FROM Weeks WHERE week_number = ?)),")
                            .repeat(availabilityAssignment.size() - 1)
                    + "((SELECT id FROM Persons WHERE person_name = ?)," +
                    "?, " +
                    "(SELECT id FROM Weeks WHERE week_number = ?));";

            try (PreparedStatement availabilityStmt = connection.prepareStatement(query)) {
                AtomicInteger index = new AtomicInteger(1);
                availabilityAssignment.forEach((weekNumber, availability_minutes) -> {
                    try {
                        availabilityStmt.setString(index.getAndIncrement(), newPersonName);
                        availabilityStmt.setInt(index.getAndIncrement(), availability_minutes);
                        availabilityStmt.setInt(index.getAndIncrement(), weekNumber);
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                });
                availabilityStmt.executeUpdate();
            }

            connection.commit();
        } catch (SQLException e) {
            throw new IllegalStateException("Cannot connect the database!", e);
        }
    }

    @Override
    public void changeTask(String taskName, String newTaskName, int costs, String personName, boolean isPreferredFixed, int lastDoneAt, boolean isRepeated, int minRepeatInterval, int maxRepeatInterval) {
        String query = """
                UPDATE Tasks
                SET task_name = ?,
                    costs = ?,
                    preferred_person_id = (SELECT id FROM Persons WHERE person_name = ? LIMIT 1),
                    is_preferred_fixed = ?,
                    last_done_at = ?,
                    is_repeated = ?,
                    min_repeat_interval = ?,
                    max_repeat_interval = ?
                    WHERE task_name = ?;""";

        try (Connection connection = SQLHelper.getConnection()) {
            try (PreparedStatement taskStmt = connection.prepareStatement(query)) {
                taskStmt.setString(1, newTaskName);
                taskStmt.setInt(2, costs);
                taskStmt.setString(3, personName);
                taskStmt.setBoolean(4, isPreferredFixed);
                taskStmt.setInt(5, lastDoneAt);
                taskStmt.setBoolean(6, isRepeated);
                taskStmt.setInt(7, minRepeatInterval);
                taskStmt.setInt(8, maxRepeatInterval);
                taskStmt.setString(9, taskName);
                taskStmt.executeUpdate();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Cannot connect the database!", e);
        }
    }


    @Override
    public IPerson getPerson(String personName) {
        String query = """
                SELECT P.person_name AS name,
                    W.week_number AS weekNumber,
                    AA.availability_minutes AS availability
                FROM Persons AS P
                JOIN AvailabilityAssignments AS AA ON P.id = AA.person_id
                JOIN Weeks AS W ON W.id = AA.week_id
                WHERE P.person_name = "%s";
                """.formatted(personName);


        return executeQuery(query, result -> {
            ScalaMapBuilder<Object, Object> availabilityAssignment = new ScalaMapBuilder<>();
            while (result.next()) {
                int weekNumber = result.getInt("weekNumber");
                int availability = result.getInt("availability");

                availabilityAssignment.update(weekNumber, availability);
            }
            return factory.createPerson(personName, availabilityAssignment.getMap());
        });
    }

    @Override
    public ITask getTask(String name) {
        String query = """
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
                WHERE T.task_name = "%s";""".formatted(name);

        return executeQuery(query, result -> {
            if (result.next()) {
                int costs = result.getInt("costs");
                String preferredAssignee = result.getString("preferredAssignee");
                boolean isPrefFixed = result.getBoolean("isPreferredFixed");
                int weekNumber = result.getInt("weekNumber");
                boolean isRepeated = result.getBoolean("isRepeated");
                int minRepeatInterval = result.getInt("minRepeatInterval");
                int maxRepeatInterval = result.getInt("maxRepeatInterval");

                ScalaMapBuilder<Object, Object> availabilityAssignment = new ScalaMapBuilder<>();
                int assignmentWeekNumber = result.getInt("availabilityWeekNumber");
                int availability = result.getInt("availability");
                availabilityAssignment.update(assignmentWeekNumber, availability);
                while (result.next()) {
                    assignmentWeekNumber = result.getInt("availabilityWeekNumber");
                    availability = result.getInt("availability");
                    availabilityAssignment.update(assignmentWeekNumber, availability);
                }
                Person person = factory.createPerson(preferredAssignee, availabilityAssignment.getMap());

                return factory.createTask(name, costs, person, isPrefFixed, weekNumber, isRepeated, minRepeatInterval, maxRepeatInterval);
            } else {
                return null;
            }
        });
    }

    @Override
    public ISchedule getSchedule(DateTime createdAt) {
        String query = """
                    SELECT S.created_at AS createdAt,
                    W.week_number AS weekNumber,
                    T.task_name AS taskName,
                    P.person_name AS personName
                FROM TaskAssignments AS TA
                JOIN Weeks AS W ON W.id = TA.week_id
                JOIN Tasks AS T ON T.id = TA.task_id
                JOIN Persons AS P ON P.id = TA.person_id
                JOIN Schedules AS S ON S.id = TA.schedule_id
                WHERE S.created_at = "%s"
                ORDER BY W.week_number;""".formatted(createdAt.toString());


        return executeQuery(query, result -> {
            ScalaListBuilder<Week> listBuilder = new ScalaListBuilder<>();
            if (result.next()) {
                int currentWeek = result.getInt("weekNumber");
                ScalaMapBuilder<Task, Person> taskAssignmentBuilder = new ScalaMapBuilder<>();
                Task task;
                Person person;
                do {
                    if (result.getInt("weekNumber") != currentWeek) {
                        Week week = new Week(currentWeek, taskAssignmentBuilder.getMap());
                        listBuilder.add(week);

                        taskAssignmentBuilder = new ScalaMapBuilder<>();
                        currentWeek = result.getInt("weekNumber");
                    }
                    task = (Task) getTask(result.getString("taskName"));
                    person = (Person) getPerson(result.getString("personName"));
                    taskAssignmentBuilder.update(task, person);
                } while (result.next());

                Week week = factory.createWeek(currentWeek, taskAssignmentBuilder.getMap());
                listBuilder.add(week);
            }
            scala.collection.immutable.List<IWeek> weekList = listBuilder.getList().map(week -> week);
            return factory.createSchedule(createdAt, weekList);
        });
    }


    @Override
    public List<IPerson> getAllPeople() {
        String query = """
                SELECT P.person_name AS name,
                    W.week_number AS weekNumber,
                    AA.availability_minutes AS availability
                FROM Persons AS P
                JOIN AvailabilityAssignments AS AA ON P.id = AA.person_id
                JOIN Weeks AS W ON W.id = AA.week_id;
                """;

        return executeQuery(query, result -> {
            List<IPerson> personList = new ArrayList<>();
            while (result.next()) {
                String name = result.getString("name");
                ScalaMapBuilder<Object, Object> availabilityAssignment = new ScalaMapBuilder<>();
                int weekNumber;
                int availability;

                do {
                    if (!result.getString("name").equals(name)) {
                        personList.add(factory.createPerson(name, availabilityAssignment.getMap()));

                        name = result.getString("name");
                        availabilityAssignment = new ScalaMapBuilder<>();
                    }

                    weekNumber = result.getInt("weekNumber");
                    availability = result.getInt("availability");
                    availabilityAssignment.update(weekNumber, availability);
                } while (result.next());
                personList.add(factory.createPerson(name, availabilityAssignment.getMap()));
            }

            return personList;
        });
    }

    @Override
    public List<ITask> getAllTasks() {
        String query = "SELECT * FROM Tasks;";

        return executeQuery(query, result -> {
            List<ITask> taskList = new ArrayList<>();
            while (result.next()) {
                String taskName = result.getString("task_name");
                taskList.add(getTask(taskName));
            }
            return taskList;
        });
    }


    @Override
    public List<ISchedule> getAllSchedules() {
        String query = "SELECT * FROM Schedules ORDER BY created_at DESC;";

        return executeQuery(query, result -> {
            List<ISchedule> scheduleList = new ArrayList<>();
            while (result.next()) {
                String createdAtString = result.getString("created_at");
                DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");

                DateTime createdAt = formatter.withZoneUTC().parseDateTime(createdAtString);
                createdAt.withZoneRetainFields(DateTimeZone.UTC);

                scheduleList.add(getSchedule(createdAt));
            }
            return scheduleList;
        });
    }
}
