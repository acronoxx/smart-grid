package fr.imta.smartgrid.server.handler;

import fr.imta.smartgrid.model.Person;
import fr.imta.smartgrid.service.PersonService;

import java.util.ArrayList;
import java.util.List;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import jakarta.persistence.EntityNotFoundException;

public class PersonHandler {
    private PersonService personService;

    public PersonHandler(PersonService personService) {
        this.personService = personService;
    }

    // CRUD Operations

    public void create(RoutingContext ctx){
        try{
            JsonObject input = ctx.body().asJsonObject();

            String firstName = getFirstNameFromJSON(input);
            String lastName = getLastNameFromJSON(input);
            Integer gridId = getGridIdFromJSON(input);
            List<Integer> sensorIds = getSensorIdsFromJSON(input);
            Person p = personService.create(firstName,lastName,gridId,sensorIds);

            ctx.json(new JsonObject().put("id", p.getId()));
        } 
        catch (IllegalArgumentException e) {
            ctx.response().setStatusCode(500).end(e.getMessage());
        }
        catch (Exception e){
            ctx.response().setStatusCode(500).end("Error during person creation");
        }
    }

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

    // UTILS

    private Integer getIdFromPath(RoutingContext ctx){
        return Integer.parseInt(ctx.pathParam("id"));
    }

    private String getFirstNameFromJSON(JsonObject input){
        return input.getString("first_name");
    }

    private String getLastNameFromJSON(JsonObject input){
        return input.getString("last_name");
    }

    private Integer getGridIdFromJSON(JsonObject input){
        return input.getInteger("grid");
    }

    private List<Integer> getSensorIdsFromJSON(JsonObject input){
        JsonArray ownedSensors = input.getJsonArray("owned_sensors");
        List<Integer> sensorIds = new ArrayList<>();
        if (ownedSensors != null){
            for (int i = 0; i < ownedSensors.size(); i++) {
                Integer sensorId = ownedSensors.getInteger(i);
                sensorIds.add(sensorId);
            }
        }
        return sensorIds;
    }
}
