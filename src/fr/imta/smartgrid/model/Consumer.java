package fr.imta.smartgrid.model;

import io.vertx.core.json.JsonObject;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;

/**
 * Abstract JPA entity representing a Consumer sensor.
 * A Consumer is a Sensor that consumes energy (e.g. EVCharger).
 * Mapped to the "consumer" table using joined inheritance.
 */
@Entity
@Table(name = "consumer")
@PrimaryKeyJoinColumn(name = "id")
public abstract class Consumer extends Sensor {

    @Column(name = "max_power")
    private Double maxPower;

    /**
     * Returns a JSON representation of this Consumer, extending the parent Sensor JSON.
     * @return a JsonObject containing all Consumer fields
     */
    public JsonObject toJSON() {
        JsonObject res = super.toJSON();
        res.put("max_power", maxPower);
        return res;
    }

    /**
     * Returns the maximum power consumption of this Consumer.
     * @return the maximum power in kW
     */
    public Double getMaxPower() {
        return maxPower;
    }

    /**
     * Sets the maximum power consumption of this Consumer.
     * @param maxPower the maximum power in kW
     */
    public void setMaxPower(Double maxPower) {
        this.maxPower = maxPower;
    }
}