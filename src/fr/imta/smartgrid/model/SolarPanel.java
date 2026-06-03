package fr.imta.smartgrid.model;

import io.vertx.core.json.JsonObject;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;

/**
 * JPA entity representing a Solar Panel sensor.
 * A Producer that generates energy from sunlight.
 * Mapped to the "solar_panel" table using joined inheritance.
 */
@Entity
@Table(name = "solar_panel")
@PrimaryKeyJoinColumn(name = "id")
public class SolarPanel extends Producer {

    private float efficiency;

    /**
     * Returns a JSON representation of this SolarPanel, extending the parent Producer JSON.
     * @return a JsonObject containing all SolarPanel fields
     */
    public JsonObject toJSON() {
        JsonObject res = super.toJSON();
        res.put("kind", "SolarPanel");
        res.put("efficiency", efficiency);
        return res;
    }

    /**
     * Returns the efficiency of this SolarPanel.
     * @return the efficiency as a float between 0 and 1
     */
    public float getEfficiency() {
        return efficiency;
    }

    /**
     * Sets the efficiency of this SolarPanel.
     * @param efficiency the efficiency to set, between 0 and 1
     */
    public void setEfficiency(float efficiency) {
        this.efficiency = efficiency;
    }
}