package fr.imta.smartgrid.model;

/**
 * Enumeration of the available Sensor types in the SmartGrid system.
 */
public enum SensorType {
    /** Solar panel sensor, produces energy from sunlight. */
    SOLAR_PANEL,

    /** Wind turbine sensor, produces energy from wind. */
    WIND_TURBINE,

    /** Electric vehicle charger sensor, consumes energy. */
    EV_CHARGER
}