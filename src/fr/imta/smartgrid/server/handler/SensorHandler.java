package fr.imta.smartgrid.server.handlers;

import fr.imta.smartgrid.model.Sensor;
import fr.imta.smartgrid.model.Producer;
import fr.imta.smartgrid.model.Consumer;
import fr.imta.smartgrid.service.SensorService;
import fr.imta.smartgrid.model.SensorType;
import fr.imta.smartgrid.model.SolarPanel;
import fr.imta.smartgrid.model.WindTurbine;
import fr.imta.smartgrid.model.EVCharger;

import java.util.ArrayList;
import java.util.List;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import jakarta.persistence.EntityNotFoundException;

public class SensorHandler {
    private SensorService sensorService;

    public SensorHandler(SensorService sensorService) {
        this.sensorService = sensorService;
    }

    // CRUD Operations

    public void create(RoutingContext ctx){
        try{
            JsonObject input = ctx.body().asJsonObject();

            String name = getNameFromJSON(input);
            String description = getDescriptionFromJSON(input);
            String kind = getKindFromJSON(input);
            Integer gridId = getGridIdFromJSON(input);
            List<Integer> ownerIds = getOwnerIdsFromJSON(input);
            List<Integer> measurementIds = getMeasurementIdsFromJSON(input);

            SensorType sensorType = switch (kind) {
                case "SolarPanel"  -> SensorType.SOLAR_PANEL;
                case "WindTurbine" -> SensorType.WIND_TURBINE;
                case "EVCharger"   -> SensorType.EV_CHARGER;
                default -> throw new IllegalArgumentException("Unknown Sensor Type");
            };

            Sensor s = switch (sensorType) {
            case SOLAR_PANEL -> {
                String powerSource = getPowerSourceFromJSON(input);
                Float efficiency   = getEfficiencyFromJSON(input);
                yield sensorService.createSolarPanel(name, description, gridId, ownerIds, measurementIds, powerSource, efficiency);
            }
            case WIND_TURBINE -> {
                String powerSource = getPowerSourceFromJSON(input);
                Double bladeLength = getBladeLengthFromJSON(input);
                Double height = getHeightFromJSON(input);
                yield sensorService.createWindTurbine(name, description, gridId, ownerIds, measurementIds, powerSource, bladeLength, height);
            }
            case EV_CHARGER -> {
                Double maxPower = getMaxPowerFromJSON(input);
                Integer maxAmp = getMaxAmpFromJSON(input);
                String type = getTypeFromJSON(input);
                Integer voltage = getVoltageFromJSON(input);
                yield sensorService.createEVCharger(name, description, gridId, ownerIds, measurementIds, maxPower, maxAmp, type, voltage);
            }
        };

        ctx.json(s.getId());
        } 
        catch (IllegalArgumentException e) {
            ctx.response().setStatusCode(500).end(e.getMessage());
        }
        catch (Exception e){
            ctx.response().setStatusCode(500).end("Error during sensor creation");
        }
    }

    public void getSensors(RoutingContext ctx) {
        try {
            List<Sensor> sensors = sensorService.findAll();

            List<Integer> sensorIds = new ArrayList<>();
            for (Sensor s : sensors) {
                sensorIds.add(s.getId());
            }

            ctx.json(sensorIds);
        }
        catch (Exception e){
            ctx.response().setStatusCode(500).end("Error during sensors retrieval");
        }
    }

    public void getProducers(RoutingContext ctx){
        try {
            List<Producer> producers = sensorService.findAllProducers();

            JsonArray producersJson = new JsonArray();
            for (Producer p : producers) {
                producersJson.add(p.toJSON());
            }

            ctx.json(producersJson);
        }
        catch (Exception e){
            ctx.response().setStatusCode(500).end("Error during producers retrieval");
        }
    }

    public void getConsumers(RoutingContext ctx){
        try {
            List<Consumer> consumers = sensorService.findAllConsumers();

            JsonArray consumersJson = new JsonArray();
            for (Consumer c : consumers) {
                consumersJson.add(c.toJSON());
            }

            ctx.json(consumersJson);
        }
        catch (Exception e){
            ctx.response().setStatusCode(500).end("Error during consumers retrieval");
        }
    }

