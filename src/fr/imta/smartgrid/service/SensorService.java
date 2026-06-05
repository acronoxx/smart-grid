package fr.imta.smartgrid.service;

import fr.imta.smartgrid.model.Sensor;
import fr.imta.smartgrid.model.Producer;
import fr.imta.smartgrid.model.Consumer;
import fr.imta.smartgrid.model.SensorType;
import fr.imta.smartgrid.model.SolarPanel;
import fr.imta.smartgrid.model.WindTurbine;
import fr.imta.smartgrid.model.EVCharger;
import fr.imta.smartgrid.model.Grid;
import fr.imta.smartgrid.model.Person;
import fr.imta.smartgrid.model.Measurement;
import fr.imta.smartgrid.model.DataPoint;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;

/**
 * Business service for CRUD operations on Sensor entities (SolarPanel, WindTurbine, EVCharger).
 * Manages JPA transactions, measurement reception, and total production tracking.
 */
public class SensorService {
    private EntityManager db;

    public SensorService(EntityManager db) {
        this.db = db;
    }

    // ===== CRUD Operations =====

    /**
     * Retrieves all Sensors from the database.
     * @return the list of all Sensors
     */
    public List<Sensor> findAll(){
        return db.createQuery("SELECT s FROM Sensor s", Sensor.class).getResultList();
    }

    /**
     * Retrieves all Producer Sensors from the database.
     * @return the list of all Producers
     */
    public List<Producer> findAllProducers(){
        return db.createQuery("SELECT p FROM Producer p", Producer.class).getResultList();
    }

    /**
     * Retrieves all Consumer Sensors from the database.
     * @return the list of all Consumers
     */
    public List<Consumer> findAllConsumers(){
        return db.createQuery("SELECT c FROM Consumer c", Consumer.class).getResultList();
    }

