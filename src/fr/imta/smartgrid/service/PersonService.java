package fr.imta.smartgrid.service;

import fr.imta.smartgrid.model.Person;
import fr.imta.smartgrid.model.Grid;
import fr.imta.smartgrid.model.Sensor;

import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;

public class PersonService {
    private EntityManager db;

    public PersonService(EntityManager db) {
        this.db = db;
    }

    // CRUD Operations

    public Person create(String firstName, String lastName, Integer gridId, List<Integer> sensorsId) {
        // Input validation
        createValidation(firstName, lastName, gridId, sensorsId);
        
        try {
            // Begin transaction
            db.getTransaction().begin();

            // Create a new Person and set its data
            Person p = new Person();
            applyFields(p, firstName, lastName, gridId, sensorsId);

            // Validate the transaction
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

    public List<Person> findAll(){
        return db.createQuery("SELECT p FROM Person p", Person.class).getResultList();
    }

    public Person findById(Integer id){
        // Input Validation
        idValidation(id);

        // Find the Person
        Person p = db.find(Person.class, id);
        if (p == null){
            throw new EntityNotFoundException("Person not found");
        }
        return p;
    }

    public Person update(Integer id, String firstName, String lastName, Integer gridId, List<Integer> sensorsId){
        try {
            // Begin transaction
            db.getTransaction().begin();

            // Find the Person and update its data
            Person p = findById(id);
            applyFields(p, firstName, lastName, gridId, sensorsId);

            // Validate the transaction
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

    public void delete(Integer id){
        try {
            // Begin transaction
            db.getTransaction().begin();

            // Find the Person and delete it if he exists
            Person p = findById(id);
            db.remove(p);

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

    // UTILS

    private void createValidation(String firstName, String lastName, Integer gridId, List<Integer> sensorsId){
        if (firstName == null) throw new IllegalArgumentException("Missing first name"); 
        if (lastName == null) throw new IllegalArgumentException("Missing last name"); 
        if (gridId == null) throw new IllegalArgumentException("Missing grid id"); 
    }

    private void idValidation(Integer id){
        if (id == null) throw new IllegalArgumentException("Missing id");
    }

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