    public void getByKind(RoutingContext ctx) {
        try {
            String kind = getKindFromPath(ctx);
            List<Sensor> sensors = sensorService.findByKind(kind);

            List<Integer> sensorIds = new ArrayList<>();
            for (Sensor s : sensors) {
                sensorIds.add(s.getId());
            }

            ctx.json(sensorIds);
        }
        catch (IllegalArgumentException e){
            ctx.response().setStatusCode(400).end(e.getMessage());
        }
        catch (Exception e){
            ctx.response().setStatusCode(500).end("Error during sensors retrieval");
        }
    }

    public void getById(RoutingContext ctx) {
        try {
            Integer id = getIdFromPath(ctx);
            Sensor s = sensorService.findById(id);
            ctx.json(s.toJSON());
        }
        catch (IllegalArgumentException | EntityNotFoundException e){
            ctx.response().setStatusCode(404).end(e.getMessage());
        }
        catch (Exception e){
            ctx.response().setStatusCode(500).end("Error during sensor retrieval");
        }
    }

    public void update(RoutingContext ctx) {
        try{
            Integer id = getIdFromPath(ctx);
            Sensor s = sensorService.findById(id);

            if (s instanceof SolarPanel) {
                ctx.json(updateSolarPanel(ctx, id).toJSON());
            }
            else if (s instanceof WindTurbine) {
                ctx.json(updateWindTurbine(ctx, id).toJSON());
            }
            else if (s instanceof EVCharger) {
                ctx.json(updateEVCharger(ctx, id).toJSON());
            }
            else {
                throw new IllegalArgumentException("Unknown sensor type");
            }
        }
        catch (IllegalArgumentException | EntityNotFoundException e){
            ctx.response().setStatusCode(404).end(e.getMessage());
        }
        catch (Exception e){
            ctx.response().setStatusCode(500).end("Error during sensor update");
        }
    }

    public void delete(RoutingContext ctx){
        try {
            Integer id = getIdFromPath(ctx);
            sensorService.delete(id);
            ctx.response().setStatusCode(200).end();
        }
        catch (IllegalArgumentException | EntityNotFoundException e) {
            ctx.response().setStatusCode(404).end(e.getMessage());
        }
        catch (Exception e){
            ctx.response().setStatusCode(500).end("Error during sensor deletion");
        }
    }

    // SolarPanel

    private SolarPanel updateSolarPanel(RoutingContext ctx, Integer id) {
        JsonObject input = ctx.body().asJsonObject();
        String name = getNameFromJSON(input);
        String description = getDescriptionFromJSON(input);
        Integer gridId = getGridIdFromJSON(input);
        List<Integer> ownerIds = getOwnerIdsFromJSON(input);
        List<Integer> measurementIds = getMeasurementIdsFromJSON(input);
        String powerSource = getPowerSourceFromJSON(input);
        Float efficiency = getEfficiencyFromJSON(input);

        return sensorService.updateSolarPanel(id, name, description, gridId, ownerIds, measurementIds, powerSource, efficiency);
    }

    public void receiveSolarPanelMeasurement(RoutingContext ctx){
        try {
            String body = ctx.body().asString();
            if (body != null) {
                String[] parts = body.split(":");

                Integer id = Integer.parseInt(parts[0]);
                Double temperature = Double.parseDouble(parts[1]);
                Double power = Double.parseDouble(parts[2]);
                Long timestamp = Long.parseLong(parts[3]);
                sensorService.receiveSolarPanelMeasurement(id, timestamp, "temperature", temperature);
                sensorService.receiveSolarPanelMeasurement(id, timestamp, "power", power);
            }

            ctx.json(new JsonObject().put("status", "success"));
        }
        catch (IllegalArgumentException | EntityNotFoundException e){
            ctx.response().setStatusCode(404).end(e.getMessage());
        }
        catch (Exception e){
            ctx.response().setStatusCode(500).end("Error during wind turbine measurement reception");
        }
    }

    // WindTurbine

