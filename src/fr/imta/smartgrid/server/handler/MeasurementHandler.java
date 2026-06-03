package fr.imta.smartgrid.server.handler;

import fr.imta.smartgrid.model.Measurement;
import fr.imta.smartgrid.service.MeasurementService;
import fr.imta.smartgrid.model.DataPoint;

import java.util.ArrayList;
import java.util.List;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import jakarta.persistence.EntityNotFoundException;

/**
 * HTTP handler for CRUD operations on Measurement entities.
 * Delegates business logic to {@link MeasurementService}.
 * Handles errors and returns appropriate HTTP responses.
 */
public class MeasurementHandler {
    private MeasurementService measurementService;

    public MeasurementHandler(MeasurementService measurementService) {
        this.measurementService = measurementService;
    }

    // ===== CRUD Operations =====

    /**
     * Creates a new Measurement from the JSON data provided in the request body.
     * Returns 200 with the created Measurement ID, 500 on error.
     * @param ctx the Vert.x routing context containing the request data
     * @throws IllegalArgumentException if the JSON data is invalid or missing
     */
    public void create(RoutingContext ctx){
        try{
            JsonObject input = ctx.body().asJsonObject();
            Integer sensorId = getSensorIdFromJSON(input);
            String name = getNameFromJSON(input);
            String unit = getUnitFromJSON(input);
            List<Integer> dataPointIds = getDataPointIdsFromJSON(input);
            Measurement m = measurementService.create(unit, name, sensorId, dataPointIds);
            ctx.json(new JsonObject().put("id", m.getId()));
        } 
        catch (IllegalArgumentException e) {
            ctx.response().setStatusCode(500).end(e.getMessage());
        }
        catch (Exception e){
            ctx.response().setStatusCode(500).end("Error during measurement creation");
        }
    }

    /**
     * Retrieves the list of all Measurement IDs.
     * Returns 200 with the list of IDs, 500 on error.
     * @param ctx the Vert.x routing context
     */
    public void getMeasurements(RoutingContext ctx) {
        try {
            List<Measurement> measurements = measurementService.findAll();
            List<Integer> measurementIds = new ArrayList<>();
            for (Measurement m : measurements) {
                measurementIds.add(m.getId());
            }
            ctx.json(measurementIds);
        }
        catch (Exception e){
            ctx.response().setStatusCode(500).end("Error during measurements retrieval");
        }
    }

    /**
     * Retrieves a Measurement by its ID.
     * Returns 200 with the Measurement as JSON, 404 if not found, 500 on error.
     * @param ctx the Vert.x routing context containing the path param "id"
     * @throws IllegalArgumentException if the ID is not a valid integer
     * @throws EntityNotFoundException if no Measurement matches the given ID
     */
    public void getById(RoutingContext ctx) {
        try {
            Integer id = getIdFromPath(ctx);
            Measurement m = measurementService.findById(id);
            ctx.json(m.toJSON());
        }
        catch (IllegalArgumentException | EntityNotFoundException e){
            ctx.response().setStatusCode(404).end(e.getMessage());
        }
        catch (Exception e){
            ctx.response().setStatusCode(500).end("Error during measurement retrieval");
        }
    }

    /**
     * Retrieves the DataPoints of a Measurement over an optional time range.
     * Returns 200 with the DataPoints as JSON, 404 if not found, 500 on error.
     * @param ctx the Vert.x routing context containing the path param "id" and optional query params "from" and "to" (timestamps)
     * @throws IllegalArgumentException if the ID is invalid
     * @throws EntityNotFoundException if no Measurement matches the given ID
     */
    public void getValues(RoutingContext ctx) {
        try {
            Integer id = getIdFromPath(ctx);
            Long from = getFromFromQuery(ctx);
            Long to = getToFromQuery(ctx);
            Measurement m = measurementService.findById(id);
            List<DataPoint> dataPoints = measurementService.findValues(id, from, to);
            JsonArray dataPointsJson = new JsonArray();
            for (DataPoint dp : dataPoints) {
                dataPointsJson.add(dp.toJSON());
            }
            JsonObject response = new JsonObject()
                .put("sensor_id", m.getSensor() != null ? m.getSensor().getId() : null)
                .put("measurement_id", id)
                .put("values", dataPointsJson);
            ctx.json(response);
        }
        catch (IllegalArgumentException | EntityNotFoundException e){
            ctx.response().setStatusCode(404).end(e.getMessage());
        }
        catch (Exception e){
            ctx.response().setStatusCode(500).end("Error during measurement values retrieval");
        }
    }

