package cleaningscheduler.api.controllers;

import cleaningscheduler.api.models.PersonDTO;
import cleaningscheduler.api.models.ScheduleDTO;
import cleaningscheduler.api.models.TaskDTO;
import cleaningscheduler.domain.*;
import cleaningscheduler.domain.scheduler.IScheduler;
import cleaningscheduler.exporter.Exporter;
import cleaningscheduler.persistence.ISchedulerRepository;
import cleaningscheduler.persistence.exceptions.DeleteNotAllowedException;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.glassfish.jersey.jackson.internal.jackson.jaxrs.json.annotation.JSONP;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.awt.*;
import java.util.Map;

@Path("/cleaning-scheduler/api")
public class ScheduleController {

    private final ISchedulerRepository repository;
    private final IScheduleFactory factory;
    private final IScheduler scheduler;

    public ScheduleController(
            IScheduleFactory factory,
            ISchedulerRepository repository,
            IScheduler scheduler) {
        this.repository = repository;
        this.factory = factory;
        this.scheduler = scheduler;
//        fillDataBaseWithMockExamples();
    }

    @Path("/getPerson")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPerson(@QueryParam("name") String name) {

        IPerson person = repository.getPerson(name);

        PersonDTO output = new PersonDTO(person);

        return Response.status(200).entity(output).build();
    }

    @Path("/getTask")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTask(@QueryParam("name") String name) {

        ITask task = repository.getTask(name);

        TaskDTO output = new TaskDTO(task);

        return Response.status(200).entity(output).build();
    }

    @Path("/getSchedule")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSchedule(@QueryParam("createdAt") String createdAtString) {
        DateTime createdAt = DateTime.parse(createdAtString);

        ISchedule schedule = repository.getSchedule(createdAt);
        java.util.List<IPerson> personList = repository.getAllPeople();
        int score = scheduler.calculateScore(schedule, personList);

        ScheduleDTO output = new ScheduleDTO(schedule, score);

        return Response.status(200).entity(output).build();
    }


    @Path("/getAllPeople")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllPeople() {

        java.util.List<IPerson> personList = repository.getAllPeople();

        java.util.List<PersonDTO> output = personList.stream()
                .map(PersonDTO::new)
                .toList();

        return Response.status(200).entity(output).build();
    }

    @Path("/getAllTasks")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllTasks() {

        java.util.List<ITask> taskList = repository.getAllTasks();

        java.util.List<TaskDTO> output = taskList.stream()
                .map(TaskDTO::new)
                .toList();

        return Response.status(200).entity(output).build();
    }

    @Path("/getAllSchedules")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllSchedules() {

        java.util.List<ISchedule> scheduleList = repository.getAllSchedules();
        java.util.List<IPerson> personList = repository.getAllPeople();

        java.util.List<ScheduleDTO> output = scheduleList.stream()
                .map(schedule -> {
                    int score = scheduler.calculateScore(schedule, personList);
                    return new ScheduleDTO(schedule, score);
                })
                .toList();

        return Response.status(200).entity(output).build();
    }

    @Path("/newSchedule")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getNewSchedule(@QueryParam("startWeek") int startWeek,
                                   @DefaultValue("1") @QueryParam("scheduleLength") int scheduleLength,
                                   @DefaultValue("false") @QueryParam("isIntervalVariable") boolean isIntervalVariable,
                                   @DefaultValue("false") @QueryParam("isBalanced") boolean isBalanced
    ) {
        java.util.List<IPerson> personList = repository.getAllPeople();
        java.util.List<ITask> taskList = repository.getAllTasks();

        ISchedule schedule =
                !isBalanced ? scheduler.schedule(personList, taskList, startWeek, scheduleLength, isIntervalVariable)
                        : scheduler.scheduleBalanced(personList, taskList, startWeek, scheduleLength, isIntervalVariable);
        int score =
                !isBalanced ? scheduler.calculateScore(schedule, personList)
                        : scheduler.calculateScoreBalanced(schedule, personList);

        repository.save(schedule);

        ScheduleDTO output = new ScheduleDTO(schedule, score);

        return Response.status(200).entity(output).build();
    }

    @Path("/deletePerson")
    @DELETE
    public Response deletePerson(@QueryParam("name") String personName) {
        try {
            repository.deletePerson(personName);
            return Response.status(204).build();
        } catch (DeleteNotAllowedException e) {
            return Response.status(409).build();
        }
    }

