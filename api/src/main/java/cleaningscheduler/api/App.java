package cleaningscheduler.api;

import cleaningscheduler.api.controllers.ScheduleController;
import cleaningscheduler.api.utility.CustomObjectMapperProvider;
import cleaningscheduler.domain.IScheduleFactory;
import cleaningscheduler.domain.SchedulerFactory;
import cleaningscheduler.domain.scheduler.IScheduler;
import cleaningscheduler.domain.scheduler.MaxCostsPerPersonScheduler;
import cleaningscheduler.domain.scheduler.MaxTaskPersonVariableScheduler;
import cleaningscheduler.persistence.ISchedulerRepository;
import cleaningscheduler.persistence.VanillaSQL.SQLRepository;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;

// This class has been based on the Mancala API we were provided for a Sogyo Mastercourse Assignment
public class App {
    private static final int PORT = 8080;

    public static void main(String[] args) throws Exception {
        Server server = createServer();
        server.start();

        System.out.println("Started server.");
        System.out.format("Listening on http://localhost:%d/%n", PORT);
        System.out.println("Press CTRL+C to exit.");

        server.join();
    }

    private static Server createServer() {
        var server = new Server(App.PORT);

        ServletContextHandler context = createStatefulContext(server);
        registerServlets(context);

        return server;
    }


    private static ServletContextHandler createStatefulContext(Server server) {
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        server.setHandler(context);

        return context;
    }

    private static void registerServlets(ServletContextHandler context) {
        // Use the Jersey framework to translate the methods in the
        // ScheduleController class to server endpoints (servlets).
        // For example, the start method will become an endpoint at
        // http://localhost:8080/cleaning-scheduler/api/start
        context.addServlet(new ServletHolder(new ServletContainer(createResources())), "/*");
    }

    private static ResourceConfig createResources() {
        // Create the dependencies we want to inject
        IScheduleFactory factory = new SchedulerFactory();
//        ISchedulerRepository repository = new InMemoryRepository();
        ISchedulerRepository repository = new SQLRepository();
//        IScorer scorer = new MaxCostsPerPersonScorer();
//        IScheduler scheduler = new MaxTaskPersonVariableScheduler();
        IScheduler scheduler = new MaxCostsPerPersonScheduler();
        // Create the Controller and inject the dependencies
        ScheduleController scheduleController = new ScheduleController(factory, repository, scheduler);
        // Register our Controller
        return new ResourceConfig().register(scheduleController)
                .register(CustomObjectMapperProvider.class);
        // Note: Jetty (and most other frameworks) can also handle Dependency
        // Injection and registering controllers automatically.
    }
}