    /**
     * Updates an existing Measurement from the JSON data provided in the request body.
     * Returns 200 with the updated Measurement as JSON, 404 if not found, 500 on error.
     * @param ctx the Vert.x routing context containing the path param "id" and request data
     * @throws IllegalArgumentException if the JSON data is invalid
     * @throws EntityNotFoundException if no Measurement matches the given ID
     */
    public void update(RoutingContext ctx) {
        try {
            Integer id = getIdFromPath(ctx);
            JsonObject input = ctx.body().asJsonObject();
            String name = getNameFromJSON(input);
            String unit = getUnitFromJSON(input);
            Integer sensorId = getSensorIdFromJSON(input);
            List<Integer> dataPointIds = getDataPointIdsFromJSON(input);
            Measurement m = measurementService.update(id, unit, name, sensorId, dataPointIds);
            ctx.json(m.toJSON());
        }
        catch (IllegalArgumentException | EntityNotFoundException e){
            ctx.response().setStatusCode(404).end(e.getMessage());
        }
        catch (Exception e){
            ctx.response().setStatusCode(500).end("Error during measurement update");
        }
    }

    /**
     * Deletes a Measurement by its ID.
     * Returns 200 on success, 404 if not found, 500 on error.
     * @param ctx the Vert.x routing context containing the path param "id"
     * @throws EntityNotFoundException if no Measurement matches the given ID
     */
    public void delete(RoutingContext ctx) {
        try {
            Integer id = getIdFromPath(ctx);
            measurementService.delete(id);
            ctx.response().setStatusCode(200).end();
        }
        catch (IllegalArgumentException | EntityNotFoundException e){
            ctx.response().setStatusCode(404).end(e.getMessage());
        }
        catch (Exception e){
            ctx.response().setStatusCode(500).end("Error during measurement deletion");
        }
    }

    // ===== Utils =====

    /**
     * Extracts the Measurement ID from the path param "id".
     * @param ctx the Vert.x routing context
     * @return the ID as an Integer
     */
    private Integer getIdFromPath(RoutingContext ctx){
        return Integer.parseInt(ctx.pathParam("id"));
    }

    /**
     * Extracts the associated Sensor ID from the request JSON.
     * @param input the request JsonObject
     * @return the Sensor ID
     */
    private Integer getSensorIdFromJSON(JsonObject input){
        return input.getInteger("sensor");
    }

    /**
     * Extracts the Measurement name from the request JSON.
     * @param input the request JsonObject
     * @return the name as a String
     */
    private String getNameFromJSON(JsonObject input){
        return input.getString("name");
    }

    /**
     * Extracts the Measurement unit from the request JSON.
     * @param input the request JsonObject
     * @return the unit as a String
     */
    private String getUnitFromJSON(JsonObject input){
        return input.getString("unit");
    }

    /**
     * Extracts the list of DataPoint IDs from the request JSON.
     * @param input the request JsonObject
     * @return the list of DataPoint IDs
     */
    private List<Integer> getDataPointIdsFromJSON(JsonObject input){
        JsonArray dataPointsJson = input.getJsonArray("datapoints");
        List<Integer> dataPointIds = new ArrayList<>();
        if (dataPointsJson != null){
            for (int i = 0; i < dataPointsJson.size(); i++) {
                dataPointIds.add(dataPointsJson.getInteger(i));
            }
        }
        return dataPointIds;
    }

    /**
     * Extracts the start timestamp from the query param "from".
     * @param ctx the Vert.x routing context
     * @return the timestamp as a Long, or null if absent
     */
    private Long getFromFromQuery(RoutingContext ctx){
        String from = ctx.queryParam("from").isEmpty() ? null : ctx.queryParam("from").get(0);
        return from != null ? Long.parseLong(from) : null;
    }

    /**
     * Extracts the end timestamp from the query param "to".
     * @param ctx the Vert.x routing context
     * @return the timestamp as a Long, or null if absent
     */
    private Long getToFromQuery(RoutingContext ctx){
        String to = ctx.queryParam("to").isEmpty() ? null : ctx.queryParam("to").get(0);
        return to != null ? Long.parseLong(to) : null;
    }
}