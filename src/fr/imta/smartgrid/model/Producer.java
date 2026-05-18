package fr.imta.smartgrid.model;

import io.vertx.core.json.JsonObject;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;

@Entity
@Table(name = "producer")
@PrimaryKeyJoinColumn(name = "id")
public abstract class Producer extends Sensor {
    @Column(name = "power_source")
    private String powerSource;

    public JsonObject toJSON() {
        JsonObject res = super.toJSON();

        res.put("power_source", powerSource);

        return res;
    }

    public String getPowerSource() {
        return powerSource;
    }

    public void setPowerSource(String powerSource) {
        this.powerSource = powerSource;
    }
}
