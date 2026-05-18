package fr.imta.smartgrid.server.handler;

import fr.imta.smartgrid.model.Grid;
import fr.imta.smartgrid.service.GridService;

import java.util.ArrayList;
import java.util.List;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import jakarta.persistence.EntityNotFoundException;

public class GridHandler {
    private GridService gridService;

    public GridHandler(GridService gridService) {
        this.gridService = gridService;
    }

    // CRUD Operations

    public void create(RoutingContext ctx){
        try{
            JsonObject input = ctx.body().asJsonObject();

            String name = getNameFromJSON(input);
            String description = getDescriptionFromJSON(input);
            List <Integer> personIds = getPersonIdsFromJSON(input);
            List <Integer> sensorIds = getSensorIdsFromJSON(input);
            Grid g = gridService.create(name, description, personIds, sensorIds);

            ctx.json(g.getId());
        } 
        catch (IllegalArgumentException e) {
            ctx.response().setStatusCode(500).end(e.getMessage());
        }
        catch (Exception e){
            ctx.response().setStatusCode(500).end("Error during creation");
        }
    }

    public void getGrids(RoutingContext ctx) {
        try{
            List<Grid> grids = gridService.findAll();

            List<Integer> gridIds = new ArrayList<>();
            for (Grid g : grids) {
                gridIds.add(g.getId());
            }

            ctx.json(gridIds);
        }
        catch (Exception e){
            ctx.response().setStatusCode(500).end("Error during grids retrieval");
        }
    }

    public void getById(RoutingContext ctx) {
        try {
            Integer id = getIdFromPath(ctx);
            Grid g = gridService.findById(id);
            ctx.json(g.toJSON());
        }
        catch (IllegalArgumentException | EntityNotFoundException e){
            ctx.response().setStatusCode(404).end(e.getMessage());
        }
        catch (Exception e){
            ctx.response().setStatusCode(500).end("Error during grid retrieval");
        }
    }

    public void update(RoutingContext ctx){
        try {
            Integer id = getIdFromPath(ctx);

            JsonObject input = ctx.body().asJsonObject();
            String name = getNameFromJSON(input);
            String description = getDescriptionFromJSON(input);
            List<Integer> personIds = getPersonIdsFromJSON(input);
            List<Integer> sensorIds = getSensorIdsFromJSON(input);
            Grid g = gridService.update(id, name, description, personIds, sensorIds);
            
            ctx.json(g.getId());
        }
        catch (IllegalArgumentException | EntityNotFoundException e) {
            ctx.response().setStatusCode(404).end(e.getMessage());
        }
        catch (Exception e){
            ctx.response().setStatusCode(500).end("Error during grid update");
        }
    }

    public void delete(RoutingContext ctx){
        try {
            Integer id = getIdFromPath(ctx);
            gridService.delete(id);
            ctx.response().setStatusCode(200).end();
        }
        catch (IllegalArgumentException | EntityNotFoundException e) {
            ctx.response().setStatusCode(404).end(e.getMessage());
        }
        catch (Exception e){
            ctx.response().setStatusCode(500).end("Error during grid deletion");
        }
    }

    // Analytic operations

    public void getTotalProduction(RoutingContext ctx) {
        try{
            Integer id = getIdFromPath(ctx);
            Double result = gridService.computeTotalProduction(id);
            ctx.json(result);
        }
        catch (Exception e){
            ctx.response().setStatusCode(500).end("Error retrieval of grid production");
        }
    }

    public void getTotalConsumption(RoutingContext ctx){
        try{
            Integer id = getIdFromPath(ctx);
            Double result = gridService.computeTotalConsumption(id);
            ctx.json(result);
        }
        catch (Exception e){
            ctx.response().setStatusCode(500).end("Error retrieval of grid consumption");
        }
    }

    // Utils

    private Integer getIdFromPath(RoutingContext ctx){
        return Integer.parseInt(ctx.pathParam("id"));
    }

    private String getNameFromJSON(JsonObject input){
        return input.getString("name");
    }

    private String getDescriptionFromJSON(JsonObject input){
        return input.getString("description");
    }

    private List<Integer> getPersonIdsFromJSON(JsonObject input){
        JsonArray persons = input.getJsonArray("users");
        List<Integer> personIds = new ArrayList<>();
        if (persons != null){
            for (int i = 0; i < persons.size(); i++) {
                Integer personId = persons.getInteger(i);
                personIds.add(personId);
            }
        }
        return personIds;
    }

    private List<Integer> getSensorIdsFromJSON(JsonObject input){
        JsonArray sensors = input.getJsonArray("sensors");
        List<Integer> sensorIds = new ArrayList<>();
        if (sensors != null){
            for (int i = 0; i < sensors.size(); i++) {
                Integer sensorId = sensors.getInteger(i);
                sensorIds.add(sensorId);
            }
        }
        return sensorIds;
    }
}
