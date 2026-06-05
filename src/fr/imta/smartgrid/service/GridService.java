package fr.imta.smartgrid.service;

import fr.imta.smartgrid.model.Grid;
import fr.imta.smartgrid.model.Person;
import fr.imta.smartgrid.model.Sensor;

import java.util.List;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;

/**
 * Business service for CRUD and analytics operations on Grid entities.
 * Manages JPA transactions and Grid-Person-Sensor associations.
 * Provides methods to compute total production and consumption for a Grid.
 */
public class GridService {
    private EntityManager db;
    

    public GridService(EntityManager db) {
        this.db = db;
    }

    // ===== CRUD Operations =====

    /**
     * Creates a new Grid and persists it to the database.
     * @param name the name of the Grid
     * @param description the description of the Grid
     * @param personIds the list of Person IDs to associate
     * @param sensorIds the list of Sensor IDs to associate
     * @return the created Grid
     * @throws IllegalArgumentException if name or description are null
     * @throws EntityNotFoundException if an associated Person or Sensor does not exist
     */
    public Grid create(String name, String description, List<Integer> personIds, List<Integer> sensorIds) {
        createValidation(name, description, personIds, sensorIds);
        try {
            db.getTransaction().begin();
            Grid g = new Grid();
            applyFields(g, name, description, personIds, sensorIds);
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

    /**
     * Retrieves all Grids from the database.
     * @return the list of all Grids
     */
    public List<Grid> findAll(){
        return db.createQuery("SELECT g FROM Grid g", Grid.class).getResultList();
    }

    /**
     * Retrieves a Grid by its ID.
     * @param id the ID of the Grid
     * @return the matching Grid
     * @throws IllegalArgumentException if the ID is null
     * @throws EntityNotFoundException if no Grid matches the given ID
     */
    public Grid findById(Integer id){
        idValidation(id);
        Grid g = db.find(Grid.class, id);
        if (g == null) throw new EntityNotFoundException("Grid not found");
        return g;
    }

    /**
     * Updates an existing Grid in the database.
     * @param id the ID of the Grid to update
     * @param name the new name
     * @param description the new description
     * @param userIds the new list of Person IDs
     * @param sensorIds the new list of Sensor IDs
     * @return the updated Grid
     * @throws EntityNotFoundException if the Grid, an associated Person, or a Sensor does not exist
     */
    public Grid update(Integer id, String name, String description, List<Integer> userIds, List<Integer> sensorIds){
        try {
            db.getTransaction().begin();
            Grid g = findById(id);
            applyFields(g, name, description, userIds, sensorIds);
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

    /**
     * Deletes a Grid by its ID.
     * @param id the ID of the Grid to delete
     * @throws EntityNotFoundException if no Grid matches the given ID
     */
    public void delete(Integer id){
        try {
            db.getTransaction().begin();
            Grid g = findById(id);
            db.remove(g);
            db.getTransaction().commit();
        } 
        catch (Exception e) {
            if (db.getTransaction().isActive()) {
                db.getTransaction().rollback();
            }
            throw e;
        }
    }

    // ===== Analytic Operations =====

    /**
     * Computes the total production of all producer Sensors in the Grid.
     * @param id the ID of the Grid
     * @return the total production as a Double
     * @throws EntityNotFoundException if no Grid matches the given ID
     */
    public Double computeTotalProduction(Integer id){
        findById(id);
        String qlString = 
            "SELECT SUM(dp.value) FROM DataPoint dp " +
            "WHERE dp.measurement.sensor.id IN (" +
            "SELECT p.id FROM Producer p " +
            "WHERE p.grid.id = :id) AND dp.measurement.name = 'power'";
        Double result = db.createQuery(qlString, Double.class)
            .setParameter("id", id)
            .getSingleResult();
        return result != null ? result : 0.0;
    }

    /**
     * Computes the total consumption of all consumer Sensors in the Grid using a JPQL query.
     * @param id the ID of the Grid
     * @return the total consumption as a Double, 0.0 if no data
     */
    public Double computeTotalConsumption(Integer id){
        findById(id);
        String qlString = 
            "SELECT SUM(dp.value) FROM DataPoint dp " +
            "WHERE dp.measurement.sensor.id IN (" +
            "SELECT c.id FROM Consumer c " +
            "WHERE c.grid.id = :id)";
        Double result = db.createQuery(qlString, Double.class)
            .setParameter("id", id)
            .getSingleResult();
        return result != null ? result : 0.0;
    }

    // ===== Utils =====

    /**
     * Validates mandatory fields for Grid creation.
     * @param name the name to validate
     * @param description the description to validate
     * @param personIds the list of Person IDs (not validated here)
     * @param sensorIds the list of Sensor IDs (not validated here)
     * @throws IllegalArgumentException if name or description are null
     */
    private void createValidation(String name, String description, List<Integer> personIds, List<Integer> sensorIds){
        if (name == null) throw new IllegalArgumentException("Missing name"); 
        if (description == null) throw new IllegalArgumentException("Missing description"); 
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
     * Applies the given fields to an existing Grid, including Person and Sensor associations.
     * @param g the Grid to modify
     * @param name the name to apply
     * @param description the description to apply
     * @param personIds the list of Person IDs to associate
     * @param sensorIds the list of Sensor IDs to associate
     * @throws EntityNotFoundException if an associated Person or Sensor does not exist
     */
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