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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;

public class SensorService {
    private EntityManager db;
    private Map<Integer, Double> totalProduction = new HashMap<>();

    public SensorService (EntityManager db) {
        this.db = db;
        initializeTotalProduction();
    }

    public void initializeTotalProduction() {
        try {
            List<Producer> producers = db.createQuery("SELECT p FROM Producer p", Producer.class).getResultList();
            for (Producer p : producers) {
                Double production = db.createQuery(
                    "SELECT SUM(dp.value) FROM DataPoint dp " +
                    "JOIN dp.measurement m " +
                    "WHERE m.sensor.id = :sensorId AND m.name = 'power'", 
                    Double.class
                )
                .setParameter("sensorId", p.getId())
                .getSingleResult();

                totalProduction.put(p.getId(), production);
            }
        }
        catch (Exception e){}
    }

    public Map<Integer, Double> getTotalProduction(){
        return totalProduction;
    }

    // CRUD Operations

    public List<Sensor> findAll(){
        return db.createQuery("SELECT s FROM Sensor s", Sensor.class).getResultList();
    }

    public List<Producer> findAllProducers(){
        return db.createQuery("SELECT p FROM Producer p", Producer.class).getResultList();
    }
    public List<Consumer> findAllConsumers(){
        return db.createQuery("SELECT c FROM Consumer c", Consumer.class).getResultList();  
    }

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

    public Sensor findById(Integer id){
        // Input Validation
        idValidation(id);

        // Find the Sensor
        Sensor s = db.find(Sensor.class, id);
        if (s == null) throw new IllegalArgumentException("Sensor not found");
        return s;
    }

    public void delete(Integer id){
        try {
            // Begin transaction
            db.getTransaction().begin();

            // Find the Sensor and delete it if it exists
            Sensor s = findById(id);
            db.remove(s);

            // Validate the transaction
            db.getTransaction().commit();
        } 
        catch (Exception e) {
            if (db.getTransaction().isActive()) {
                db.getTransaction().rollback();
            }
            throw e;
        }
    }

    // SolarPanel

