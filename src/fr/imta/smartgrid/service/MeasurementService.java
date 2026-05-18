package fr.imta.smartgrid.service;

import fr.imta.smartgrid.model.Sensor;
import fr.imta.smartgrid.model.Measurement;
import fr.imta.smartgrid.model.DataPoint;

import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.TypedQuery;

public class MeasurementService {
    private EntityManager db;

    public MeasurementService (EntityManager db) {
        this.db = db;
    }

    // CRUD Operations

    public Measurement create(String unit, String name, Integer sensorId, List<Integer> dataPointIds) {
        // Input validation
        createValidation(unit,name,sensorId,dataPointIds);

        // Creation
        try {
            // Begin transaction
            db.getTransaction().begin();

            // Create a new Measurement and set its data
            Measurement m = new Measurement();
            applyFields(m,unit,name,sensorId,dataPointIds);

            // Validate the transaction
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

    public List<Measurement> findAll(){
        return db.createQuery("SELECT m FROM Measurement m", Measurement.class).getResultList();
    }

    public Measurement findById(Integer id){
        // Input Validation
        idValidation(id);

        // Find the Measurement
        Measurement m = db.find(Measurement.class, id);
        if (m == null) throw new EntityNotFoundException("Measurement not found");
        return m;
    }

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

    public Measurement update(Integer id, String unit, String name, Integer sensorId, List<Integer> dataPointIds){
        try {
            // Begin transaction
            db.getTransaction().begin();

            // Find the Measurement and update its data
            Measurement m = findById(id);
            applyFields(m, unit, name, sensorId, dataPointIds);

            // Validate the transaction
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

    public void delete(Integer id){
        try {
            // Begin transaction
            db.getTransaction().begin();

            // Find the Grid and delete it if it exists
            Measurement m = findById(id);
            db.remove(m);

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

    // Utils

    private void createValidation(String unit, String name, Integer sensorId, List<Integer> dataPointIds){
        if (unit == null) throw new IllegalArgumentException("Missing unit"); 
        if (name == null) throw new IllegalArgumentException("Missing name"); 
        if(sensorId == null) throw new IllegalArgumentException("Missing sensor id");
    }

    private void idValidation(Integer id){
        if (id == null) throw new IllegalArgumentException("Missing id");
    }

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

