package fr.imta.smartgrid.server.handler;

import fr.imta.smartgrid.model.Person;
import fr.imta.smartgrid.service.PersonService;

import java.util.ArrayList;
import java.util.List;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import jakarta.persistence.EntityNotFoundException;

/**
 * HTTP handler for CRUD operations on Person entities.
 * Delegates business logic to {@link PersonService}.
 * Handles errors and returns appropriate HTTP responses.
 */
public class PersonHandler {
    private PersonService personService;

    public PersonHandler(PersonService personService) {
        this.personService = personService;
    }

    // ===== CRUD Operations =====

    /**
     * Creates a new Person from the JSON data provided in the request body.
     * Returns 200 with the created Person ID, 500 on error.
     * @param ctx the Vert.x routing context containing the request data
     * @throws IllegalArgumentException if the JSON data is invalid or missing
     */
    public void create(RoutingContext ctx){
        try{
            JsonObject input = ctx.body().asJsonObject();
            String firstName = getFirstNameFromJSON(input);
            String lastName = getLastNameFromJSON(input);
            Integer gridId = getGridIdFromJSON(input);
            List<Integer> sensorIds = getSensorIdsFromJSON(input);
            Person p = personService.create(firstName, lastName, gridId, sensorIds);
            ctx.json(new JsonObject().put("id", p.getId()));
        } 
        catch (IllegalArgumentException e) {
            ctx.response().setStatusCode(500).end(e.getMessage());
        }
        catch (Exception e){
            ctx.response().setStatusCode(500).end("Error during person creation");
        }
    }

    /**
     * Retrieves the list of all Person IDs.
     * Returns 200 with the list of IDs, 500 on error.
     * @param ctx the Vert.x routing context
     */
    public void getPersons(RoutingContext ctx) {
        try{
            List<Person> persons = personService.findAll();
            List<Integer> personIds = new ArrayList<>();
            for (Person p : persons) {
                personIds.add(p.getId());
            }
            ctx.json(personIds);
        }
        catch (Exception e){
            ctx.response().setStatusCode(500).end("Error during persons retrieval");
        }
    }

    /**
     * Retrieves a Person by its ID.
     * Returns 200 with the Person as JSON, 404 if not found, 500 on error.
     * @param ctx the Vert.x routing context containing the path param "id"
     * @throws IllegalArgumentException if the ID is not a valid integer
     * @throws EntityNotFoundException if no Person matches the given ID
     */
    public void getPerson(RoutingContext ctx) {
        try {
            Integer id = getIdFromPath(ctx);
            Person p = personService.findById(id);
            ctx.json(p.toJSON());
        }
        catch (IllegalArgumentException | EntityNotFoundException e){
            ctx.response().setStatusCode(404).end(e.getMessage());
        }
        catch (Exception e){
            ctx.response().setStatusCode(500).end("Error during person retrieval");
        }
    }

    /**
     * Updates an existing Person from the JSON data provided in the request body.
     * Returns 200 with the updated Person as JSON, 404 if not found, 500 on error.
     * @param ctx the Vert.x routing context containing the path param "id" and request data
     * @throws IllegalArgumentException if the JSON data is invalid
     * @throws EntityNotFoundException if no Person matches the given ID
     */
    public void update(RoutingContext ctx){
        try {
            Integer id = getIdFromPath(ctx);
            JsonObject input = ctx.body().asJsonObject();
            String firstName = getFirstNameFromJSON(input);
            String lastName = getLastNameFromJSON(input);
            Integer gridId = getGridIdFromJSON(input);
            List<Integer> sensorIds = getSensorIdsFromJSON(input);
            Person p = personService.update(id, firstName, lastName, gridId, sensorIds);
            ctx.json(p.toJSON());
        }
        catch (IllegalArgumentException | EntityNotFoundException e) {
            ctx.response().setStatusCode(404).end(e.getMessage());
        }
        catch (Exception e){
            ctx.response().setStatusCode(500).end("Error during person update");
        }
    }

    /**
     * Deletes a Person by its ID.
     * Returns 200 on success, 404 if not found, 500 on error.
     * @param ctx the Vert.x routing context containing the path param "id"
     * @throws EntityNotFoundException if no Person matches the given ID
     */
    public void delete(RoutingContext ctx){
        try {
            Integer id = getIdFromPath(ctx);
            personService.delete(id);
            ctx.response().setStatusCode(200).end();
        }
        catch (IllegalArgumentException | EntityNotFoundException e) {
            ctx.response().setStatusCode(404).end(e.getMessage());
        }
        catch (Exception e){
            ctx.response().setStatusCode(500).end("Error during person deletion");
        }
    }

    // ===== Utils =====

    /**
     * Extracts the Person ID from the path param "id".
     * @param ctx the Vert.x routing context
     * @return the ID as an Integer
     */
    private Integer getIdFromPath(RoutingContext ctx){
        return Integer.parseInt(ctx.pathParam("id"));
    }

    /**
     * Extracts the first name from the request JSON.
     * @param input the request JsonObject
     * @return the first name as a String
     */
    private String getFirstNameFromJSON(JsonObject input){
        return input.getString("first_name");
    }

    /**
     * Extracts the last name from the request JSON.
     * @param input the request JsonObject
     * @return the last name as a String
     */
    private String getLastNameFromJSON(JsonObject input){
        return input.getString("last_name");
    }

    /**
     * Extracts the associated Grid ID from the request JSON.
     * @param input the request JsonObject
     * @return the Grid ID
     */
    private Integer getGridIdFromJSON(JsonObject input){
        return input.getInteger("grid");
    }

    /**
     * Extracts the list of owned Sensor IDs from the request JSON.
     * @param input the request JsonObject
     * @return the list of Sensor IDs
     */
    private List<Integer> getSensorIdsFromJSON(JsonObject input){
        JsonArray ownedSensors = input.getJsonArray("owned_sensors");
        List<Integer> sensorIds = new ArrayList<>();
        if (ownedSensors != null){
            for (int i = 0; i < ownedSensors.size(); i++) {
                Integer sensorId = ownedSensors.getInteger(i);
                sensorIds.add(sensorId);
            }
            return sensorIds;
        }
        else{
            return null;
        }
    }
}