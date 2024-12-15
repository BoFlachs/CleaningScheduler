package cleaningscheduler.persistence;

import cleaningscheduler.domain.IPerson;
import cleaningscheduler.domain.ISchedule;
import cleaningscheduler.domain.ITask;
import cleaningscheduler.persistence.exceptions.DeleteNotAllowedException;
import org.joda.time.DateTime;

import java.util.List;
import java.util.Map;

public interface ISchedulerRepository {
    void deletePerson(String name) throws DeleteNotAllowedException;

    void deleteTask(String name) throws DeleteNotAllowedException;

    void deleteSchedule(DateTime createdAt);

    void save(String personName, Map<Integer, Integer> availabilityAssignment);

    void save(String taskName, int costs, String personName, boolean isPreferredFixed, int lastDoneAt,
              boolean isRepeated, int minRepeatInterval, int maxRepeatInterval);

    void save(ISchedule schedule);

    void changePerson(String personName, String newPersonName, Map<Integer, Integer> availabilityAssignment);

    void changeTask(String taskName, String newTaskName, int costs, String personName, boolean isPreferredFixed, int lastDoneAt,
                    boolean isRepeated, int minRepeatInterval, int maxRepeatInterval);

    IPerson getPerson(String name);

    ITask getTask(String name);

    ISchedule getSchedule(DateTime createdAt);

    List<IPerson> getAllPeople();

    List<ITask> getAllTasks();

    List<ISchedule> getAllSchedules();
}
