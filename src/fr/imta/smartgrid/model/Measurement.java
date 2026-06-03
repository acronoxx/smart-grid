package fr.imta.smartgrid.model;

import java.util.ArrayList;
import java.util.List;

import io.vertx.core.json.JsonObject;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

/**
 * JPA entity representing a Measurement tracked by a Sensor.
 * A Measurement has a name, a unit, and a list of DataPoints recording its values over time.
 * Mapped to the "measurement" table.
 */
@Entity
@Table(name = "measurement")
public class Measurement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String unit;

    private String name;

    @ManyToOne
    @JoinColumn(name = "sensor")
    private Sensor sensor;

    @OneToMany(mappedBy = "measurement")
    private List<DataPoint> datapoints = new ArrayList<>();

    /**
     * Returns a JSON representation of this Measurement.
     * @return a JsonObject containing id, sensor id, name and unit
     */
    public JsonObject toJSON() {
        JsonObject result = new JsonObject();
        result.put("id", id);
        result.put("sensor", sensor != null ? sensor.getId() : null);
        result.put("name", name);
        result.put("unit", unit);
        return result;
    }

    /**
     * Returns the ID of this Measurement.
     * @return the ID
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the ID of this Measurement.
     * @param id the ID to set
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Returns the unit of this Measurement.
     * @return the unit as a String (e.g. "kW", "°C")
     */
    public String getUnit() {
        return unit;
    }

    /**
     * Sets the unit of this Measurement.
     * @param unit the unit to set
     */
    public void setUnit(String unit) {
        this.unit = unit;
    }

    /**
     * Returns the name of this Measurement.
     * @return the name as a String (e.g. "power", "temperature")
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of this Measurement.
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the Sensor this Measurement belongs to.
     * @return the associated Sensor
     */
    public Sensor getSensor() {
        return sensor;
    }

    /**
     * Sets the Sensor this Measurement belongs to.
     * @param sensor the Sensor to associate
     */
    public void setSensor(Sensor sensor) {
        this.sensor = sensor;
    }

    /**
     * Returns the list of DataPoints recorded for this Measurement.
     * @return the list of DataPoints
     */
    public List<DataPoint> getDatapoints() {
        return datapoints;
    }

    /**
     * Sets the list of DataPoints for this Measurement.
     * @param datapoints the list of DataPoints to set
     */
    public void setDatapoints(List<DataPoint> datapoints) {
        this.datapoints = datapoints;
    }
}