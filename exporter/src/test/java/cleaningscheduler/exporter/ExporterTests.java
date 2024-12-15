package cleaningscheduler.exporter;

import cleaningscheduler.domain.*;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.jupiter.api.Test;
import scala.collection.immutable.*;
import org.joda.time.DateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class ExporterTests {

    private ISchedule getSchedule() {
        SchedulerFactory factory = new SchedulerFactory();
        Map<Object, Object> availabilityAssignment = Map$.MODULE$.empty().updated(1, 1);
        Map<Object, Object> updatedAssignment = availabilityAssignment.updated(2, 8);
        IPerson person1 = factory.createPerson("Person A", updatedAssignment);
        IPerson person2 = factory.createPerson("Person B", availabilityAssignment);

        ITask task = factory.createTask("Task 1", 40, person1, true, 20, true, 1, 5);

        Map<ITask, IPerson> taskAssignment1 = HashMap$.MODULE$.<ITask, IPerson>empty().updated(task, person1);
        Map<ITask, IPerson> taskAssignment2 = HashMap$.MODULE$.<ITask, IPerson>empty().updated(task, person2);
        IWeek week1 = factory.createWeek(30, taskAssignment1);
        IWeek week2 = factory.createWeek(31, taskAssignment2);

        List<IWeek> weekList = List$.MODULE$.empty().$colon$colon(week2).$colon$colon(week1);
        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        DateTime createdAt = formatter.parseDateTime("2020-11-30T00:00:00.000Z");

        return factory.createSchedule(createdAt, weekList);
    }

    @Test
    public void toPrettyStringShouldShowCreatedAtAndScore() {
        ISchedule schedule = getSchedule();
        int score = -900;

        String prettyString = Exporter.scheduleToPrettyString(schedule, score);

        String expectedStartOfString = """
                Schedule
                
                Created at: 2020-11-30 00:00:00.000
                Score: -900
                """;

        assert (prettyString.startsWith(expectedStartOfString));
    }

    @Test
    public void toPrettyStringShouldShowAllWeeks() {
        ISchedule schedule = getSchedule();
        int score = -900;

        String prettyString = Exporter.scheduleToPrettyString(schedule, score);

        String weekListString = """
                Week 30:
                    Task 1    - Person A, 40 Minutes
                
                Week 31:
                    Task 1    - Person B, 40 Minutes
                """;

        assert (prettyString.endsWith(weekListString));
    }

    @Test
    public void toPrettyStringFullTest() {
        ISchedule schedule = getSchedule();
        int score = -900;

        String prettyString = Exporter.scheduleToPrettyString(schedule, score);

        String weekListString = """
                Schedule
                
                Created at: 2020-11-30 00:00:00.000
                Score: -900
                
                Week 30:
                    Task 1    - Person A, 40 Minutes
                
                Week 31:
                    Task 1    - Person B, 40 Minutes
                """;

        assertEquals(weekListString, prettyString);
    }


    @Test
    public void toPrettyStringWithVariableTaskNames() {
        SchedulerFactory factory = new SchedulerFactory();
        Map<Object, Object> availabilityAssignment = Map$.MODULE$.empty().updated(1, 1);
        Map<Object, Object> updatedAssignment = availabilityAssignment.updated(2, 8);
        IPerson person1 = factory.createPerson("Person A", updatedAssignment);
        IPerson person2 = factory.createPerson("Person B", availabilityAssignment);

        ITask task1 = factory.createTask("Tsk1", 40, person1, true, 20, true, 1, 5);
        ITask task2 = factory.createTask("De was doen enzo", 40, person1, true, 20, true, 1, 5);

        Map<ITask, IPerson> taskAssignment1 = HashMap$.MODULE$.<ITask, IPerson>empty().updated(task1, person1);
        Map<ITask, IPerson> taskAssignment2 = HashMap$.MODULE$.<ITask, IPerson>empty().updated(task2, person2);
        IWeek week1 = factory.createWeek(30, taskAssignment1);
        IWeek week2 = factory.createWeek(31, taskAssignment2);

        List<IWeek> weekList = List$.MODULE$.empty().$colon$colon(week2).$colon$colon(week1);
        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        DateTime createdAt = formatter.parseDateTime("2020-11-30T00:00:00.000Z");

        ISchedule schedule = factory.createSchedule(createdAt, weekList);
        int score = -900;

        String prettyString = Exporter.scheduleToPrettyString(schedule, score);

        String weekListString = """
                Schedule
                
                Created at: 2020-11-30 00:00:00.000
                Score: -900
                
                Week 30:
                    Tsk1                - Person A, 40 Minutes
                
                Week 31:
                    De was doen enzo    - Person B, 40 Minutes
                """;

        assertEquals(weekListString, prettyString);
    }
}
