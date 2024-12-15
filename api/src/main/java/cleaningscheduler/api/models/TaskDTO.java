package cleaningscheduler.api.models;

import cleaningscheduler.domain.ITask;

public record TaskDTO(String name, int costs, PersonDTO preferredAssignee, boolean isPreferredFixed, int lastDoneAt,
                      boolean isRepeated, int minRepeatInterval, int maxRepeatInterval) {
    public TaskDTO(ITask task) {
        this(task.name(),
                task.costs(),
                new PersonDTO(task.preferredAssignee()),
                task.isPreferredFixed(),
                task.lastDoneAt(),
                task.isRepeated(),
                task.minRepeatInterval(),
                task.maxRepeatInterval()
        );
    }
}
