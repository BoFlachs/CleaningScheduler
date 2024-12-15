package cleaningscheduler.api.models;

import cleaningscheduler.domain.IPerson;

import java.util.Map;

public record PersonDTO(String name, Map<Integer, Integer> availabilityAssignment) {
    public PersonDTO(IPerson person){
        this(person.name(), person.getAvailabilityAssignmentAsJavaMap());
    }
}

