package fr.imta.smartgrid.server.handler;

import fr.imta.smartgrid.model.Grid;
import fr.imta.smartgrid.service.GridService;

import java.util.ArrayList;
import java.util.List;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import jakarta.persistence.EntityNotFoundException;

/**
 * HTTP handler for CRUD and analytics operations on Grid entities.
 * Delegates business logic to {@link GridService}.
 * Handles errors and returns appropriate HTTP responses.
 */
public class GridHandler {
    private GridService gridService;

    public GridHandler(GridService gridService) {
        this.gridService = gridService;
    }

    // ===== CRUD Operations =====

    /**
     * Creates a new Grid from the JSON data provided in the request body.
     * Returns 200 with the created Grid ID, 500 on error.
     * @param ctx the Vert.x routing context containing the request data
     * @throws IllegalArgumentException if the JSON data is invalid or missing
     */
    public void create(RoutingContext ctx){
        try{
            JsonObject input = ctx.body().asJsonObject();
            String name = getNameFromJSON(input);
            String description = getDescriptionFromJSON(input);
            List<Integer> personIds = getPersonIdsFromJSON(input);
            List<Integer> sensorIds = getSensorIdsFromJSON(input);
            Grid g = gridService.create(name, description, personIds, sensorIds);
            ctx.json(new JsonObject().put("id", g.getId()));
        } 
        catch (IllegalArgumentException e) {
            ctx.response().setStatusCode(500).end(e.getMessage());
        }
        catch (Exception e){
            ctx.response().setStatusCode(500).end("Error during creation");
        }
    }

    /**
     * Retrieves the list of all Grid IDs.
     * Returns 200 with the list of IDs, 500 on error.
     * @param ctx the Vert.x routing context
     */
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

    /**
     * Retrieves a Grid by its ID.
     * Returns 200 with the Grid as JSON, 404 if not found, 500 on error.
     * @param ctx the Vert.x routing context containing the path param "id"
     * @throws IllegalArgumentException if the ID is not a valid integer
     * @throws EntityNotFoundException if no Grid matches the given ID
     */
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

    /**
     * Updates an existing Grid from the JSON data provided in the request body.
     * Returns 200 with the updated Grid ID, 404 if not found, 500 on error.
     * @param ctx the Vert.x routing context containing the path param "id" and request data
     * @throws IllegalArgumentException if the JSON data is invalid
     * @throws EntityNotFoundException if no Grid matches the given ID
     */
    public void update(RoutingContext ctx){
        try {
            Integer id = getIdFromPath(ctx);
            JsonObject input = ctx.body().asJsonObject();
            String name = getNameFromJSON(input);
            String description = getDescriptionFromJSON(input);
            List<Integer> personIds = getPersonIdsFromJSON(input);
            List<Integer> sensorIds = getSensorIdsFromJSON(input);
            Grid g = gridService.update(id, name, description, personIds, sensorIds);
            ctx.json(g.toJSON());
        }
        catch (IllegalArgumentException | EntityNotFoundException e) {
            ctx.response().setStatusCode(404).end(e.getMessage());
        }
        catch (Exception e){
            ctx.response().setStatusCode(500).end("Error during grid update");
        }
    }

    /**
     * Deletes a Grid by its ID.
     * Returns 200 on success, 404 if not found, 500 on error.
     * @param ctx the Vert.x routing context containing the path param "id"
     * @throws EntityNotFoundException if no Grid matches the given ID
     */
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

    // ===== Analytics =====

    /**
     * Computes the total production of the Grid identified by the given ID.
     * Returns 200 with the value as a Double, 500 on error.
     * @param ctx the Vert.x routing context containing the path param "id"
     */
    public void getTotalProduction(RoutingContext ctx) {
        try{
            Integer id = getIdFromPath(ctx);
            Double result = gridService.computeTotalProduction(id);
            ctx.json(result);
        }
        catch (EntityNotFoundException e){
            ctx.response().setStatusCode(404).end("Grid not found");
        }
        catch (Exception e){
            ctx.response().setStatusCode(500).end("Error retrieval of grid production");
        }
    }

    /**
     * Computes the total consumption of the Grid identified by the given ID.
     * Returns 200 with the value as a Double, 500 on error.
     * @param ctx the Vert.x routing context containing the path param "id"
     */
    public void getTotalConsumption(RoutingContext ctx){
        try{
            Integer id = getIdFromPath(ctx);
            Double result = gridService.computeTotalConsumption(id);
            ctx.json(result);
        }
        catch (EntityNotFoundException e){
            ctx.response().setStatusCode(404).end("Grid not found");
        }
        catch (Exception e){
            ctx.response().setStatusCode(500).end("Error retrieval of grid consumption");
        }
    }

    // ===== Utils =====

    /**
     * Extracts the Grid ID from the path param "id".
     * @param ctx the Vert.x routing context
     * @return the ID as an Integer
     * @throws IllegalArgumentException if the ID is not a valid integer
     */
    private Integer getIdFromPath(RoutingContext ctx){
        try{
            Integer id = Integer.parseInt(ctx.pathParam("id"));
            return id;
        }
        catch (NumberFormatException e){
            throw new IllegalArgumentException("Invalid ID format");
        }
    }


    /**
     * Extracts the name from the request JSON.
     * @param input the request JsonObject
     * @return the name as a String
     */
    private String getNameFromJSON(JsonObject input){
        return input.getString("name");
    }

    /**
     * Extracts the description from the request JSON.
     * @param input the request JsonObject
     * @return the description as a String
     */
    private String getDescriptionFromJSON(JsonObject input){
        return input.getString("description");
    }

    /**
     * Extracts the list of associated Person IDs from the request JSON.
     * @param input the request JsonObject
     * @return the list of Person IDs
     */
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

    /**
     * Extracts the list of associated Sensor IDs from the request JSON.
     * @param input the request JsonObject
     * @return the list of Sensor IDs
     */
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