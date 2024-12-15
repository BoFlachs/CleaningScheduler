package cleaningscheduler.api.models;

import cleaningscheduler.domain.ISchedule;
import org.joda.time.DateTime;

import java.util.List;
import java.util.stream.Collectors;


public record ScheduleDTO(DateTime createdAt, List<WeekDTO> weekList, int score) {
    public ScheduleDTO(ISchedule schedule, int score){
        this(
                schedule.createdAt(),
                schedule.weekListAsJava()
                        .stream()
                        .map(WeekDTO::new)
                        .collect(Collectors.toList()),
                score
        );
    }
}
