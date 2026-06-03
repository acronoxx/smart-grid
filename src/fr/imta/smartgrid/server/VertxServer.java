package fr.imta.smartgrid.server;

import fr.imta.smartgrid.server.handler.GridHandler;
import fr.imta.smartgrid.server.handler.MeasurementHandler;
import fr.imta.smartgrid.server.handler.PersonHandler;
import fr.imta.smartgrid.server.handler.SensorHandler;
import fr.imta.smartgrid.service.GridService;
import fr.imta.smartgrid.service.PersonService;
import fr.imta.smartgrid.service.SensorService;
import fr.imta.smartgrid.service.MeasurementService;

import java.util.Map;

import org.eclipse.persistence.logging.SessionLog;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Persistence;

import static org.eclipse.persistence.config.PersistenceUnitProperties.*;

/**
 * Main Vert.x HTTP server for the SmartGrid API.
 * Initializes the JPA database connection, creates services and handlers,
 * and registers all HTTP routes on port 8080.
 */
public class VertxServer {
    private Vertx vertx;
    private EntityManager db; // database object

    /**
     * Initializes the Vert.x instance and the JPA EntityManager.
     */
    public VertxServer() {
        this.vertx = Vertx.vertx();

        // setup database connexion
        Map<String, String> properties = Map.of(
            LOGGING_LEVEL, SessionLog.WARNING_LABEL // change to FINE_LABEL to get details on SQL query to database
        );

        var emf = Persistence.createEntityManagerFactory("smart-grid", properties);
        db = emf.createEntityManager();
    }

    /**
     * Configures the router, registers all routes, and starts the HTTP server on port 8080.
     */
    public void start() {
        Router router = Router.router(vertx);

        // add handlers for payload parsing and to allow swagger to send requests
        router.route().handler(BodyHandler.create());
        router.route().handler(CorsHandler.create().addOrigin("*").allowedMethod(HttpMethod.DELETE).allowedMethod(HttpMethod.PUT));

        // create services
        PersonService ps = new PersonService(db);
        SensorService ss = new SensorService(db);
        MeasurementService ms = new MeasurementService(db);
        GridService gs = new GridService(db,ss);

        // create handlers and registers routes
        GridHandler gh = new GridHandler(gs);
        router.put("/grid").handler(gh::create);
        router.get("/grids").handler(gh::getGrids);
        router.get("/grid/:id").handler(gh::getById);
        router.get("/grid/:id/production").handler(gh::getTotalProduction);
        router.get("/grid/:id/consumption").handler(gh::getTotalConsumption);
        router.post("/grid/:id").handler(gh::update);
        router.delete("/grid/:id").handler(gh::delete);

        PersonHandler ph = new PersonHandler(ps);
        router.put("/person").handler(ph::create);
        router.get("/persons").handler(ph::getPersons);
        router.get("/person/:id").handler(ph::getPerson);
        router.post("/person/:id").handler(ph::update);
        router.delete("/person/:id").handler(ph::delete);

        SensorHandler sh = new SensorHandler(ss);
        router.put("/sensor").handler(sh::create);
        router.get("/sensors").handler(sh::getSensors);
        router.get("/producers").handler(sh::getProducers);
        router.get("/consumers").handler(sh::getConsumers);
        router.get("/sensors/:kind").handler(sh::getByKind);
        router.get("/sensor/:id").handler(sh::getById);
        router.post("/ingress/solarpanel").handler(sh::receiveSolarPanelMeasurement);
        router.post("/ingress/windturbine").handler(sh::receiveWindTurbineMeasurement);
        router.post("/sensor/:id").handler(sh::update);
        router.delete("/sensor/:id").handler(sh::delete);

        MeasurementHandler mh = new MeasurementHandler(ms);
        router.put("/measurement").handler(mh::create);
        router.get("/measurements").handler(mh::getMeasurements);
        router.get("/measurement/:id").handler(mh::getById);
        router.get("/measurement/:id/values").handler(mh::getValues);
        router.post("/measurement/:id").handler(mh::update);
        router.delete("/measurement/:id").handler(mh::delete);
        
        // start the server
        vertx.createHttpServer().requestHandler(router).listen(8080)
            .onSuccess(e -> 
                System.out.println("Server is listening on localhost:" + e.actualPort())
            ).onFailure(e -> {
                System.out.println("Cannot start server, got error: " + e.getLocalizedMessage());
                System.exit(1);
            });
    }

    /**
     * Application entry point. Creates and starts the Vert.x server.
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        new VertxServer().start();
    }
}