    private WindTurbine updateWindTurbine(RoutingContext ctx, Integer id) {
        JsonObject input = ctx.body().asJsonObject();
        String name = getNameFromJSON(input);
        String description = getDescriptionFromJSON(input);
        Integer gridId = getGridIdFromJSON(input);
        List<Integer> ownerIds = getOwnerIdsFromJSON(input);
        List<Integer> measurementIds = getMeasurementIdsFromJSON(input);
        String powerSource = getPowerSourceFromJSON(input);
        Double bladeLength = getBladeLengthFromJSON(input);
        Double height = getHeightFromJSON(input);

        return sensorService.updateWindTurbine(id, name, description, gridId, ownerIds, measurementIds, powerSource, bladeLength, height);
    }

    public void receiveWindTurbineMeasurement(RoutingContext ctx){
        try {
            JsonObject input = ctx.body().asJsonObject();
            Integer id = input.getInteger("windturbine");
            Long timestamp = input.getLong("timestamp");

            JsonObject data = input.getJsonObject("data");
            if (data != null) {
                for (String name : data.fieldNames()) {
                    Double value = data.getDouble(name);
                    sensorService.receiveWindTurbineMeasurement(id, timestamp, name, value);
                }
            }

            ctx.json(new JsonObject().put("status", "success"));
        }
        catch (IllegalArgumentException | EntityNotFoundException e){
            ctx.response().setStatusCode(404).end(e.getMessage());
        }
        catch (Exception e){
            ctx.response().setStatusCode(500).end("Error during wind turbine measurement reception");
        }
    }

    // EVCharger

    private EVCharger updateEVCharger(RoutingContext ctx, Integer id) {
        JsonObject input = ctx.body().asJsonObject();
        String name = getNameFromJSON(input);
        String description = getDescriptionFromJSON(input);
        Integer gridId = getGridIdFromJSON(input);
        List<Integer> ownerIds = getOwnerIdsFromJSON(input);
        List<Integer> measurementIds = getMeasurementIdsFromJSON(input);
        Double maxPower = getMaxPowerFromJSON(input);
        Integer maxAmp = getMaxAmpFromJSON(input);
        String type = getTypeFromJSON(input);
        Integer voltage = getVoltageFromJSON(input);

        return sensorService.updateEVCharger(id, name, description, gridId, ownerIds, measurementIds, maxPower, maxAmp, type, voltage);
    }

    // Utils

    private Integer getIdFromPath(RoutingContext ctx){
        return Integer.parseInt(ctx.pathParam("id"));
    }

    private String getKindFromPath(RoutingContext ctx){
        return ctx.pathParam("kind");
    }

    private String getNameFromJSON(JsonObject input){
        return input.getString("name");
    }

    private String getDescriptionFromJSON(JsonObject input){
        return input.getString("description");
    }

    private String getKindFromJSON(JsonObject input){
        return input.getString("kind");
    }

    private Integer getGridIdFromJSON(JsonObject input){
        return input.getInteger("grid");
    }

    private List<Integer> getOwnerIdsFromJSON(JsonObject input){
        JsonArray persons = input.getJsonArray("owners");
        List<Integer> personIds = new ArrayList<>();
        if (persons != null){
            for (int i = 0; i < persons.size(); i++) {
                Integer personId = persons.getInteger(i);
                personIds.add(personId);
            }
        }
        return personIds;
    }

    private List<Integer> getMeasurementIdsFromJSON(JsonObject input){
        JsonArray measurements = input.getJsonArray("available_measurements");
        List<Integer> measurementIds = new ArrayList<>();
        if (measurements != null){
            for (int i = 0; i < measurements.size(); i++) {
                Integer measurementId = measurements.getInteger(i);
                measurementIds.add(measurementId);
            }
        }
        return measurementIds;
    }

    private String getPowerSourceFromJSON(JsonObject input){
        return input.getString("powerSource");
    }

    private Float getEfficiencyFromJSON(JsonObject input){
        return input.getFloat("efficiency");
    }

    private Double getBladeLengthFromJSON(JsonObject input){
        return input.getDouble("bladeLength");
    }

    private Double getHeightFromJSON(JsonObject input){
        return input.getDouble("height");
    }

    private Double getMaxPowerFromJSON(JsonObject input){
        return input.getDouble("maxPower");
    }

    private Integer getMaxAmpFromJSON(JsonObject input){
        return input.getInteger("maxAmp");
    }

    private Integer getVoltageFromJSON(JsonObject input){
        return input.getInteger("voltage");
    }

    private String getTypeFromJSON(JsonObject input){
        return input.getString("connectorType");
    }
}
