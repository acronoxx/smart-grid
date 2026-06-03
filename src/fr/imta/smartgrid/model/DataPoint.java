package fr.imta.smartgrid.model;

import io.vertx.core.json.JsonObject;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * JPA entity representing a single data point recorded by a Measurement.
 * Stores a timestamped value associated with a Measurement.
 * Mapped to the "datapoint" table.
 */
@Entity
@Table(name = "datapoint")
public class DataPoint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private long timestamp;

    private double value;

    @ManyToOne
    @JoinColumn(name = "measurement")
    private Measurement measurement;

    /**
     * Returns a JSON representation of this DataPoint.
     * @return a JsonObject containing id, measurement id, timestamp and value
     */
    public JsonObject toJSON() {
        JsonObject json = new JsonObject();
        json.put("id", id);
        json.put("measurement", measurement != null ? measurement.getId() : null);
        json.put("timestamp", timestamp);
        json.put("value", value);
        return json;
    }

    /**
     * Returns the ID of this DataPoint.
     * @return the ID
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the ID of this DataPoint.
     * @param id the ID to set
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Returns the timestamp of this DataPoint.
     * @return the timestamp as a Unix epoch (milliseconds)
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Sets the timestamp of this DataPoint.
     * @param timestamp the timestamp as a Unix epoch (milliseconds)
     */
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Returns the measured value of this DataPoint.
     * @return the value
     */
    public double getValue() {
        return value;
    }

    /**
     * Sets the measured value of this DataPoint.
     * @param value the value to set
     */
    public void setValue(double value) {
        this.value = value;
    }

    /**
     * Returns the Measurement this DataPoint belongs to.
     * @return the associated Measurement
     */
    public Measurement getMeasurement() {
        return measurement;
    }

    /**
     * Sets the Measurement this DataPoint belongs to.
     * @param measurement the Measurement to associate
     */
    public void setMeasurement(Measurement measurement) {
        this.measurement = measurement;
    }
}