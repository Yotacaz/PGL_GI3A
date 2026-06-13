package fr.cy.model.simulation;

import java.io.Serializable;
import java.util.Objects;

/**
 * A singleton class providing shared configuration settings for simulation-wide
 * behavior.
 * <p>
 * This class manages parameters such as the duration of a simulation tick and
 * the speed multiplier used to accelerate or decelerate simulation updates.
 * </p>
 */
public class SimulationSettings implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final double DEFAULT_TICK_DURATION = 1.0;
    private static final double DEFAULT_SPEED_MULTIPLIER = 1.0;
    private static final double[] SPEED_LEVELS = { 0.5, 0.75, 1.0, 1.5, 2.0, 3.0, 5.0, 10.0 };
    private static final int DEFAULT_SPEED_LEVEL_INDEX = 2; // 1.0x

    private double tickDuration = DEFAULT_TICK_DURATION;
    private double speedMultiplier = DEFAULT_SPEED_MULTIPLIER;
    private int speedLevelIndex = DEFAULT_SPEED_LEVEL_INDEX;

    /** Singleton instance. */
    private static SimulationSettings instance = new SimulationSettings();

    /**
     * Private constructor to enforce Singleton pattern.
     */
    private SimulationSettings() {
    }

    /**
     * Retrieves the global {@code SimulationSettings} instance.
     * 
     * @return The singleton instance.
     */
    public static SimulationSettings getInstance() {
        return instance;
    }

    /** @return The duration of a single tick in seconds. */
    public double getTickDuration() {
        return tickDuration;
    }

    /**
     * Sets the tick duration.
     * 
     * @param tickDuration Must be strictly positive.
     * @throws IllegalArgumentException if value is &lt;= 0.
     */
    public void setTickDuration(double tickDuration) {
        if (tickDuration <= 0) {
            throw new IllegalArgumentException("tickDuration must be strictly positive");
        }
        this.tickDuration = tickDuration;
    }

    /** @return The current simulation speed multiplier. */
    public double getSpeedMultiplier() {
        return speedMultiplier;
    }

    /**
     * Sets the speed multiplier manually within the predefined bounds.
     * 
     * @param speedMultiplier The multiplier to apply.
     * @throws IllegalArgumentException if the multiplier is out of valid range.
     */
    public void setSpeedMultiplier(double speedMultiplier) {
        if (speedMultiplier < SPEED_LEVELS[0] || speedMultiplier > SPEED_LEVELS[SPEED_LEVELS.length - 1]) {
            throw new IllegalArgumentException(
                    "speedMultiplier must be between " + SPEED_LEVELS[0] + " and "
                            + SPEED_LEVELS[SPEED_LEVELS.length - 1]);
        }
        this.speedMultiplier = speedMultiplier;
    }

    /** @return The default speed multiplier (1.0). */
    public static double getDefaultSpeedMultiplier() {
        return DEFAULT_SPEED_MULTIPLIER;
    }

    /** @return The array of available speed levels. */
    public static double[] getSpeedLevels() {
        return SPEED_LEVELS;
    }

    /**
     * Increments the simulation speed to the next level in {@link #SPEED_LEVELS}.
     */
    public void increaseSpeedLevel() {
        if (speedLevelIndex < SPEED_LEVELS.length - 1) {
            speedLevelIndex++;
            speedMultiplier = SPEED_LEVELS[speedLevelIndex];
        }
    }

    /**
     * Decrements the simulation speed to the previous level in
     * {@link #SPEED_LEVELS}.
     */
    public void decreaseSpeedLevel() {
        if (speedLevelIndex > 0) {
            speedLevelIndex--;
            speedMultiplier = SPEED_LEVELS[speedLevelIndex];
        }
    }

    /** @return The index of the current speed level. */
    public int getSpeedLevelIndex() {
        return speedLevelIndex;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        SimulationSettings other = (SimulationSettings) obj;
        return Double.compare(other.tickDuration, tickDuration) == 0 &&
                Double.compare(other.speedMultiplier, speedMultiplier) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(tickDuration, speedMultiplier);
    }

    @Override
    public String toString() {
        return "SimulationSettings{" +
                "tickDuration=" + tickDuration +
                ", speedMultiplier=" + speedMultiplier +
                '}';
    }
}