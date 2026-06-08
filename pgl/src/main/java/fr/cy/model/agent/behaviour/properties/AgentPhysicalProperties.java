package fr.cy.model.agent.behaviour.properties;

import java.io.Serializable;

/**
 * Represents the physical properties and attributes of an agent.
 * 
 * <p>This class encapsulates all the physical characteristics of an agent
 * that affect its movement and interaction with the environment, including
 * speed, health, and spatial requirements.</p>
 */
public class AgentPhysicalProperties implements Serializable {
    private static final long serialVersionUID = 1L;
    /** Maximum speed of the agent, in units per time step */
    private double maxSpeed;

    private double maxHealth;
    /** Current health level of the agent, between 0 and 100 */
    private double health;

    /** Surface area taken by the agent, used to calculate crowding effects */
    private double surfaceAreaTakenByAgent = 0.5;

    public AgentPhysicalProperties(double maxSpeed, double maxHealth, double health, double surfaceAreaTakenByAgent) {
        this.maxSpeed = maxSpeed;
        this.maxHealth = maxHealth;
        if (health < 0 || health > maxHealth) {
            throw new IllegalArgumentException("Health must be between 0 and maxHealth");
        }
        this.health = health;
        this.surfaceAreaTakenByAgent = surfaceAreaTakenByAgent;
    }

    /**
     * Gets the maximum speed of the agent.
     * 
     * @return the maximum speed in units per time step
     */
    public double getMaxSpeed() {
        return maxSpeed;
    }

    /**
     * Gets the surface area taken by the agent.
     * 
     * @return the surface area, used to calculate crowding effects
     */
    public double getSurfaceAreaTakenByAgent() {
        return surfaceAreaTakenByAgent;
    }

    /**
     * Sets the current health level of the agent.
     * 
     * @param health the new health level (must be between 0 and maxHealth)
     * @throws IllegalArgumentException if health is outside valid range
     */
    public void setHealth(double health) {
        if (health < 0 || health > maxHealth) {
            throw new IllegalArgumentException("Health must be between 0 and maxHealth");
        }
        this.health = health;
    }

    /**
     * Gets the current health level of the agent.
     * 
     * @return the current health level
     */
    public double getHealth() {
        return health;
    }

    public double getMaxHealth() {
        return maxHealth;
    }

    public void decreaseHealth(double amount) {
        setHealth(Math.max(0, getHealth() - amount));
    }

    public void restoreHealth(double amount) {
        setHealth(Math.min(maxHealth, getHealth() + amount));
    }

    public void kill() {
        setHealth(0);
    }

    public double getHealthPercentage() {
        return (double) health / maxHealth;
    }

    public boolean isAlive() {
        return health > 0;
    }

}
