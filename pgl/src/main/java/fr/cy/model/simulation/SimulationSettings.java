package fr.cy.model.simulation;

import java.util.Objects;

/** Shared settings for simulation-wide behavior. */
public class SimulationSettings {
    private static final double DEFAULT_TICK_DURATION = 1.0;

    private double tickDuration = DEFAULT_TICK_DURATION;

    public double getTickDuration() {
        return tickDuration;
    }

    public void setTickDuration(double tickDuration) {
        if (tickDuration <= 0) {
            throw new IllegalArgumentException("tickDuration must be strictly positive");
        }
        this.tickDuration = tickDuration;
    }

    public static double getDefaultTickDuration() {
        return DEFAULT_TICK_DURATION;
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
        return Double.compare(other.tickDuration, tickDuration) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(tickDuration);
    }

    @Override
    public String toString() {
        return "SimulationSettings{" +
                "tickDuration=" + tickDuration +
                '}';
    }
}