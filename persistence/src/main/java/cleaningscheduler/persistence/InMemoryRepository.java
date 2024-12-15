package cleaningscheduler.persistence;

import cleaningscheduler.domain.IPerson;
import cleaningscheduler.domain.ISchedule;
import cleaningscheduler.domain.ITask;
import cleaningscheduler.persistence.exceptions.DeleteNotAllowedException;
import org.joda.time.DateTime;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public class InMemoryRepository{
    HashMap<String, IPerson> personRepo = new HashMap<>();
    HashMap<String, ITask> taskRepo = new HashMap<>();
    HashMap<DateTime, ISchedule> scheduleRepo = new HashMap<>();

    public void deletePerson(String name) throws DeleteNotAllowedException {
        boolean noDependentTasks = taskRepo.values().stream()
                .anyMatch(task -> task.preferredAssignee().name().equals(name));

        boolean noDependentSchedules = scheduleRepo.values().stream()
                .anyMatch(schedule -> schedule.weekList()
                        .exists(week -> week.getTaskAssignmentAsJava().values().stream()
                                .anyMatch(assignedPerson -> assignedPerson.name().equals(name))));

        if (noDependentTasks && noDependentSchedules) {
            taskRepo.remove(name);
        } else {
            throw new DeleteNotAllowedException("There were schedules and/or tasks dependent on " + name);
        }
    }

    public void deleteTask(String name) throws DeleteNotAllowedException {
        boolean noDependentSchedules = scheduleRepo.values().stream()
                .anyMatch(schedule -> schedule.weekList()
                        .exists(week -> week.getTaskAssignmentAsJava()
                                .keySet().stream()
                                .anyMatch(task -> task.name().equals(name))));

        if (noDependentSchedules) {
            taskRepo.remove(name);
        } else {
            throw new DeleteNotAllowedException("There were schedules dependent on " + name);
        }
    }

    public void deleteSchedule(DateTime createdAt) {
        scheduleRepo.remove(createdAt);
    }

    public void save(IPerson person) {
        personRepo.put(person.name(), person);
    }

    public void save(ITask task) {
        taskRepo.put(task.name(), task);
    }

    public void save(ISchedule schedule) {
        scheduleRepo.put(schedule.createdAt(), schedule);
    }

    public void change(String name, IPerson person) {
        personRepo.put(name, person);
    }

    public void change(String name, ITask task) {
        taskRepo.put(name, task);
    }

    public void change(DateTime createdAt, ISchedule schedule) {
        scheduleRepo.put(createdAt, schedule);
    }

    public IPerson getPerson(String name) {
        return personRepo.get(name);
    }

    public ITask getTask(String name) {
        return taskRepo.get(name);
    }

    public ISchedule getSchedule(DateTime createdAt) {
        return scheduleRepo.get(createdAt);
    }

    public List<IPerson> getAllPeople() {
        return personRepo.values().stream().toList();
    }

    public List<ITask> getAllTasks() {
        return taskRepo.values().stream().toList();
    }

    public List<ISchedule> getAllSchedules() {
        return scheduleRepo.values().stream().toList();
    }
}
