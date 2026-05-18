package fr.imta.smartgrid.service;

import fr.imta.smartgrid.model.Grid;
import fr.imta.smartgrid.model.Person;
import fr.imta.smartgrid.model.Sensor;

import java.util.List;
import java.util.Map;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;

public class GridService {
    private EntityManager db;
    private SensorService sensorService;

    public GridService(EntityManager db, SensorService sensorService) {
        this.db = db;
        this.sensorService = sensorService;
    }

    // CRUD Operations

    public Grid create(String name, String description, List<Integer> personIds, List<Integer> sensorIds) {
        // Input validation
        createValidation(name, description, personIds, sensorIds);
        
        try {
            // Begin transaction
            db.getTransaction().begin();

            // Create a new Grid and set its data
            Grid g = new Grid();
            applyFields(g, name, description, personIds, sensorIds);

            // Validate the transaction
            db.persist(g);
            db.getTransaction().commit();

            return g;
        } 
        catch (Exception e) {
            if (db.getTransaction().isActive()) {
                db.getTransaction().rollback();
            }
            throw e;
        }
    }

    public List<Grid> findAll(){
        return db.createQuery("SELECT g FROM Grid g", Grid.class).getResultList();
    }

    public Grid findById(Integer id){
        // Input Validation
        idValidation(id);

        // Find the Grid
        Grid g = db.find(Grid.class, id);
        if (g == null) throw new EntityNotFoundException("Grid not found");
        return g;
    }

    public Grid update(Integer id, String name, String description, List<Integer> userIds, List<Integer> sensorIds){
        try {
            // Begin transaction
            db.getTransaction().begin();

            // Find the Grid and update its data
            Grid g = findById(id);
            applyFields(g, name, description, userIds, sensorIds);

            // Validate the transaction
            db.getTransaction().commit();

            return g;
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

            // Find the Grid and delete it if exists
            Grid g = findById(id);
            db.remove(g);

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

    // Analytic operations

    public Double computeTotalProduction(Integer id){
        try{
            Grid g = findById(id);
            List<Sensor> sensors = g.getSensors();
            Map<Integer, Double> totalProduction = sensorService.getTotalProduction();
            Double result = 0.0;

            for (Map.Entry<Integer, Double> entry : totalProduction.entrySet()) {
                Integer sensorId = entry.getKey();
                Sensor sensor = sensorService.findById(sensorId);
                Double production = entry.getValue();

                if(sensors.contains(sensor)){
                    result = result + production;
                }
            }

            return result;
        }
        catch (Exception e){
            throw e;
        }
    }

    public Double computeTotalConsumption(Integer id){
        try{
            String qlString = 
                "SELECT SUM(dp.value) FROM DataPoint dp " +
                "WHERE dp.measurement.sensor.id IN (" +
                "SELECT c.id FROM Consumer c " +
                "WHERE c.grid.id = :id)";

            Double result = db.createQuery(
                qlString,
                Double.class)
                .setParameter("id", id)
                .getSingleResult();

            return result != null ? result : 0.0;
        }
        catch (Exception e){
            throw e;
        }
    }

    // Utils

    private void createValidation(String name, String description, List<Integer> personIds, List<Integer> sensorIds){
        if (name == null) throw new IllegalArgumentException("Missing name"); 
        if (description == null) throw new IllegalArgumentException("Missing description"); 
    }

    private void idValidation(Integer id){
        if (id == null) throw new IllegalArgumentException("Missing id");
    }

    private void applyFields(Grid g, String name, String description, List<Integer> personIds, List<Integer> sensorIds) {
        if (name != null) g.setName(name);
        if (description != null) g.setDescription(description);

        if (personIds != null) {
            g.getPersons().clear();
            for (Integer pId : personIds) {
                Person p = db.find(Person.class, pId);
                if (p == null) throw new EntityNotFoundException("Person not found");
                g.getPersons().add(p);
            }
        }

        if (sensorIds != null) {
            g.getSensors().clear();
            for (Integer sId : sensorIds) {
                Sensor s = db.find(Sensor.class, sId);
                if (s == null) throw new EntityNotFoundException("Sensor not found");
                g.getSensors().add(s);
            }
        }
    }
}

