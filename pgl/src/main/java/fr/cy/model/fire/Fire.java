package fr.cy.model.fire;

import java.io.Serializable;

/**
 * Represents a dynamic fire instance in the simulation.
 * Each Fire keeps track of its current intensity, smoke level and spread rate.
 * The object also records how long it has been burning in seconds.
 */
public class Fire implements Serializable {
    private static final long serialVersionUID = 1L;

    // --- CONSTANTES (Remplacement des valeurs magiques) ---
    /**
     * Multiplicateur de croissance de l'intensité par seconde (1.35 = +35% par sec)
     */
    private static final double INTENSITY_GROWTH_PER_SEC = 1.05;

    /**
     * Multiplicateur de croissance de la fumée par seconde (1.20 = +20% par sec)
     */
    private static final double SMOKE_GROWTH_PER_SEC = 1.05;

    /** Sert a faire peur aux Agents, influence le stress */
    private double intensity;

    // TODO: prendre en compte smoke dans la vision ou l'étouffement des agents
    private double smokeLevel;

    private double spreadRate;

    /** Temps total de combustion en secondes */
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
     * Advance the fire based on the actual time elapsed.
     * Uses exponential growth scaled by the tick duration to ensure the fire
     * grows at the exact same speed regardless of the simulation framerate.
     * * @param tickDuration le temps écoulé depuis la dernière frame (en secondes)
     */
    public void update(double tickDuration) {
        this.burningTime += tickDuration;

        // Croissance exponentielle lissée par le temps réel
        this.intensity *= Math.pow(INTENSITY_GROWTH_PER_SEC, tickDuration);
        this.smokeLevel *= Math.pow(SMOKE_GROWTH_PER_SEC, tickDuration);
    }

    public void setIntensity(double intensity) {
        this.intensity = intensity;
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
     * @return the total time this fire has been burning (in seconds)
     */
    public double getBurningTime() {
        return burningTime;
    }

    public double getDamage(double duration) {
        // Simple formule de dégâts basée sur l'intensité et le temps de combustion
        return duration * intensity * smokeLevel * (1 + burningTime / 60.0); // +100% de dégâts après 1 minute
    }
}