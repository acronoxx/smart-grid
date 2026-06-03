package fr.imta.smartgrid.model;

import io.vertx.core.json.JsonObject;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;

/**
 * JPA entity representing an EV Charger sensor.
 * A Consumer that charges electric vehicles.
 * Mapped to the "ev_charger" table using joined inheritance.
 */
@Entity
@Table(name = "ev_charger")
@PrimaryKeyJoinColumn(name = "id")
public class EVCharger extends Consumer {

    private int voltage;
    private int maxAmp;

    @Column(name = "connector_type")
    private String type;

    /**
     * Returns a JSON representation of this EVCharger, extending the parent Consumer JSON.
     * @return a JsonObject containing all EVCharger fields
     */
    public JsonObject toJSON() {
        JsonObject res = super.toJSON();
        res.put("kind", "EVCharger");
        res.put("voltage", voltage);
        res.put("maxAmp", maxAmp);
        res.put("type", type);
        return res;
    }

    /**
     * Returns the voltage of this EVCharger.
     * @return the voltage in volts
     */
    public int getVoltage() {
        return voltage;
    }

    /**
     * Sets the voltage of this EVCharger.
     * @param voltage the voltage in volts
     */
    public void setVoltage(int voltage) {
        this.voltage = voltage;
    }

    /**
     * Returns the maximum current of this EVCharger.
     * @return the maximum current in amperes
     */
    public int getMaxAmp() {
        return maxAmp;
    }

    /**
     * Sets the maximum current of this EVCharger.
     * @param maxAmp the maximum current in amperes
     */
    public void setMaxAmp(int maxAmp) {
        this.maxAmp = maxAmp;
    }

    /**
     * Returns the connector type of this EVCharger.
     * @return the connector type as a String
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the connector type of this EVCharger.
     * @param type the connector type to set
     */
    public void setType(String type) {
        this.type = type;
    }
}