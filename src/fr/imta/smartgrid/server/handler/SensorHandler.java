package fr.imta.smartgrid.server.handler;

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

/**
 * HTTP handler for CRUD operations on Sensor entities (SolarPanel, WindTurbine, EVCharger).
 * Delegates business logic to {@link SensorService}.
 * Handles errors and returns appropriate HTTP responses.
 */
public class SensorHandler {
    private SensorService sensorService;

    public SensorHandler(SensorService sensorService) {
        this.sensorService = sensorService;
    }

    // ===== CRUD Operations =====

    /**
     * Creates a new Sensor from the JSON data provided in the request body.
     * The Sensor type (SolarPanel, WindTurbine, EVCharger) is determined by the "kind" field.
     * Returns 200 with the created Sensor ID, 500 on error.
     * @param ctx the Vert.x routing context containing the request data
     * @throws IllegalArgumentException if the Sensor type is unknown or data is invalid
     */
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
                    Float efficiency = getEfficiencyFromJSON(input);
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

            ctx.json(new JsonObject().put("id", s.getId()));
        }
        catch (IllegalArgumentException e) {
            ctx.response().setStatusCode(500).end(e.getMessage());
        }
        catch (Exception e){
            ctx.response().setStatusCode(500).end("Error during sensor creation");
        }
    }

    /**
     * Retrieves the list of all Sensor IDs.
     * Returns 200 with the list of IDs, 500 on error.
     * @param ctx the Vert.x routing context
     */
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

    /**
     * Retrieves the list of all Producers as JSON.
     * Returns 200 with the list of Producers, 500 on error.
     * @param ctx the Vert.x routing context
     */
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

    /**
     * Retrieves the list of all Consumers as JSON.
     * Returns 200 with the list of Consumers, 500 on error.
     * @param ctx the Vert.x routing context
     */
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

    /**
     * Retrieves the list of Sensor IDs of a given type.
     * Returns 200 with the list of IDs, 400 if the type is invalid, 500 on error.
     * @param ctx the Vert.x routing context containing the path param "kind"
     * @throws IllegalArgumentException if the Sensor type is unknown
     */
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

    /**
     * Retrieves a Sensor by its ID.
     * Returns 200 with the Sensor as JSON, 404 if not found, 500 on error.
     * @param ctx the Vert.x routing context containing the path param "id"
     * @throws IllegalArgumentException if the ID is not a valid integer
     * @throws EntityNotFoundException if no Sensor matches the given ID
     */
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

    /**
     * Updates an existing Sensor according to its type (SolarPanel, WindTurbine, EVCharger).
     * Returns 200 with the updated Sensor as JSON, 404 if not found, 500 on error.
     * @param ctx the Vert.x routing context containing the path param "id" and request data
     * @throws IllegalArgumentException if the Sensor type is unknown
     * @throws EntityNotFoundException if no Sensor matches the given ID
     */
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
        catch (IllegalArgumentException e){
            ctx.response().setStatusCode(400).end(e.getMessage());
        }
        catch (EntityNotFoundException e){
            ctx.response().setStatusCode(404).end(e.getMessage());
        }
        catch (Exception e){
            ctx.response().setStatusCode(500).end("Error during sensor update");
        }
    }

    /**
     * Deletes a Sensor by its ID.
     * Returns 200 on success, 404 if not found, 500 on error.
     * @param ctx the Vert.x routing context containing the path param "id"
     * @throws EntityNotFoundException if no Sensor matches the given ID
     */
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

    // ===== SolarPanel =====

    /**
     * Updates an existing SolarPanel from the JSON data provided in the request body.
     * @param ctx the Vert.x routing context containing the request data
     * @param id the ID of the SolarPanel to update
     * @return the updated SolarPanel
     */
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

    /**
     * Receives a measurement from a SolarPanel in the format "id:temperature:power:timestamp".
     * Returns 200 with a success status, 404 if not found, 500 on error.
     * @param ctx the Vert.x routing context containing the request body
     * @throws IllegalArgumentException if the body format is invalid
     * @throws EntityNotFoundException if no SolarPanel matches the given ID
     */
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
            ctx.response().setStatusCode(500).end("Error during solar panel measurement reception");
        }
    }

    // ===== WindTurbine =====

    /**
     * Updates an existing WindTurbine from the JSON data provided in the request body.
     * @param ctx the Vert.x routing context containing the request data
     * @param id the ID of the WindTurbine to update
     * @return the updated WindTurbine
     */
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

    /**
     * Receives a measurement from a WindTurbine as JSON.
     * Returns 200 with a success status, 404 if not found, 500 on error.
     * @param ctx the Vert.x routing context containing the JSON request body
     * @throws IllegalArgumentException if the data is invalid
     * @throws EntityNotFoundException if no WindTurbine matches the given ID
     */
    public void receiveWindTurbineMeasurement(RoutingContext ctx){
        try {
            JsonObject input = ctx.body().asJsonObject();
            Integer id = input.getInteger("windturbine");
            Long timestamp = input.getLong("timestamp");
            JsonObject values = input.getJsonObject("data");
            if (values!= null) {
                for (String name : values.fieldNames()) {
                    Double value = values.getDouble(name);
                    sensorService.receiveWindTurbineMeasurement(id, timestamp, name, value);
                }
            }
            ctx.response().setStatusCode(200).end("success");
        }
        catch (IllegalArgumentException | EntityNotFoundException e){
            ctx.response().setStatusCode(404).end(e.getMessage());
        }
        catch (Exception e){
            ctx.response().setStatusCode(500).end("Error during wind turbine measurement reception");
        }
    }

    // ===== EVCharger =====

    /**
     * Updates an existing EVCharger from the JSON data provided in the request body.
     * @param ctx the Vert.x routing context containing the request data
     * @param id the ID of the EVCharger to update
     * @return the updated EVCharger
     */
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
     * Extracts the Sensor type from the path param "kind".
     * @param ctx the Vert.x routing context
     * @return the type as a String
     */
    private String getKindFromPath(RoutingContext ctx){
        return ctx.pathParam("kind");
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
     * Extracts the Sensor type from the request JSON.
     * @param input the request JsonObject
     * @return the type as a String
     */
    private String getKindFromJSON(JsonObject input){
        return input.getString("kind");
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
     * Extracts the list of owner Person IDs from the request JSON.
     * @param input the request JsonObject
     * @return the list of Person IDs
     */
    private List<Integer> getOwnerIdsFromJSON(JsonObject input){
        JsonArray persons = input.getJsonArray("owners");
        List<Integer> personIds = new ArrayList<>();
        if (persons != null){
            for (int i = 0; i < persons.size(); i++) {
                Integer personId = persons.getInteger(i);
                personIds.add(personId);
            }
            return personIds;
        }
        return null;
    }

    /**
     * Extracts the list of available Measurement IDs from the request JSON.
     * @param input the request JsonObject
     * @return the list of Measurement IDs
     */
    private List<Integer> getMeasurementIdsFromJSON(JsonObject input){
        JsonArray measurements = input.getJsonArray("available_measurements");
        List<Integer> measurementIds = new ArrayList<>();
        if (measurements != null){
            for (int i = 0; i < measurements.size(); i++) {
                Integer measurementId = measurements.getInteger(i);
                measurementIds.add(measurementId);
            }
            return measurementIds;
        }
        return null;
    }

    /**
     * Extracts the power source from the request JSON.
     * @param input the request JsonObject
     * @return the power source as a String
     */
    private String getPowerSourceFromJSON(JsonObject input){
        return input.getString("power_source");
    }

    /**
     * Extracts the efficiency from the request JSON.
     * @param input the request JsonObject
     * @return the efficiency as a Float
     */
    private Float getEfficiencyFromJSON(JsonObject input){
        return input.getFloat("efficiency");
    }

    /**
     * Extracts the blade length from the request JSON.
     * @param input the request JsonObject
     * @return the blade length as a Double
     */
    private Double getBladeLengthFromJSON(JsonObject input){
        return input.getDouble("blade_length");
    }

    /**
     * Extracts the height from the request JSON.
     * @param input the request JsonObject
     * @return the height as a Double
     */
    private Double getHeightFromJSON(JsonObject input){
        return input.getDouble("height");
    }

    /**
     * Extracts the maximum power from the request JSON.
     * @param input the request JsonObject
     * @return the maximum power as a Double
     */
    private Double getMaxPowerFromJSON(JsonObject input){
        return input.getDouble("max_power");
    }

    /**
     * Extracts the maximum current from the request JSON.
     * @param input the request JsonObject
     * @return the maximum current as an Integer
     */
    private Integer getMaxAmpFromJSON(JsonObject input){
        return input.getInteger("maxAmp");
    }

    /**
     * Extracts the voltage from the request JSON.
     * @param input the request JsonObject
     * @return the voltage as an Integer
     */
    private Integer getVoltageFromJSON(JsonObject input){
        return input.getInteger("voltage");
    }

    /**
     * Extracts the connector type from the request JSON.
     * @param input the request JsonObject
     * @return the connector type as a String
     */
    private String getTypeFromJSON(JsonObject input){
        return input.getString("type");
    }
}