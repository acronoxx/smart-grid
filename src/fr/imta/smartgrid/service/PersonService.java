package fr.imta.smartgrid.service;

import fr.imta.smartgrid.model.Person;
import fr.imta.smartgrid.model.Grid;
import fr.imta.smartgrid.model.Sensor;

import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;

/**
 * Business service for CRUD operations on Person entities.
 * Manages JPA transactions and Person-Grid-Sensor associations.
 */
public class PersonService {
    private EntityManager db;

    public PersonService(EntityManager db) {
        this.db = db;
    }

    // ===== CRUD Operations =====

    /**
     * Creates a new Person and persists it to the database.
     * @param firstName the first name of the Person
     * @param lastName the last name of the Person
     * @param gridId the ID of the associated Grid
     * @param sensorsId the list of owned Sensor IDs to associate
     * @return the created Person
     * @throws IllegalArgumentException if firstName, lastName, or gridId are null
     * @throws EntityNotFoundException if the associated Grid or a Sensor does not exist
     */
    public Person create(String firstName, String lastName, Integer gridId, List<Integer> sensorsId) {
        createValidation(firstName, lastName, gridId, sensorsId);
        try {
            db.getTransaction().begin();
            Person p = new Person();
            applyFields(p, firstName, lastName, gridId, sensorsId);
            db.persist(p);
            db.getTransaction().commit();
            return p;
        } 
        catch (Exception e) {
            if (db.getTransaction().isActive()) {
                db.getTransaction().rollback();
            }
            throw e;
        }
    }

    /**
     * Retrieves all Persons from the database.
     * @return the list of all Persons
     */
    public List<Person> findAll(){
        return db.createQuery("SELECT p FROM Person p", Person.class).getResultList();
    }

    /**
     * Retrieves a Person by its ID.
     * @param id the ID of the Person
     * @return the matching Person
     * @throws IllegalArgumentException if the ID is null
     * @throws EntityNotFoundException if no Person matches the given ID
     */
    public Person findById(Integer id){
        idValidation(id);
        Person p = db.find(Person.class, id);
        if (p == null){
            throw new EntityNotFoundException("Person not found");
        }
        return p;
    }

    /**
     * Updates an existing Person in the database.
     * @param id the ID of the Person to update
     * @param firstName the new first name
     * @param lastName the new last name
     * @param gridId the new associated Grid ID
     * @param sensorsId the new list of owned Sensor IDs
     * @return the updated Person
     * @throws EntityNotFoundException if the Person, Grid, or a Sensor does not exist
     */
    public Person update(Integer id, String firstName, String lastName, Integer gridId, List<Integer> sensorsId){
        try {
            db.getTransaction().begin();
            Person p = findById(id);
            applyFields(p, firstName, lastName, gridId, sensorsId);
            db.getTransaction().commit();
            return p;
        } 
        catch (Exception e) {
            if (db.getTransaction().isActive()) {
                db.getTransaction().rollback();
            }
            throw e;
        }
    }

    /**
     * Deletes a Person by its ID.
     * @param id the ID of the Person to delete
     * @throws EntityNotFoundException if no Person matches the given ID
     */
    public void delete(Integer id){
        try {
            db.getTransaction().begin();
            Person p = findById(id);
            db.remove(p);
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
     * Validates mandatory fields for Person creation.
     * @param firstName the first name to validate
     * @param lastName the last name to validate
     * @param gridId the Grid ID to validate
     * @param sensorsId the list of Sensor IDs (not validated here)
     * @throws IllegalArgumentException if firstName, lastName, or gridId are null
     */
    private void createValidation(String firstName, String lastName, Integer gridId, List<Integer> sensorsId){
        if (firstName == null) throw new IllegalArgumentException("Missing first name"); 
        if (lastName == null) throw new IllegalArgumentException("Missing last name"); 
        if (gridId == null) throw new IllegalArgumentException("Missing grid id"); 
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
     * Applies the given fields to an existing Person, including Grid and Sensor associations.
     * @param p the Person to modify
     * @param firstName the first name to apply
     * @param lastName the last name to apply
     * @param gridId the ID of the Grid to associate
     * @param sensorIds the list of Sensor IDs to associate
     * @throws EntityNotFoundException if the associated Grid or a Sensor does not exist
     */
    private void applyFields(Person p, String firstName, String lastName, Integer gridId, List<Integer> sensorIds) {
        if (firstName != null) p.setFirstName(firstName);
        if (lastName != null) p.setLastName(lastName);
        if (gridId != null) {
            Grid g = db.find(Grid.class, gridId);
            if (g == null) throw new EntityNotFoundException("Grid not found");
            p.setGrid(g);
        }
        if (sensorIds != null) {
            p.getSensors().clear();
            for (Integer sId : sensorIds) {
                Sensor s = db.find(Sensor.class, sId);
                if (s == null) throw new EntityNotFoundException("Sensor not found");
                p.getSensors().add(s);
            }
        }
    }
}