package fr.imta.smartgrid.model;

import java.util.ArrayList;
import java.util.List;

import io.vertx.core.json.JsonObject;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

/**
 * JPA entity representing an energy grid.
 * A Grid groups Persons and Sensors together.
 * Mapped to the "grid" table.
 */
@Entity
@Table(name = "grid")
public class Grid {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String name;

    private String description;

    @OneToMany(mappedBy = "grid")
    private List<Person> persons = new ArrayList<>();

    @OneToMany(mappedBy = "grid")
    private List<Sensor> sensors = new ArrayList<>();

    /**
     * Returns the ID of this Grid.
     * @return the ID
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the ID of this Grid.
     * @param id the ID to set
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Returns the name of this Grid.
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of this Grid.
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the description of this Grid.
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description of this Grid.
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Returns the list of Persons associated with this Grid.
     * @return the list of Persons
     */
    public List<Person> getPersons() {
        return persons;
    }

    /**
     * Sets the list of Persons associated with this Grid.
     * @param persons the list of Persons to set
     */
    public void setPersons(List<Person> persons) {
        this.persons = persons;
    }

    /**
     * Returns the list of Sensors associated with this Grid.
     * @return the list of Sensors
     */
    public List<Sensor> getSensors() {
        return sensors;
    }

    /**
     * Sets the list of Sensors associated with this Grid.
     * @param sensors the list of Sensors to set
     */
    public void setSensors(List<Sensor> sensors) {
        this.sensors = sensors;
    }

    /**
     * Returns a JSON representation of this Grid.
     * Includes the list of associated Person IDs and Sensor IDs.
     * @return a JsonObject containing all Grid fields
     */
    public JsonObject toJSON() {
        JsonObject result = new JsonObject();
        result.put("id", id);
        result.put("name", name);
        result.put("description", description);
        List<Integer> personIds = new ArrayList<>();
        for (Person p : persons) {
            personIds.add(p.getId());
        }
        result.put("users", personIds);
        List<Integer> sensorIds = new ArrayList<>();
        for (Sensor s : sensors) {
            sensorIds.add(s.getId());
        }
        result.put("sensors", sensorIds);
        return result;
    }
}