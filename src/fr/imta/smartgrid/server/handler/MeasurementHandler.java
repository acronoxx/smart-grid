package fr.imta.smartgrid.server.handlers;

import fr.imta.smartgrid.model.Measurement;
import fr.imta.smartgrid.service.MeasurementService;
import fr.imta.smartgrid.model.DataPoint;

import java.util.ArrayList;
import java.util.List;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import jakarta.persistence.EntityNotFoundException;

public class MeasurementHandler {
    private MeasurementService measurementService;

    public MeasurementHandler(MeasurementService measurementService) {
        this.measurementService = measurementService;
    }

    // CRUD Operations

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
                .put ("sensor_id", m.getSensor() != null ? m.getSensor().getId() : null)
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

    // Utils

    private Integer getIdFromPath(RoutingContext ctx){
        return Integer.parseInt(ctx.pathParam("id"));
    }

    private Integer getSensorIdFromJSON(JsonObject input){
        return input.getInteger("sensor");
    }

    private String getNameFromJSON(JsonObject input){
        return input.getString("name");
    }

    private String getUnitFromJSON(JsonObject input){
        return input.getString("unit");
    }

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

    private Long getFromFromQuery(RoutingContext ctx){
        String from = ctx.queryParam("from").isEmpty() ? null : ctx.queryParam("from").get(0);
        return from != null ? Long.parseLong(from) : null;
    }

    private Long getToFromQuery(RoutingContext ctx){
        String to = ctx.queryParam("to").isEmpty() ? null : ctx.queryParam("to").get(0);
        return to != null ? Long.parseLong(to) : null;
    }
}