    public SolarPanel createSolarPanel(String name, String description, Integer gridId, List<Integer> ownerIds, List<Integer> measurementIds, String powerSource, Float efficiency){
        // Input Validation
        validateSolarPanelCreation(name, description, gridId, ownerIds, measurementIds, powerSource, efficiency);

        // Creation
        try {
            // Begin transaction
            db.getTransaction().begin();

            // Create a new SolarPanel and set its data
            SolarPanel sp = new SolarPanel();
            applySolarPanelFields(sp, name, description, gridId, ownerIds, measurementIds, powerSource, efficiency);

            // Validate the transaction
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

    public List<SolarPanel> findAllSolarPanels(){
        return db.createQuery("SELECT sp FROM SolarPanel sp", SolarPanel.class).getResultList();
    }

    public SolarPanel findSolarPanelById(Integer id){
        // Input Validation
        idValidation(id);

        // Find the SolarPanel
        SolarPanel sp = db.find(SolarPanel.class, id);
        if (sp == null) throw new IllegalArgumentException("SolarPanel not found");
        return sp;
    }

    public SolarPanel updateSolarPanel(Integer id, String name, String description, Integer gridId, List<Integer> ownerIds, List<Integer> measurementIds, String powerSource, Float efficiency){
        try{
            // Begin transaction
            db.getTransaction().begin();

            // Find the SolarPanel and update its data
            SolarPanel s = findSolarPanelById(id);
            applySolarPanelFields(s, name, description, gridId, ownerIds, measurementIds, powerSource, efficiency);

            // Validate the transaction
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

    public void receiveSolarPanelMeasurement(Integer id, Long timestamp, String name, Double value){
        try {
            // Begin transaction
            db.getTransaction().begin();

            // Find the SolarPanel and all its measurements
            SolarPanel sp = findSolarPanelById(id);
            List<Measurement> measurements = sp.getMeasurements();

            // Find or create the Measurement corresponding to the given name
            Measurement measurement = null;
            for (Measurement m : measurements) {
                if (name.equals(m.getName())){
                    measurement = m;
                }
            }

            if (measurement == null){
                measurement = new Measurement();
                measurement.setName(name);
                measurement.setSensor(sp);

                if (name.equals("power")){
                    measurement.setUnit("kW");
                }
                else if (name.equals("temperature")){
                    measurement.setUnit("°C");
                }
            }

            // Create a new DataPoint and set its data
            DataPoint datapoint = new DataPoint();
            datapoint.setTimestamp(timestamp);
            datapoint.setValue(value);
            datapoint.setMeasurement(measurement);
            measurement.getDatapoints().add(datapoint);

            // Update total production            
            if (name.equals("power")){
                totalProduction.put(sp.getId(), value * 60 + totalProduction.getOrDefault(sp.getId(), 0.0));
            }

            // Validate the transaction
            db.persist(datapoint);
            db.persist(measurement);
            db.getTransaction().commit();
        }
        catch (Exception e) {
            if (db.getTransaction().isActive()) {
                db.getTransaction().rollback();
            }
            throw e;
        }
    }

    // WindTurbine

    public WindTurbine createWindTurbine(String name, String description, Integer gridId, List<Integer> ownerIds, List<Integer> measurementIds, String powerSource, Double bladeLength, Double height){
        // Input Validation
        validateWindTurbineCreation(name, description, gridId, ownerIds, measurementIds, powerSource, bladeLength, height);

        // Creation
        try {
            // Begin transaction
            db.getTransaction().begin();

            // Create a new WindTurbine and set its data
            WindTurbine wt = new WindTurbine();
            applyWindTurbineFields(wt, name, description, gridId, ownerIds, measurementIds, powerSource, bladeLength, height);

            // Validate the transaction
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

    public List<WindTurbine> findAllWindTurbines(){
        return db.createQuery("SELECT w FROM WindTurbine w", WindTurbine.class).getResultList();
    }

    public WindTurbine findWindTurbineById(Integer id){
        // Input Validation
        idValidation(id);

        // Find the WindTurbine
        WindTurbine wt = db.find(WindTurbine.class, id);
        if (wt == null) throw new IllegalArgumentException("WindTurbine not found");
        return wt;
    }

    public WindTurbine updateWindTurbine(Integer id, String name, String description, Integer gridId, List<Integer> ownerIds, List<Integer> measurementIds, String powerSource, Double bladeLength, Double height){
        try {
            // Begin transaction
            db.getTransaction().begin();

            // Find the WindTurbine and update its data
            WindTurbine wt = findWindTurbineById(id);
            applyWindTurbineFields(wt, name, description, gridId, ownerIds, measurementIds, powerSource, bladeLength, height);

            // Validate the transaction
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

    public void receiveWindTurbineMeasurement(Integer id, Long timestamp, String name, Double value){
        try {
            // Begin transaction
            db.getTransaction().begin();

            // Find the WindTurbine and all its measurements
            WindTurbine wt = findWindTurbineById(id);
            List<Measurement> measurements = wt.getMeasurements();

            // Find or create the Measurement corresponding to the given name
            Measurement measurement = null;
            for (Measurement m : measurements) {
                if (name.equals(m.getName())){
                    measurement = m;
                }
            }

            if (measurement == null){
                measurement = new Measurement();
                measurement.setName(name);
                measurement.setSensor(wt);

                if (name.equals("speed")){
                    measurement.setUnit("m/s");
                }
                else if (name.equals("power")){
                    measurement.setUnit("kW");
                }
            }

            // Create a new DataPoint and set its data
            DataPoint datapoint = new DataPoint();
            datapoint.setTimestamp(timestamp);
            datapoint.setValue(value);
            datapoint.setMeasurement(measurement);
            measurement.getDatapoints().add(datapoint);

            // Update total production
            if (name.equals("power")){
                totalProduction.put(wt.getId(), value * 60 + totalProduction.getOrDefault(wt.getId(), 0.0));
            }

            // Validate the transaction
            db.persist(datapoint);
            db.persist(measurement);
            db.getTransaction().commit();
        }
        catch (Exception e) {
            if (db.getTransaction().isActive()) {
                db.getTransaction().rollback();
            }
            throw e;
        }
    }

    // EVCharger

    public EVCharger createEVCharger(String name, String description, Integer gridId, List<Integer> ownerIds, List<Integer> measurementIds, Double maxPower, Integer maxAmp, String connectorType, Integer voltage){
        // Input Validation
        validateEVChargerCreation(name, description, gridId, ownerIds, measurementIds, maxPower, maxAmp, connectorType, voltage);

        // Creation
        try {
            // Begin transaction
            db.getTransaction().begin();

            // Create a new EVCharger and set its data
            EVCharger ev = new EVCharger();
            applyEVChargerFields(ev, name, description, gridId, ownerIds, measurementIds, maxPower, maxAmp, connectorType, voltage);

            // Validate the transaction
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

    public List<EVCharger> findAllEVChargers(){
        return db.createQuery("SELECT e FROM EVCharger e", EVCharger.class).getResultList();
    }

    public EVCharger findEVChargerById(Integer id){
        // Input Validation
        idValidation(id);

        // Find the EVCharger
        EVCharger ev = db.find(EVCharger.class, id);
        if (ev == null) throw new IllegalArgumentException("EVCharger not found");
        return ev;
    }

    public EVCharger updateEVCharger(Integer id, String name, String description, Integer gridId, List<Integer> ownerIds, List<Integer> measurementIds, Double maxPower, Integer maxAmp, String connectorType, Integer voltage){
        try {
            // Begin transaction
            db.getTransaction().begin();

            // Find the EVCharger and update its data
            EVCharger ev = findEVChargerById(id);
            applyEVChargerFields(ev, name, description, gridId, ownerIds, measurementIds, maxPower, maxAmp, connectorType, voltage);

            // Validate the transaction
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

    // Validation methods

    private void validateSensorCreation(String name, String description, Integer gridId, List<Integer> ownerIds, List<Integer> measurementIds) {
        if (name == null) {
            throw new IllegalArgumentException("Missing name");
        }
        if (description == null) { 
            throw new IllegalArgumentException("Missing description");
        }
        if (gridId == null) { 
            throw new IllegalArgumentException("Missing grid id");
        }
    }

    private void validateSolarPanelCreation(String name, String description, Integer gridId, List<Integer> ownerIds, List<Integer> measurementIds, String powerSource, Float efficiency){
        validateSensorCreation(name, description, gridId, ownerIds, measurementIds);
        if (powerSource == null){
            throw new IllegalArgumentException("Missing power source");
        }
        if (efficiency == null){
            throw new IllegalArgumentException("Missing efficiency");
        }
    }

    private void validateWindTurbineCreation(String name, String description, Integer gridId, List<Integer> ownerIds, List<Integer> measurementIds, String powerSource, Double bladeLength, Double height){
        validateSensorCreation(name, description, gridId, ownerIds, measurementIds);
        if (powerSource == null){
            throw new IllegalArgumentException("Missing power source");
        }
        if (bladeLength == null){
            throw new IllegalArgumentException("Missing blade length");
        }
        if (height == null){
            throw new IllegalArgumentException("Missing height");
        }
    }

    private void validateEVChargerCreation(String name, String description, Integer gridId, List<Integer> ownerIds, List<Integer> measurementIds, Double maxPower, Integer maxAmp, String connectorType, Integer voltage){
        validateSensorCreation(name, description, gridId, ownerIds, measurementIds);
        if (maxPower == null){
            throw new IllegalArgumentException("Missing max power");
        }
        if (maxAmp == null){
            throw new IllegalArgumentException("Missing max amp");
        }
        if (connectorType == null){
            throw new IllegalArgumentException("Missing connector type");
        }
        if (voltage == null){
            throw new IllegalArgumentException("Missing voltage");
        }
    }

    public void idValidation(Integer id){
        if (id == null) throw new IllegalArgumentException("Missing id");
    }

    // Apply Fields methods

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

    private void applySolarPanelFields(SolarPanel sp, String name, String description, Integer gridId, List<Integer> ownerIds, List<Integer> measurementIds, String powerSource, Float efficiency){
        applySensorFields(sp, name, description, gridId, ownerIds, measurementIds);
        if (powerSource != null) sp.setPowerSource(powerSource);
        if (efficiency != null) sp.setEfficiency(efficiency);
    }

    private void applyWindTurbineFields(WindTurbine wt, String name, String description, Integer gridId, List<Integer> ownerIds, List<Integer> measurementIds, String powerSource, Double bladeLength, Double height){
        applySensorFields(wt, name, description, gridId, ownerIds, measurementIds);
        if (powerSource != null) wt.setPowerSource(powerSource);
        if (bladeLength != null) wt.setBladeLength(bladeLength);
        if (height != null) wt.setHeight(height);
    }

    private void applyEVChargerFields(EVCharger ev, String name, String description, Integer gridId, List<Integer> ownerIds, List<Integer> measurementIds, Double maxPower, Integer maxAmp, String connectorType, Integer voltage){
        applySensorFields(ev, name, description, gridId, ownerIds, measurementIds);
        if (maxPower != null) ev.setMaxPower(maxPower);
        if (maxAmp != null) ev.setMaxAmp(maxAmp);
        if (connectorType != null) ev.setType(connectorType);
        if (voltage != null) ev.setVoltage(voltage);
    }

}