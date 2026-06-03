package fr.imta.smartgrid.model;

import io.vertx.core.json.JsonObject;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;

/**
 * Abstract JPA entity representing a Producer sensor.
 * A Producer is a Sensor that generates energy (e.g. SolarPanel, WindTurbine).
 * Mapped to the "producer" table using joined inheritance.
 */
@Entity
@Table(name = "producer")
@PrimaryKeyJoinColumn(name = "id")
public abstract class Producer extends Sensor {

    @Column(name = "power_source")
    private String powerSource;

    /**
     * Returns a JSON representation of this Producer, extending the parent Sensor JSON.
     * @return a JsonObject containing all Producer fields
     */
    public JsonObject toJSON() {
        JsonObject res = super.toJSON();
        res.put("power_source", powerSource);
        return res;
    }

    /**
     * Returns the power source of this Producer.
     * @return the power source as a String (e.g. "solar", "wind")
     */
    public String getPowerSource() {
        return powerSource;
    }

    /**
     * Sets the power source of this Producer.
     * @param powerSource the power source to set
     */
    public void setPowerSource(String powerSource) {
        this.powerSource = powerSource;
    }
}