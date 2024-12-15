package cleaningscheduler.api.models;

import cleaningscheduler.domain.IWeek;

import java.util.Map;
import java.util.stream.Collectors;

public record WeekDTO(int weekNumber, Map<TaskDTO, PersonDTO> taskAssignment) {
    public WeekDTO(IWeek week) {
        this(week.weekNumber(),
                week.getTaskAssignmentAsJava().entrySet().stream()
                        .collect(Collectors.toMap(
                               entry -> new TaskDTO(entry.getKey()),
                               entry -> new PersonDTO(entry.getValue())
                        ) )
        );
    }
}
