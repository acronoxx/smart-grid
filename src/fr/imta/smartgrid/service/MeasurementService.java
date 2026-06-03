package fr.imta.smartgrid.service;

import fr.imta.smartgrid.model.Sensor;
import fr.imta.smartgrid.model.Measurement;
import fr.imta.smartgrid.model.DataPoint;

import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.TypedQuery;

/**
 * Business service for CRUD operations on Measurement entities.
 * Manages JPA transactions and DataPoint retrieval.
 */
public class MeasurementService {
    private EntityManager db;

    public MeasurementService(EntityManager db) {
        this.db = db;
    }

    // ===== CRUD Operations =====

    /**
     * Creates a new Measurement and persists it to the database.
     * @param unit the unit of measurement
     * @param name the name of the Measurement
     * @param sensorId the ID of the associated Sensor
     * @param dataPointIds the list of DataPoint IDs to associate
     * @return the created Measurement
     * @throws IllegalArgumentException if unit, name, or sensorId are null
     * @throws EntityNotFoundException if the associated Sensor or a DataPoint does not exist
     */
    public Measurement create(String unit, String name, Integer sensorId, List<Integer> dataPointIds) {
        createValidation(unit, name, sensorId, dataPointIds);
        try {
            db.getTransaction().begin();
            Measurement m = new Measurement();
            applyFields(m, unit, name, sensorId, dataPointIds);
            db.persist(m);
            db.getTransaction().commit();
            return m;
        } 
        catch (Exception e) {
            if (db.getTransaction().isActive()) {
                db.getTransaction().rollback();
            }
            throw e;
        }
    }

    /**
     * Retrieves all Measurements from the database.
     * @return the list of all Measurements
     */
    public List<Measurement> findAll(){
        return db.createQuery("SELECT m FROM Measurement m", Measurement.class).getResultList();
    }

    /**
     * Retrieves a Measurement by its ID.
     * @param id the ID of the Measurement
     * @return the matching Measurement
     * @throws IllegalArgumentException if the ID is null
     * @throws EntityNotFoundException if no Measurement matches the given ID
     */
    public Measurement findById(Integer id){
        idValidation(id);
        Measurement m = db.find(Measurement.class, id);
        if (m == null) throw new EntityNotFoundException("Measurement not found");
        return m;
    }

    /**
     * Retrieves the DataPoints of a Measurement over an optional time range.
     * @param id the ID of the Measurement
     * @param from the start timestamp (inclusive), null if not filtered
     * @param to the end timestamp (inclusive), null if not filtered
     * @return the list of matching DataPoints
     * @throws EntityNotFoundException if no Measurement matches the given ID
     */
    public List<DataPoint> findValues(Integer id, Long from, Long to){
        findById(id);
        TypedQuery<DataPoint> query = db.createQuery(
            "SELECT dp FROM DataPoint dp WHERE dp.measurement.id = :id" +
            (from != null ? " AND dp.timestamp >= :from" : "") +
            (to != null ? " AND dp.timestamp <= :to" : ""),
            DataPoint.class)
            .setParameter("id", id);
        if (from != null) query.setParameter("from", from);
        if (to != null) query.setParameter("to", to);
        return query.getResultList();
    }

    /**
     * Updates an existing Measurement in the database.
     * @param id the ID of the Measurement to update
     * @param unit the new unit of measurement
     * @param name the new name
     * @param sensorId the new associated Sensor ID
     * @param dataPointIds the new list of DataPoint IDs
     * @return the updated Measurement
     * @throws EntityNotFoundException if the Measurement, Sensor, or a DataPoint does not exist
     */
    public Measurement update(Integer id, String unit, String name, Integer sensorId, List<Integer> dataPointIds){
        try {
            db.getTransaction().begin();
            Measurement m = findById(id);
            applyFields(m, unit, name, sensorId, dataPointIds);
            db.getTransaction().commit();
            return m;
        } 
        catch (Exception e) {
            if (db.getTransaction().isActive()) {
                db.getTransaction().rollback();
            }
            throw e;
        }
    }

    /**
     * Deletes a Measurement by its ID.
     * @param id the ID of the Measurement to delete
     * @throws EntityNotFoundException if no Measurement matches the given ID
     */
    public void delete(Integer id){
        try {
            db.getTransaction().begin();
            Measurement m = findById(id);
            db.remove(m);
            db.getTransaction().commit();
        } 
        catch (Exception e) {
            if (db.getTransaction().isActive()) {
                db.getTransaction().rollback();
            }
            throw e;
        }
    }

    // ===== Utils =====

    /**
     * Validates mandatory fields for Measurement creation.
     * @param unit the unit to validate
     * @param name the name to validate
     * @param sensorId the Sensor ID to validate
     * @param dataPointIds the list of DataPoint IDs (not validated here)
     * @throws IllegalArgumentException if unit, name, or sensorId are null
     */
    private void createValidation(String unit, String name, Integer sensorId, List<Integer> dataPointIds){
        if (unit == null) throw new IllegalArgumentException("Missing unit"); 
        if (name == null) throw new IllegalArgumentException("Missing name"); 
        if (sensorId == null) throw new IllegalArgumentException("Missing sensor id");
    }

    /**
     * Validates that the given ID is not null.
     * @param id the ID to validate
     * @throws IllegalArgumentException if the ID is null
     */
    private void idValidation(Integer id){
        if (id == null) throw new IllegalArgumentException("Missing id");
    }

    /**
     * Applies the given fields to an existing Measurement, including Sensor and DataPoint associations.
     * @param m the Measurement to modify
     * @param unit the unit to apply
     * @param name the name to apply
     * @param sensorId the ID of the Sensor to associate
     * @param dataPointIds the list of DataPoint IDs to associate
     * @throws EntityNotFoundException if the associated Sensor or a DataPoint does not exist
     */
    private void applyFields(Measurement m, String unit, String name, Integer sensorId, List<Integer> dataPointIds) {
        if (unit != null) m.setUnit(unit);
        if (name != null) m.setName(name);
        if (sensorId != null) {
            Sensor s = db.find(Sensor.class, sensorId);
            if (s == null) throw new EntityNotFoundException("Sensor not found");
            m.setSensor(s);
        }
        if (dataPointIds != null) {
            m.getDatapoints().clear();
            for (Integer dpId : dataPointIds) {
                DataPoint dp = db.find(DataPoint.class, dpId);
                if (dp == null) throw new EntityNotFoundException("DataPoint not found");
                m.getDatapoints().add(dp);
            }
        }
    }
}