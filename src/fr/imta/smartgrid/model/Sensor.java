package fr.imta.smartgrid.model;

import java.util.ArrayList;
import java.util.List;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Abstract JPA base entity for all Sensor types.
 * Uses joined table inheritance — concrete subclasses are Producer and Consumer.
 * A Sensor belongs to a Grid, can have multiple owners (Person) and Measurements.
 * Mapped to the "sensor" table.
 */
@Entity
@Table(name = "sensor")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Sensor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String name;

    private String description;

    @ManyToOne
    @JoinColumn(name = "grid")
    private Grid grid;

    @ManyToMany(mappedBy = "sensors")
    private List<Person> owners = new ArrayList<>();

    @OneToMany(mappedBy = "sensor")
    private List<Measurement> measurements = new ArrayList<>();

    /**
     * Returns a JSON representation of this Sensor.
     * Includes the associated Grid ID, owner IDs, and Measurement IDs.
     * Subclasses should call super.toJSON() and extend the result.
     * @return a JsonObject containing all base Sensor fields
     */
    public JsonObject toJSON() {
        JsonObject res = new JsonObject();
        res.put("id", id);
        res.put("name", name);
        res.put("description", description);
        res.put("grid", grid != null ? grid.getId() : null);
        JsonArray measurementIds = new JsonArray();
        for (Measurement m : measurements) {
            measurementIds.add(m.getId());
        }
        res.put("available_measurements", measurementIds);
        JsonArray ownerIds = new JsonArray();
        for (Person p : owners) {
            ownerIds.add(p.getId());
        }
        res.put("owners", ownerIds);
        return res;
    }

    /**
     * Returns the ID of this Sensor.
     * @return the ID
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the ID of this Sensor.
     * @param id the ID to set
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Returns the name of this Sensor.
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of this Sensor.
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the description of this Sensor.
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description of this Sensor.
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Returns the Grid this Sensor belongs to.
     * @return the associated Grid
     */
    public Grid getGrid() {
        return grid;
    }

    /**
     * Sets the Grid this Sensor belongs to.
     * @param grid the Grid to associate
     */
    public void setGrid(Grid grid) {
        this.grid = grid;
    }

    /**
     * Returns the list of Persons who own this Sensor.
     * @return the list of owners
     */
    public List<Person> getOwners() {
        return owners;
    }

    /**
     * Sets the list of Persons who own this Sensor.
     * @param owners the list of owners to set
     */
    public void setOwners(List<Person> owners) {
        this.owners = owners;
    }

    /**
     * Returns the list of Measurements associated with this Sensor.
     * @return the list of Measurements
     */
    public List<Measurement> getMeasurements() {
        return measurements;
    }

    /**
     * Sets the list of Measurements associated with this Sensor.
     * @param measurements the list of Measurements to set
     */
    public void setMeasurements(List<Measurement> measurements) {
        this.measurements = measurements;
    }
}