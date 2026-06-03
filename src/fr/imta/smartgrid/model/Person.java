package fr.imta.smartgrid.model;

import java.util.ArrayList;
import java.util.List;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * JPA entity representing a person in the SmartGrid system.
 * A Person belongs to a Grid and can own multiple Sensors.
 * Mapped to the "person" table.
 */
@Entity
@Table(name = "person")
public class Person {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String firstName;
    private String lastName;

    @ManyToOne
    @JoinColumn(name = "grid")
    private Grid grid;

    @ManyToMany
    @JoinTable(
        name = "person_sensor",
        joinColumns = @JoinColumn(name = "person_id"),
        inverseJoinColumns = @JoinColumn(name = "sensor_id")
    )
    private List<Sensor> sensors = new ArrayList<>();

    /**
     * Returns the ID of this Person.
     * @return the ID
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the ID of this Person.
     * @param id the ID to set
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Returns the first name of this Person.
     * @return the first name
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * Sets the first name of this Person.
     * @param firstName the first name to set
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * Returns the last name of this Person.
     * @return the last name
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * Sets the last name of this Person.
     * @param lastName the last name to set
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * Returns the Grid this Person belongs to.
     * @return the associated Grid
     */
    public Grid getGrid() {
        return grid;
    }

    /**
     * Sets the Grid this Person belongs to.
     * @param grid the Grid to associate
     */
    public void setGrid(Grid grid) {
        this.grid = grid;
    }

    /**
     * Returns the list of Sensors owned by this Person.
     * @return the list of owned Sensors
     */
    public List<Sensor> getSensors() {
        return sensors;
    }

    /**
     * Sets the list of Sensors owned by this Person.
     * @param sensors the list of Sensors to set
     */
    public void setSensors(List<Sensor> sensors) {
        this.sensors = sensors;
    }

    /**
     * Returns a JSON representation of this Person.
     * Includes the associated Grid ID and the list of owned Sensor IDs.
     * @return a JsonObject containing all Person fields
     */
    public JsonObject toJSON() {
        JsonObject result = new JsonObject();
        result.put("id", id);
        result.put("first_name", firstName);
        result.put("last_name", lastName);
        if (grid != null) {
            result.put("grid", grid.getId());
        }
        JsonArray sensorIds = new JsonArray();
        for (Sensor s : sensors) {
            sensorIds.add(s.getId());
        }
        result.put("owned_sensors", sensorIds);
        return result;
    }
}