    /**
     * Retrieves all Sensors of a given type.
     * @param kind the sensor type as a string ("SolarPanel", "WindTurbine", "EVCharger")
     * @return the list of matching Sensors
     * @throws IllegalArgumentException if the kind is unknown
     */
    public List<Sensor> findByKind(String kind){
        try {
            SensorType sensorType = switch (kind) {
                case "SolarPanel"  -> SensorType.SOLAR_PANEL;
                case "WindTurbine" -> SensorType.WIND_TURBINE;
                case "EVCharger"   -> SensorType.EV_CHARGER;
                default -> throw new IllegalArgumentException("Unknown Sensor Type");
            };
            return switch (sensorType) {
                case SOLAR_PANEL  -> new ArrayList<>(findAllSolarPanels());
                case WIND_TURBINE -> new ArrayList<>(findAllWindTurbines());
                case EV_CHARGER   -> new ArrayList<>(findAllEVChargers());
            };
        } 
        catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown Sensor Type");
        }
        catch (Exception e) {
            throw e;
        }
    }

    /**
     * Retrieves a Sensor by its ID.
     * @param id the ID of the Sensor
     * @return the matching Sensor
     * @throws IllegalArgumentException if the ID is null or no Sensor matches
     */
    public Sensor findById(Integer id){
        idValidation(id);
        Sensor s = db.find(Sensor.class, id);
        if (s == null) throw new EntityNotFoundException("Sensor not found");
        return s;
    }

    /**
     * Deletes a Sensor by its ID.
     * @param id the ID of the Sensor to delete
     * @throws IllegalArgumentException if no Sensor matches the given ID
     */
    public void delete(Integer id){
        try {
            db.getTransaction().begin();
            Sensor s = findById(id);
            db.remove(s);
            db.getTransaction().commit();
        } 
        catch (Exception e) {
            if (db.getTransaction().isActive()) {
                db.getTransaction().rollback();
            }
            throw e;
        }
    }

    // ===== SolarPanel =====

    /**
     * Creates a new SolarPanel and persists it to the database.
     * @param name the name of the SolarPanel
     * @param description the description
     * @param gridId the ID of the associated Grid
     * @param ownerIds the list of owner Person IDs
     * @param measurementIds the list of Measurement IDs
     * @param powerSource the power source type
     * @param efficiency the panel efficiency
     * @return the created SolarPanel
     * @throws IllegalArgumentException if any mandatory field is null
     * @throws EntityNotFoundException if the Grid, a Person, or a Measurement does not exist
     */
    public SolarPanel createSolarPanel(String name, String description, Integer gridId, List<Integer> ownerIds, List<Integer> measurementIds, String powerSource, Float efficiency){
        validateSolarPanelCreation(name, description, gridId, ownerIds, measurementIds, powerSource, efficiency);
        try {
            db.getTransaction().begin();
            SolarPanel sp = new SolarPanel();
            applySolarPanelFields(sp, name, description, gridId, ownerIds, measurementIds, powerSource, efficiency);
            db.persist(sp);
            db.getTransaction().commit();
            return sp;
        } 
        catch (Exception e) {
            if (db.getTransaction().isActive()) {
                db.getTransaction().rollback();
            }
            throw e;
        }
    }

    /**
     * Retrieves all SolarPanels from the database.
     * @return the list of all SolarPanels
     */
    public List<SolarPanel> findAllSolarPanels(){
        return db.createQuery("SELECT sp FROM SolarPanel sp", SolarPanel.class).getResultList();
    }

    /**
     * Retrieves a SolarPanel by its ID.
     * @param id the ID of the SolarPanel
     * @return the matching SolarPanel
     * @throws IllegalArgumentException if the ID is null or no SolarPanel matches
     */
    public SolarPanel findSolarPanelById(Integer id){
        idValidation(id);
        SolarPanel sp = db.find(SolarPanel.class, id);
        if (sp == null) throw new IllegalArgumentException("SolarPanel not found");
        return sp;
    }

    /**
     * Updates an existing SolarPanel in the database.
     * @param id the ID of the SolarPanel to update
     * @param name the new name
     * @param description the new description
     * @param gridId the new Grid ID
     * @param ownerIds the new list of owner Person IDs
     * @param measurementIds the new list of Measurement IDs
     * @param powerSource the new power source
     * @param efficiency the new efficiency
     * @return the updated SolarPanel
     * @throws IllegalArgumentException if no SolarPanel matches the given ID
     */
    public SolarPanel updateSolarPanel(Integer id, String name, String description, Integer gridId, List<Integer> ownerIds, List<Integer> measurementIds, String powerSource, Float efficiency){
        try{
            db.getTransaction().begin();
            SolarPanel s = findSolarPanelById(id);
            applySolarPanelFields(s, name, description, gridId, ownerIds, measurementIds, powerSource, efficiency);
            db.getTransaction().commit();
            return s;
        }
        catch (Exception e) {
            if (db.getTransaction().isActive()) {
                db.getTransaction().rollback();
            }
            throw e;
        }
    }

    /**
     * Receives a measurement from a SolarPanel and persists the DataPoint.
     * Updates the total production map if the measurement name is "power".
     * @param id the ID of the SolarPanel
     * @param timestamp the timestamp of the measurement
     * @param name the measurement name ("power" or "temperature")
     * @param value the measured value
     * @throws IllegalArgumentException if no SolarPanel matches the given ID
     */
    public void receiveSolarPanelMeasurement(Integer id, Long timestamp, String name, Double value){
        try {
            db.getTransaction().begin();
            receiveMeasurement(id, timestamp, name, value);
            db.getTransaction().commit();
        }
        catch (Exception e) {
            if (db.getTransaction().isActive()) {
                db.getTransaction().rollback();
            }
            throw e;
        }
    }

    // ===== WindTurbine =====

    /**
     * Creates a new WindTurbine and persists it to the database.
     * @param name the name of the WindTurbine
     * @param description the description
     * @param gridId the ID of the associated Grid
     * @param ownerIds the list of owner Person IDs
     * @param measurementIds the list of Measurement IDs
     * @param powerSource the power source type
     * @param bladeLength the blade length in meters
     * @param height the height in meters
     * @return the created WindTurbine
     * @throws IllegalArgumentException if any mandatory field is null
     * @throws EntityNotFoundException if the Grid, a Person, or a Measurement does not exist
     */
    public WindTurbine createWindTurbine(String name, String description, Integer gridId, List<Integer> ownerIds, List<Integer> measurementIds, String powerSource, Double bladeLength, Double height){
        validateWindTurbineCreation(name, description, gridId, ownerIds, measurementIds, powerSource, bladeLength, height);
        try {
            db.getTransaction().begin();
            WindTurbine wt = new WindTurbine();
            applyWindTurbineFields(wt, name, description, gridId, ownerIds, measurementIds, powerSource, bladeLength, height);
            db.persist(wt);
            db.getTransaction().commit();
            return wt;
        } 
        catch (Exception e) {
            if (db.getTransaction().isActive()) {
                db.getTransaction().rollback();
            }
            throw e;
        }
    }

    /**
     * Retrieves all WindTurbines from the database.
     * @return the list of all WindTurbines
     */
    public List<WindTurbine> findAllWindTurbines(){
        return db.createQuery("SELECT w FROM WindTurbine w", WindTurbine.class).getResultList();
    }

    /**
     * Retrieves a WindTurbine by its ID.
     * @param id the ID of the WindTurbine
     * @return the matching WindTurbine
     * @throws IllegalArgumentException if the ID is null or no WindTurbine matches
     */
    public WindTurbine findWindTurbineById(Integer id){
        idValidation(id);
        WindTurbine wt = db.find(WindTurbine.class, id);
        if (wt == null) throw new IllegalArgumentException("WindTurbine not found");
        return wt;
    }

    /**
     * Updates an existing WindTurbine in the database.
     * @param id the ID of the WindTurbine to update
     * @param name the new name
     * @param description the new description
     * @param gridId the new Grid ID
     * @param ownerIds the new list of owner Person IDs
     * @param measurementIds the new list of Measurement IDs
     * @param powerSource the new power source
     * @param bladeLength the new blade length
     * @param height the new height
     * @return the updated WindTurbine
     * @throws IllegalArgumentException if no WindTurbine matches the given ID
     */
    public WindTurbine updateWindTurbine(Integer id, String name, String description, Integer gridId, List<Integer> ownerIds, List<Integer> measurementIds, String powerSource, Double bladeLength, Double height){
        try {
            db.getTransaction().begin();
            WindTurbine wt = findWindTurbineById(id);
            applyWindTurbineFields(wt, name, description, gridId, ownerIds, measurementIds, powerSource, bladeLength, height);
            db.getTransaction().commit();
            return wt;
        }
        catch (Exception e) {
            if (db.getTransaction().isActive()) {
                db.getTransaction().rollback();
            }
            throw e;
        }
    }

    /**
     * Receives a measurement from a WindTurbine and persists the DataPoint.
     * Updates the total production map if the measurement name is "power".
     * @param id the ID of the WindTurbine
     * @param timestamp the timestamp of the measurement
     * @param name the measurement name ("power" or "speed")
     * @param value the measured value
     * @throws IllegalArgumentException if no WindTurbine matches the given ID
     */
    public void receiveWindTurbineMeasurement(Integer id, Long timestamp, String name, Double value){
        try {
            db.getTransaction().begin();
            receiveMeasurement(id, timestamp, name, value);
            db.getTransaction().commit();
        }
        catch (Exception e) {
            if (db.getTransaction().isActive()) {
                db.getTransaction().rollback();
            }
            throw e;
        }
    }

    // ===== EVCharger =====

    /**
     * Creates a new EVCharger and persists it to the database.
     * @param name the name of the EVCharger
     * @param description the description
     * @param gridId the ID of the associated Grid
     * @param ownerIds the list of owner Person IDs
     * @param measurementIds the list of Measurement IDs
     * @param maxPower the maximum power in kW
     * @param maxAmp the maximum current in amperes
     * @param connectorType the connector type
     * @param voltage the voltage in volts
     * @return the created EVCharger
     * @throws IllegalArgumentException if any mandatory field is null
     * @throws EntityNotFoundException if the Grid, a Person, or a Measurement does not exist
     */
    public EVCharger createEVCharger(String name, String description, Integer gridId, List<Integer> ownerIds, List<Integer> measurementIds, Double maxPower, Integer maxAmp, String connectorType, Integer voltage){
        validateEVChargerCreation(name, description, gridId, ownerIds, measurementIds, maxPower, maxAmp, connectorType, voltage);
        try {
            db.getTransaction().begin();
            EVCharger ev = new EVCharger();
            applyEVChargerFields(ev, name, description, gridId, ownerIds, measurementIds, maxPower, maxAmp, connectorType, voltage);
            db.persist(ev);
            db.getTransaction().commit();
            return ev;
        } 
        catch (Exception e) {
            if (db.getTransaction().isActive()) {
                db.getTransaction().rollback();
            }
            throw e;
        }
    }

    /**
     * Retrieves all EVChargers from the database.
     * @return the list of all EVChargers
     */
    public List<EVCharger> findAllEVChargers(){
        return db.createQuery("SELECT e FROM EVCharger e", EVCharger.class).getResultList();
    }

    /**
     * Retrieves an EVCharger by its ID.
     * @param id the ID of the EVCharger
     * @return the matching EVCharger
     * @throws IllegalArgumentException if the ID is null or no EVCharger matches
     */
    public EVCharger findEVChargerById(Integer id){
        idValidation(id);
        EVCharger ev = db.find(EVCharger.class, id);
        if (ev == null) throw new IllegalArgumentException("EVCharger not found");
        return ev;
    }

    /**
     * Updates an existing EVCharger in the database.
     * @param id the ID of the EVCharger to update
     * @param name the new name
     * @param description the new description
     * @param gridId the new Grid ID
     * @param ownerIds the new list of owner Person IDs
     * @param measurementIds the new list of Measurement IDs
     * @param maxPower the new maximum power
     * @param maxAmp the new maximum current
     * @param connectorType the new connector type
     * @param voltage the new voltage
     * @return the updated EVCharger
     * @throws IllegalArgumentException if no EVCharger matches the given ID
     */
    public EVCharger updateEVCharger(Integer id, String name, String description, Integer gridId, List<Integer> ownerIds, List<Integer> measurementIds, Double maxPower, Integer maxAmp, String connectorType, Integer voltage){
        try {
            db.getTransaction().begin();
            EVCharger ev = findEVChargerById(id);
            applyEVChargerFields(ev, name, description, gridId, ownerIds, measurementIds, maxPower, maxAmp, connectorType, voltage);
            db.getTransaction().commit();
            return ev;
        }
        catch (Exception e) {
            if (db.getTransaction().isActive()) {
                db.getTransaction().rollback();
            }
            throw e;
        }
    }

    // ===== Validation =====

    /**
     * Validates mandatory fields common to all Sensor types.
     * @param name the name to validate
     * @param description the description to validate
     * @param gridId the Grid ID to validate
     * @param ownerIds the list of owner IDs (not validated here)
     * @param measurementIds the list of Measurement IDs (not validated here)
     * @throws IllegalArgumentException if name, description, or gridId are null
     */
    private void validateSensorCreation(String name, String description, Integer gridId, List<Integer> ownerIds, List<Integer> measurementIds) {
        if (name == null) throw new IllegalArgumentException("Missing name");
        if (description == null) throw new IllegalArgumentException("Missing description");
        if (gridId == null) throw new IllegalArgumentException("Missing grid id");
    }

    /**
     * Validates mandatory fields for SolarPanel creation.
     * @param name the name to validate
     * @param description the description to validate
     * @param gridId the Grid ID to validate
     * @param ownerIds the list of owner IDs (not validated here)
     * @param measurementIds the list of Measurement IDs (not validated here)
     * @param powerSource the power source to validate
     * @param efficiency the efficiency to validate
     * @throws IllegalArgumentException if any mandatory field is null
     */
    private void validateSolarPanelCreation(String name, String description, Integer gridId, List<Integer> ownerIds, List<Integer> measurementIds, String powerSource, Float efficiency){
        validateSensorCreation(name, description, gridId, ownerIds, measurementIds);
        if (powerSource == null) throw new IllegalArgumentException("Missing power source");
        if (efficiency == null) throw new IllegalArgumentException("Missing efficiency");
    }

    /**
     * Validates mandatory fields for WindTurbine creation.
     * @param name the name to validate
     * @param description the description to validate
     * @param gridId the Grid ID to validate
     * @param ownerIds the list of owner IDs (not validated here)
     * @param measurementIds the list of Measurement IDs (not validated here)
     * @param powerSource the power source to validate
     * @param bladeLength the blade length to validate
     * @param height the height to validate
     * @throws IllegalArgumentException if any mandatory field is null
     */
    private void validateWindTurbineCreation(String name, String description, Integer gridId, List<Integer> ownerIds, List<Integer> measurementIds, String powerSource, Double bladeLength, Double height){
        validateSensorCreation(name, description, gridId, ownerIds, measurementIds);
        if (powerSource == null) throw new IllegalArgumentException("Missing power source");
        if (bladeLength == null) throw new IllegalArgumentException("Missing blade length");
        if (height == null) throw new IllegalArgumentException("Missing height");
    }

    /**
     * Validates mandatory fields for EVCharger creation.
     * @param name the name to validate
     * @param description the description to validate
     * @param gridId the Grid ID to validate
     * @param ownerIds the list of owner IDs (not validated here)
     * @param measurementIds the list of Measurement IDs (not validated here)
     * @param maxPower the maximum power to validate
     * @param maxAmp the maximum current to validate
     * @param connectorType the connector type to validate
     * @param voltage the voltage to validate
     * @throws IllegalArgumentException if any mandatory field is null
     */
    private void validateEVChargerCreation(String name, String description, Integer gridId, List<Integer> ownerIds, List<Integer> measurementIds, Double maxPower, Integer maxAmp, String connectorType, Integer voltage){
        validateSensorCreation(name, description, gridId, ownerIds, measurementIds);
        if (maxPower == null) throw new IllegalArgumentException("Missing max power");
        if (maxAmp == null) throw new IllegalArgumentException("Missing max amp");
        if (connectorType == null) throw new IllegalArgumentException("Missing connector type");
        if (voltage == null) throw new IllegalArgumentException("Missing voltage");
    }

    /**
     * Validates that the given ID is not null.
     * @param id the ID to validate
     * @throws IllegalArgumentException if the ID is null
     */
    public void idValidation(Integer id){
        if (id == null) throw new IllegalArgumentException("Missing id");
    }

    // ===== Apply Fields =====

    /**
     * Applies common Sensor fields to an existing Sensor, including Grid, owner, and Measurement associations.
     * @param s the Sensor to modify
     * @param name the name to apply
     * @param description the description to apply
     * @param gridId the Grid ID to associate
     * @param ownerIds the list of owner Person IDs to associate
     * @param measurementIds the list of Measurement IDs to associate
     * @throws EntityNotFoundException if the Grid, a Person, or a Measurement does not exist
     */
    private void applySensorFields(Sensor s, String name, String description, Integer gridId, List<Integer> ownerIds, List<Integer> measurementIds){
        if (name != null) s.setName(name);
        if (description != null) s.setDescription(description);
        if (gridId != null) {
            Grid g = db.find(Grid.class, gridId);
            if (g == null) throw new EntityNotFoundException("Grid not found");
            s.setGrid(g);
        }
        if (ownerIds != null) {
            s.getOwners().clear();
            for (Integer pId : ownerIds) {
                Person p = db.find(Person.class, pId);
                if (p == null) throw new EntityNotFoundException("Person not found");
                s.getOwners().add(p);
            }
        }
        if (measurementIds != null) {
            s.getMeasurements().clear();
            for (Integer mId : measurementIds) {
                Measurement m = db.find(Measurement.class, mId);
                if (m == null) throw new EntityNotFoundException("Measurement not found");
                s.getMeasurements().add(m);
            }
        }
    }

    /**
     * Applies SolarPanel-specific fields to an existing SolarPanel.
     * @param sp the SolarPanel to modify
     * @param name the name to apply
     * @param description the description to apply
     * @param gridId the Grid ID to associate
     * @param ownerIds the list of owner Person IDs to associate
     * @param measurementIds the list of Measurement IDs to associate
     * @param powerSource the power source to apply
     * @param efficiency the efficiency to apply
     */
    private void applySolarPanelFields(SolarPanel sp, String name, String description, Integer gridId, List<Integer> ownerIds, List<Integer> measurementIds, String powerSource, Float efficiency){
        applySensorFields(sp, name, description, gridId, ownerIds, measurementIds);
        if (powerSource != null) sp.setPowerSource(powerSource);
        if (efficiency != null) sp.setEfficiency(efficiency);
    }

    /**
     * Applies WindTurbine-specific fields to an existing WindTurbine.
     * @param wt the WindTurbine to modify
     * @param name the name to apply
     * @param description the description to apply
     * @param gridId the Grid ID to associate
     * @param ownerIds the list of owner Person IDs to associate
     * @param measurementIds the list of Measurement IDs to associate
     * @param powerSource the power source to apply
     * @param bladeLength the blade length to apply
     * @param height the height to apply
     */
    private void applyWindTurbineFields(WindTurbine wt, String name, String description, Integer gridId, List<Integer> ownerIds, List<Integer> measurementIds, String powerSource, Double bladeLength, Double height){
        applySensorFields(wt, name, description, gridId, ownerIds, measurementIds);
        if (powerSource != null) wt.setPowerSource(powerSource);
        if (bladeLength != null) wt.setBladeLength(bladeLength);
        if (height != null) wt.setHeight(height);
    }

    /**
     * Applies EVCharger-specific fields to an existing EVCharger.
     * @param ev the EVCharger to modify
     * @param name the name to apply
     * @param description the description to apply
     * @param gridId the Grid ID to associate
     * @param ownerIds the list of owner Person IDs to associate
     * @param measurementIds the list of Measurement IDs to associate
     * @param maxPower the maximum power to apply
     * @param maxAmp the maximum current to apply
     * @param connectorType the connector type to apply
     * @param voltage the voltage to apply
     */
    private void applyEVChargerFields(EVCharger ev, String name, String description, Integer gridId, List<Integer> ownerIds, List<Integer> measurementIds, Double maxPower, Integer maxAmp, String connectorType, Integer voltage){
        applySensorFields(ev, name, description, gridId, ownerIds, measurementIds);
        if (maxPower != null) ev.setMaxPower(maxPower);
        if (maxAmp != null) ev.setMaxAmp(maxAmp);
        if (connectorType != null) ev.setType(connectorType);
        if (voltage != null) ev.setVoltage(voltage);
    }

    /**
    * Receives a measurement for a Sensor, creates or updates the corresponding Measurement entity, and persists the new DataPoint.
    * 
    * If a Measurement with the given name already exists for the Sensor, it adds the new DataPoint to it. Otherwise, it creates a new Measurement and associates it with the Sensor.
     * The unit of the Measurement is set based on the name of the measurement (e.g., "power" -> "kW", "speed" -> "m/s", "temperature" -> "°C").
     * @param id the ID of the Sensor sending the measurement
     * @param timestamp the timestamp of the measurement
     * @param name the name of the measurement (e.g., "power", "speed", "temperature")
     * @param value the value of the measurement
     * @throws EntityNotFoundException if no Sensor matches the given ID
     */
    private void receiveMeasurement(Integer id, Long timestamp, String name, Double value){
        Sensor s = findById(id);
        List<Measurement> measurements = s.getMeasurements();
        Measurement measurement = null;
        for (Measurement m : measurements) {
            if (name.equals(m.getName())){
                measurement = m;
            }
        }

        if (measurement == null){
            measurement = new Measurement();
            measurement.setName(name);
            measurement.setSensor(s);
            if (name.equals("speed")){
                measurement.setUnit("rpm");
            }
            else if (name.equals("power")){
                measurement.setUnit("W");
            }
            else if (name.equals("temperature")){
                measurement.setUnit("C°");
            }
            else if(name.equals("total_energy_produced"))
            {
                measurement.setUnit("Wh");
            }
            s.getMeasurements().add(measurement);
        }

        DataPoint datapoint = new DataPoint();
        datapoint.setTimestamp(timestamp);
        datapoint.setValue(value);
        datapoint.setMeasurement(measurement);
        measurement.getDatapoints().add(datapoint);
        db.persist(datapoint);
        db.persist(measurement);

        if ("power".equals(name) && s instanceof Producer) {
            updateTotalEnergyProduced(s, timestamp, value);
        }
    }

    private void updateTotalEnergyProduced(Sensor s, Long timestamp, Double powerW) {
        Measurement totalMeasurement = null;
        for (Measurement m : s.getMeasurements()) {
            if ("total_energy_produced".equals(m.getName())) {
                totalMeasurement = m;
                break;
            }
        }
        if (totalMeasurement == null) {
            totalMeasurement = new Measurement();
            totalMeasurement.setName("total_energy_produced");
            totalMeasurement.setSensor(s);
            totalMeasurement.setUnit("Wh");
            s.getMeasurements().add(totalMeasurement);
            db.persist(totalMeasurement);
        }

        Double lastTotal = db.createQuery(
            "SELECT MAX(dp.value) FROM DataPoint dp WHERE dp.measurement = :m", Double.class)
            .setParameter("m", totalMeasurement).getSingleResult();
        if (lastTotal == null) lastTotal = 0.0;

        double newTotal = lastTotal + powerW * 60.0 / 3600.0;

        DataPoint dp = new DataPoint();
        dp.setTimestamp(timestamp);
        dp.setValue(newTotal);
        dp.setMeasurement(totalMeasurement);
        totalMeasurement.getDatapoints().add(dp);
        db.persist(dp);
        db.persist(totalMeasurement);
    }
}