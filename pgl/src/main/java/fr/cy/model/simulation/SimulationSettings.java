package fr.cy.model.simulation;

import java.io.Serializable;
import java.util.Objects;

/** Shared settings for simulation-wide behavior. */
public class SimulationSettings implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final double DEFAULT_TICK_DURATION = 1.0;
    private static final double DEFAULT_SPEED_MULTIPLIER = 1.0;
    private static final double[] SPEED_LEVELS = { 0.5, 0.75, 1.0, 1.5, 2.0, 3.0, 5.0, 10.0 };
    private static final int DEFAULT_SPEED_LEVEL_INDEX = 2; // 1.0x

    private double tickDuration = DEFAULT_TICK_DURATION;
    private double speedMultiplier = DEFAULT_SPEED_MULTIPLIER;
    private int speedLevelIndex = DEFAULT_SPEED_LEVEL_INDEX;

    private static SimulationSettings instance = new SimulationSettings();

    private SimulationSettings() {
        // Private constructor to prevent instantiation without using the singleton instance
    }

    public static SimulationSettings getInstance() {
        return instance;
    }

    public double getTickDuration() {
        return tickDuration;
    }

    public void setTickDuration(double tickDuration) {
        if (tickDuration <= 0) {
            throw new IllegalArgumentException("tickDuration must be strictly positive");
        }
        this.tickDuration = tickDuration;
    }

    public double getSpeedMultiplier() {
        return speedMultiplier;
    }

    public void setSpeedMultiplier(double speedMultiplier) {
        if (speedMultiplier < SPEED_LEVELS[0] || speedMultiplier > SPEED_LEVELS[SPEED_LEVELS.length - 1]) {
            throw new IllegalArgumentException(
                    "speedMultiplier must be between " + SPEED_LEVELS[0] + " and "
                            + SPEED_LEVELS[SPEED_LEVELS.length - 1]);
        }
        this.speedMultiplier = speedMultiplier;
    }

    public static double getDefaultSpeedMultiplier() {
        return DEFAULT_SPEED_MULTIPLIER;
    }

    public static double[] getSpeedLevels() {
        return SPEED_LEVELS;
    }

    public void increaseSpeedLevel() {
        if (speedLevelIndex < SPEED_LEVELS.length - 1) {
            speedLevelIndex++;
            speedMultiplier = SPEED_LEVELS[speedLevelIndex];
        }
    }

    public void decreaseSpeedLevel() {
        if (speedLevelIndex > 0) {
            speedLevelIndex--;
            speedMultiplier = SPEED_LEVELS[speedLevelIndex];
        }
    }

    public int getSpeedLevelIndex() {
        return speedLevelIndex;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
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