package fr.imta.smartgrid.model;

import io.vertx.core.json.JsonObject;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;

/**
 * JPA entity representing a Wind Turbine sensor.
 * A Producer that generates energy from wind.
 * Mapped to the "wind_turbine" table using joined inheritance.
 */
@Entity
@Table(name = "wind_turbine")
@PrimaryKeyJoinColumn(name = "id")
public class WindTurbine extends Producer {

    private Double height;
    private Double bladeLength;

    /**
     * Returns a JSON representation of this WindTurbine, extending the parent Producer JSON.
     * @return a JsonObject containing all WindTurbine fields
     */
    public JsonObject toJSON() {
        JsonObject res = super.toJSON();
        res.put("kind", "WindTurbine");
        res.put("height", height);
        res.put("blade_length", bladeLength);
        return res;
    }

    /**
     * Returns the height of this WindTurbine.
     * @return the height in meters
     */
    public Double getHeight() {
        return height;
    }

    /**
     * Sets the height of this WindTurbine.
     * @param height the height in meters
     */
    public void setHeight(Double height) {
        this.height = height;
    }

    /**
     * Returns the blade length of this WindTurbine.
     * @return the blade length in meters
     */
    public Double getBladeLength() {
        return bladeLength;
    }

    /**
     * Sets the blade length of this WindTurbine.
     * @param bladeLength the blade length in meters
     */
    public void setBladeLength(Double bladeLength) {
        this.bladeLength = bladeLength;
    }
}