package fr.cy.model.fire;

import java.io.Serializable;

/**
 * Represents a dynamic fire instance in the simulation.
 * Each Fire keeps track of its current intensity, smoke level and spread rate.
 * The object also records how long it has been burning in seconds.
 */
public class Fire implements Serializable {
    private static final long serialVersionUID = 1L;

    // --- CONSTANTS (Replace magic numbers) ---
    /**
     * Intensity growth multiplier per second (1.35 = +35% per sec)
     */
    private static final double INTENSITY_GROWTH_PER_SEC = 1.05;

    /**
     * Multiplicateur de croissance de la fumée par seconde (1.20 = +20% par sec)
     */
    private static final double SMOKE_GROWTH_PER_SEC = 1.05;

    /** Used to intimidate agents; influences stress */
    private double intensity;

    // TODO: account for smoke in agents' vision or suffocation
    private double smokeLevel;

    private double spreadRate;

    /** Total burning time in seconds */
    private double burningTime;

    /**
     * Create a new Fire.
     *
     * @param intensity  initial intensity of the fire (higher means stronger fire)
     * @param smokeLevel initial smoke level produced by the fire
     * @param spreadRate rate at which the fire spreads to neighbouring elements
     */
    public Fire(double intensity, double smokeLevel, double spreadRate) {
        this.intensity = intensity;
        this.smokeLevel = smokeLevel;
        this.spreadRate = spreadRate;

        this.burningTime = 0.0;
    }

    /**
     * Advances the fire state based on the actual time elapsed.
     * Uses exponential growth scaled by the tick duration to ensure the fire
     * grows at the exact same speed regardless of the simulation framerate.
     *
     * @param tickDuration the time elapsed since the last frame, in seconds
     */
    public void update(double tickDuration) {
        this.burningTime += tickDuration;

        // Croissance exponentielle lissée par le temps réel
        this.intensity *= Math.pow(INTENSITY_GROWTH_PER_SEC, tickDuration);
        this.smokeLevel *= Math.pow(SMOKE_GROWTH_PER_SEC, tickDuration);
    }

    /**
     * Sets the intensity of the fire.
     *
     * @param intensity the new intensity value
     */
    public void setIntensity(double intensity) {
        this.intensity = intensity;
    }

    /**
     * Returns the current intensity of the fire.
     *
     * @return the current intensity of the fire
     */
    public double getIntensity() {
        return intensity;
    }

    /**
     * Returns the current smoke level produced by the fire.
     *
     * @return the current smoke level produced by the fire
     */
    public double getSmokeLevel() {
        return smokeLevel;
    }

    /**
     * Returns the spread rate of the fire used by the simulation to propagate fire.
     *
     * @return the spread rate of the fire
     */
    public double getSpreadRate() {
        return spreadRate;
    }

    /**
     * Returns the total time this fire has been burning.
     *
     * @return the total burning time in seconds
     */
    public double getBurningTime() {
        return burningTime;
    }

    /**
     * Computes the damage dealt to an agent based on current fire properties and exposure duration.
     *
     * @param duration the exposure duration in seconds
     * @return the damage amount for the given exposure duration
     */
    public double getDamageForAgent(double duration) {
        // Simple damage formula based on intensity and burn duration
        return duration * intensity * smokeLevel * (1 + burningTime / 60.0); // +100% of damage after 1 minute
    }
}