package fr.cy.model.fire;


import java.io.Serializable;
/** TODO: VALEURS MAGIQUES: A RETRAVAILLER */

/**
 * Represents a dynamic fire instance in the simulation.
 * Each Fire keeps track of its current intensity, smoke level and spread rate.
 * The object also records how many update ticks it has been burning. The
 * {@link #update()} method advances the fire by one tick and modifies the
 * intensity and smoke level according to fixed multipliers.
 */
public class Fire implements Serializable {
    private static final long serialVersionUID = 1L;
    private double intensity;
    private double smokeLevel;
    private double spreadRate;

    private int burningTicks;

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

        this.burningTicks = 0;
    }

    /**
     * Advance the fire by one simulation tick.
     * This increments the burning tick counter and increases the intensity and
     * smoke level by fixed multipliers to simulate growth over time.
     */
    public void update() {
        burningTicks++;

        intensity *= 1.05;
        smokeLevel *= 1.03;
    }

    /**
     * @return the current intensity of the fire
     */
    public double getIntensity() {
        return intensity;
    }

    /**
     * @return the current smoke level produced by the fire
     */
    public double getSmokeLevel() {
        return smokeLevel;
    }

    /**
     * @return the spread rate of the fire (used by the simulation to propagate
     *         fire)
     */
    public double getSpreadRate() {
        return spreadRate;
    }

    /**
     * @return the number of update ticks this fire has been burning
     */
    public int getBurningTicks() {
        return burningTicks;
    }
}