    @Path("/deleteTask")
    @DELETE
    public Response deleteTask(@QueryParam("name") String taskName) {
        try {
            repository.deleteTask(taskName);
            return Response.status(204).build();
        } catch (DeleteNotAllowedException e) {
            return Response.status(409).build();
        }
    }

    @Path("/deleteSchedule")
    @DELETE
    public Response deleteSchedule(@QueryParam("createdAt") String createdAtString) {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        DateTime createdAt = formatter.parseDateTime(createdAtString);

        try {
            repository.deleteSchedule(createdAt);
            return Response.status(204).build();
        } catch (Exception e) {
            return Response.status(500).build();
        }
    }

    @Path("addPerson")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addPerson(PersonDTO body) {

        Map<Integer, Integer> availabilityAssignment = body.availabilityAssignment();
        availabilityAssignment.entrySet().removeIf(entry -> entry.getValue() == 0);

        repository.save(body.name(), availabilityAssignment);

        return Response.status(200).entity(body).build();
    }

    @Path("addTask")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addTask(TaskDTO body) {

        repository.save(body.name(),
                body.costs(),
                body.preferredAssignee().name(),
                body.isPreferredFixed(),
                body.lastDoneAt(),
                body.isRepeated(),
                body.minRepeatInterval(),
                body.maxRepeatInterval()
        );

        return Response.status(200).entity(body).build();
    }

    @Path("changePerson")
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response changePerson(@QueryParam("oldName") String oldName, PersonDTO body) {

        repository.changePerson(oldName, body.name(), body.availabilityAssignment());

        return Response.status(200).entity(body).build();
    }


    @Path("changeTask")
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response changeTask(@QueryParam("oldName") String oldName, TaskDTO body) {

        repository.changeTask(oldName,
                body.name(),
                body.costs(),
                body.preferredAssignee().name(),
                body.isPreferredFixed(),
                body.lastDoneAt(),
                body.isRepeated(),
                body.minRepeatInterval(),
                body.maxRepeatInterval()
        );

        return Response.status(200).entity(body).build();
    }

    @Path("/exportScheduleAsTxt")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public Response exportScheduleAsTxt(@QueryParam("createdAt") String createdAtString) {
        DateTime createdAt = DateTime.parse(createdAtString);

        ISchedule schedule = repository.getSchedule(createdAt);
        java.util.List<IPerson> personList = repository.getAllPeople();
        int score = scheduler.calculateScore(schedule, personList);

        String output = Exporter.scheduleToPrettyString(schedule, score);

        return Response.status(200).entity(output).build();
    }

    @Path("/exportScheduleAsPDF")
    @GET
    @Produces("application/pdf")
    public Response exportScheduleAsPDF(@QueryParam("createdAt") String createdAtString) {
        DateTime createdAt = DateTime.parse(createdAtString);

        ISchedule schedule = repository.getSchedule(createdAt);
        java.util.List<IPerson> personList = repository.getAllPeople();
        int score = scheduler.calculateScore(schedule, personList);

        byte[] output = Exporter.scheduleToPDF(schedule, score);

        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss.SSS");
        String formattedDate = schedule.createdAt().toString(formatter);

        Response.ResponseBuilder response = Response.ok(output);
        response.header("Content-Disposition", "attachment; filename=" + formattedDate + ".pdf");
        return response.build();
    }

//    public void fillDataBaseWithMockExamples(){
//        // This is to fill my InMemoryRepository to test some things
//        Map<Object, Object> availabilityAssignment = Map$.MODULE$.empty().updated(1,1);
//        Map<Object, Object> updatedAssignment = availabilityAssignment.updated(2, 8);
//        IPerson person = factory.createPerson("Person A", updatedAssignment);
//        IPerson person2 = factory.createPerson("Person B", availabilityAssignment);
//        repository.save(person);
//        repository.save(person2);
//
//        ITask task = factory.createTask("Task 1", 40, person, true, 20, true, 1, 5) ;
//        repository.save(task);
//
//        Map<ITask, IPerson> taskAssignment = HashMap$.MODULE$.<ITask, IPerson>empty().updated(task, person);
//        IWeek week = factory.createWeek(30, taskAssignment);
//
//        List<IWeek> weekList = List$.MODULE$.empty().$colon$colon(week);
//        DateTime date = DateTime.parse("2024-11-15");
//        ISchedule schedule  = factory.createSchedule(date, weekList);
//        repository.save(schedule);
//    }
}
