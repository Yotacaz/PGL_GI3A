package fr.cy.model.agent;

import java.io.Serializable;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

import fr.cy.model.agent.behaviour.decisions.AgentPossibleNodeDecision;
import fr.cy.model.agent.behaviour.decisions.AgentPossibleEdgeDecision;

/**
 * Singleton class to hold global settings for agents, such as decision-making
 * factors and speed parameters. This allows for centralized configuration of
 * agent behavior and easy adjustments without modifying individual agent code.
 */
public class AgentSettings implements Serializable {
    private static final long serialVersionUID = 1L;

    private AgentSettings() {
        // Private constructor to prevent instantiation
    }

    /** The singleton instance of AgentSettings */
    private static final AgentSettings instance = new AgentSettings();

    /**
     * Gets the singleton instance of AgentSettings.
     * 
     * @return the singleton instance of AgentSettings
     */
    public static AgentSettings getInstance() {
        return instance;
    }

    /**
     * Immutable map of default decision-making factors for each possible agent decision type.
     * These factors influence how likely an agent is to choose each type of decision.
     */
    private final Map<AgentPossibleNodeDecision, Double> defaultNodeDecisionMakingFactors = new EnumMap<>(
            AgentPossibleNodeDecision.class);

    /**
     * Immutable map of default decision-making factors for each possible edge decision type.
     * These factors influence how likely an agent is to choose each type of edge decision.
     */
    private final Map<AgentPossibleEdgeDecision, Double> defaultEdgeDecisionMakingFactors = new EnumMap<>(
            AgentPossibleEdgeDecision.class);
    {
        // Initialize decision-making factors for each decision type
        defaultNodeDecisionMakingFactors.put(AgentPossibleNodeDecision.FOLLOW_CROWD, 2.0);
        defaultNodeDecisionMakingFactors.put(AgentPossibleNodeDecision.FOLLOW_LESS_CROWDED_PATH, 0.1);
        defaultNodeDecisionMakingFactors.put(AgentPossibleNodeDecision.FOLLOW_RECOMMENDED_PATH, 1.5);
        defaultNodeDecisionMakingFactors.put(AgentPossibleNodeDecision.FOLLOW_SHORTEST_PATH, 0.05);
        defaultNodeDecisionMakingFactors.put(AgentPossibleNodeDecision.NICEST_PATH, 0.2);
        defaultNodeDecisionMakingFactors.put(AgentPossibleNodeDecision.RANDOM, 0.5);
        defaultNodeDecisionMakingFactors.put(AgentPossibleNodeDecision.CONTINUE_LAST_ACTION, 2.5);
        defaultNodeDecisionMakingFactors.put(AgentPossibleNodeDecision.WAIT, 1.0);

        defaultEdgeDecisionMakingFactors.put(AgentPossibleEdgeDecision.CONTINUE, 5.0); //prefer continuing on the current edge
        defaultEdgeDecisionMakingFactors.put(AgentPossibleEdgeDecision.BACKTRACK, 1.0);
        defaultEdgeDecisionMakingFactors.put(AgentPossibleEdgeDecision.WAIT_BEFORE_ACTION, 0.05);
    }

    /* Default speeds and tolerances for agents, can be overridden by individual agents  */
    /** Walking speed for agents in m/s */
    private double WALKING_SPEED = 1.5;
    /** Running speed for agents in m/s */
    private double RUNNING_SPEED = 3.0;
    /** Maximum running speed for agents, derived from RUNNING_SPEED in m/s */
    private double MAX_RUNNING_SPEED = RUNNING_SPEED * 1.5;
    /** Reduction factor applied to speed when walking instead of running, derived from WALKING_SPEED and RUNNING_SPEED */
    private double WALK_SPEED_REDUCTION_FACTOR = WALKING_SPEED / RUNNING_SPEED;

    /** Minimum max speed for every agent in m/s (ie: minimum speed at which an agent goes when running)*/
    private double MIN_AGENT_MAX_SPEED = 1.0;

    /** Minimum stress tolerance for every agent between 0 and 1*/
    private double MIN_STRESS_TOLERANCE = 0.0;
    /** Maximum stress tolerance for every agent between 0 and 1*/
    private double MAX_STRESS_TOLERANCE = 1.0;
    /** Minimum crowding tolerance for every agent between 0 and 1*/
    private double MIN_CROWDING_TOLERANCE = 0.0;
    /** Maximum crowding tolerance for every agent between 0 and 1*/
    private double MAX_CROWDING_TOLERANCE = 1.0;
    /** Minimum tendency to repeat the last decision */
    private double MIN_REPEAT_LAST_DECISION_TENDENCY = 0.875;
    /** Maximum tendency to repeat the last decision */
    private double MAX_REPEAT_LAST_DECISION_TENDENCY = 1.875;
    /** Minimum base own decision-making factor for every agent */
    private double MIN_BASE_OWN_DECISION_MAKING_FACTOR = 0.0;
    /** Maximum base own decision-making factor for every agent */
    private double MAX_BASE_OWN_DECISION_MAKING_FACTOR = 1.0;
    /** Minimum health for every agent */
    private int MIN_HEALTH = 75;
    /** Maximum health for every agent */
    private int MAX_HEALTH = 100;
    /** Minimum surface area taken by an agent on a node or edge */
    private double MIN_SURFACE_AREA_TAKEN_BY_AGENT = 0.5;
    /** Maximum surface area taken by an agent on a node or edge */
    private double MAX_SURFACE_AREA_TAKEN_BY_AGENT = 1.5;

    /** Multiplier for stress calculation */
    private double STRESS_MULTIPLIER = 40.0;
    /** Rate at which stress increases */
    private double stressIncreaseRate = 0.15;
    /** Rate at which stress decreases */
    private double stressDecreaseRate = 0.03;

    /** Multiplier for backtracking edge scores */
    private double backtrackingEdgeScoreMultiplier = 0.1;

    /** Time step between edge decisions */
    private double timeStepBetweenEdgeDecisions = 0.1; // in seconds

    /**
     * Gets an immutable view of the default decision-making factors for node decisions.
     * @return an unmodifiable map of default decision-making factors for node decisions
     */
    public Map<AgentPossibleNodeDecision, Double> getImmutableDecisionMakingFactors() {
        return Collections.unmodifiableMap(defaultNodeDecisionMakingFactors);
    }

    /**
     * Retrieves the decision-making factor for a given agent decision type.
     *
     * @param decision The type of agent decision to retrieve the factor for
     * @return The decision-making factor associated with the specified decision type
     */
    public double getDecisionMakingFactor(AgentPossibleNodeDecision decision) {
        return defaultNodeDecisionMakingFactors.get(decision);
    }

    /**
     * Retrieves the decision-making factor for a given edge decision type.
     * @param decision The type of edge decision to retrieve the factor for
     * @return The decision-making factor associated with the specified edge decision type
     */
    public double getDecisionMakingFactor(AgentPossibleEdgeDecision decision) {
        return defaultEdgeDecisionMakingFactors.get(decision);
    }

    /**
     * Updates the walking speed reduction factor based on the current walking and running speeds.
     */
    private void updateWALK_SPEED_REDUCTION_FACTOR() {
        this.WALK_SPEED_REDUCTION_FACTOR = WALKING_SPEED / RUNNING_SPEED;
    }

    /**
     * Validates the coherence of walking and running speeds, ensuring that they are positive and that running speed is not less than walking speed.
     * @param walkingSpeed the walking speed to validate
     * @param runningSpeed the running speed to validate
     */
    private void validateSpeedCoherence(double walkingSpeed, double runningSpeed) {
        if (walkingSpeed <= 0) {
            throw new IllegalArgumentException("Walking speed must be greater than zero.");
        }
        if (runningSpeed <= 0) {
            throw new IllegalArgumentException("Running speed must be greater than zero.");
        }
        if (runningSpeed < walkingSpeed) {
            throw new IllegalArgumentException("Running speed must be greater than or equal to walking speed.");
        }
    }

    /**
     * Validates the coherence of agent maximum speeds, ensuring that they are positive and that the maximum running speed is not less than the minimum agent max speed.
     * @param minAgentMaxSpeed the minimum agent max speed to validate
     * @param maxRunningSpeed the maximum running speed to validate
     */
    private void validateAgentMaxSpeedCoherence(double minAgentMaxSpeed, double maxRunningSpeed) {
        if (minAgentMaxSpeed <= 0) {
            throw new IllegalArgumentException("Minimum agent max speed must be greater than zero.");
        }
        if (maxRunningSpeed < minAgentMaxSpeed) {
            throw new IllegalArgumentException(
                    "Maximum running speed must be greater than or equal to minimum agent max speed.");
        }
    }

    /**
     * Sets the running speed for agents and updates related parameters to maintain coherence. The walking speed reduction factor and maximum running speed are updated based on the new running speed.
     * @param runningSpeed the new running speed to set for agents, must be greater than zero and not less than the current walking speed
     */
    public void setRUNNING_SPEED(double runningSpeed) {
        validateSpeedCoherence(WALKING_SPEED, runningSpeed);
        double candidateMaxRunningSpeed = runningSpeed * 1.5;
        validateAgentMaxSpeedCoherence(MIN_AGENT_MAX_SPEED, candidateMaxRunningSpeed);
        this.RUNNING_SPEED = runningSpeed;
        this.MAX_RUNNING_SPEED = candidateMaxRunningSpeed;
        updateWALK_SPEED_REDUCTION_FACTOR();
    }

    /**
     * Sets the maximum running speed for agents, ensuring it is coherent 
     * with the minimum agent max speed. The running speed is not directly set by this method,
     *  but it ensures that the maximum running speed is not less than the minimum agent max speed.
     * @param walkingSpeed the new walking speed to set for agents, must be greater than zero and not greater than the current running speed
     */
    public void setWALKING_SPEED(double walkingSpeed) {
        validateSpeedCoherence(walkingSpeed, RUNNING_SPEED);
        this.WALKING_SPEED = walkingSpeed;
        updateWALK_SPEED_REDUCTION_FACTOR();
    }

    /**
     * Sets the minimum agent maximum speed, ensuring it is coherent with the maximum running speed.
     * @param minAgentMaxSpeed the new minimum agent maximum speed to set, must be greater than zero and not greater than the current maximum running speed
     */
    public void setMIN_AGENT_MAX_SPEED(double minAgentMaxSpeed) {
        validateAgentMaxSpeedCoherence(minAgentMaxSpeed, MAX_RUNNING_SPEED);
        this.MIN_AGENT_MAX_SPEED = minAgentMaxSpeed;
    }

    /**
     * Returns the current running speed for agents.
     * @return the running speed
     */
    public double getRUNNING_SPEED() {
        return RUNNING_SPEED;
    }

    /**
     * Returns the current max running speed for agents.
     * @return the running speed
     */
    public double getMAX_RUNNING_SPEED() {
        return MAX_RUNNING_SPEED;
    }

    /**
     * Returns the current walking speed for agents.
     * @return the walking speed
     */
    public double getMIN_AGENT_MAX_SPEED() {
        return MIN_AGENT_MAX_SPEED;
    }

    /**
     * Returns the current walking speed reduction factor for agents.
     * @return the walking speed reduction factor
     */
    public double getWALKING_SPEED() {
        return WALKING_SPEED;
    }

    /**
     * Returns the current walking speed reduction factor for agents, which is derived from the walking and running speeds.
     * @return the walking speed reduction factor
     */
    public double getWALK_SPEED_REDUCTION_FACTOR() {
        return WALK_SPEED_REDUCTION_FACTOR;
    }

    /**
     * Returns the minimum stress tolerance for agents, which is a value between 0 and 1 that influences how agents react to stress in their environment.
     * @return the minimum stress tolerance
     */
    public double getMIN_STRESS_TOLERANCE() {
        return MIN_STRESS_TOLERANCE;
    }

    /**
     * Returns the maximum stress tolerance for agents, which is a value between 0 and 1 that influences how agents react to stress in their environment.
     * @return the maximum stress tolerance
     */
    public double getMAX_STRESS_TOLERANCE() {
        return MAX_STRESS_TOLERANCE;
    }

    /**
     * Returns the minimum crowding tolerance for agents, which is a value between 0 and 1 that influences how agents react to crowding in their environment.
     * @return the minimum crowding tolerance
     */
    public double getMIN_CROWDING_TOLERANCE() {
        return MIN_CROWDING_TOLERANCE;
    }

    /**
     * Returns the maximum crowding tolerance for agents, which is a value between 0 and 1 that influences how agents react to crowding in their environment.
     * @return the maximum crowding tolerance
     */
    public double getMAX_CROWDING_TOLERANCE() {
        return MAX_CROWDING_TOLERANCE;
    }

    /**
     * Returns the minimum repeat last decision tendency for agents, which is a value between 0 and 1 that influences how agents react to repeating their last decision.
     * @return the minimum repeat last decision tendency
     */
    public double getMIN_REPEAT_LAST_DECISION_TENDENCY() {
        return MIN_REPEAT_LAST_DECISION_TENDENCY;
    }

    /**
     * Returns the maximum repeat last decision tendency for agents, which is a value between 0 and 1 that influences how agents react to repeating their last decision.
     * @return the maximum repeat last decision tendency
     */
    public double getMAX_REPEAT_LAST_DECISION_TENDENCY() {
        return MAX_REPEAT_LAST_DECISION_TENDENCY;
    }

    /**
     * Returns the minimum base own decision making factor for agents, which is a value between 0 and 1 that influences how agents make decisions.
     * @return the minimum base own decision making factor
     */
    public double getMIN_BASE_OWN_DECISION_MAKING_FACTOR() {
        return MIN_BASE_OWN_DECISION_MAKING_FACTOR;
    }

    /**
     * Returns the maximum base own decision making factor for agents, which is a value between 0 and 1 that influences how agents make decisions.
     * @return the maximum base own decision making factor
     */
    public double getMAX_BASE_OWN_DECISION_MAKING_FACTOR() {
        return MAX_BASE_OWN_DECISION_MAKING_FACTOR;
    }

    /**
     * Returns the minimum health for agents, a value that can influence how agents react to damage or stress in their environment.
     * @return the minimum health
     */
    public int getMIN_HEALTH() {
        return MIN_HEALTH;
    }

    /**
     * Returns the maximum health for agents, a value that can influence how agents react to damage or stress in their environment.
     * @return the maximum health
     */
    public int getMAX_HEALTH() {
        return MAX_HEALTH;
    }

    /**
     * Returns the minimum surface area taken by an agent on a node or edge, which can influence how agents interact with their environment and with each other.
     * @return the minimum surface area taken by an agent
     */
    public double getMIN_SURFACE_AREA_TAKEN_BY_AGENT() {
        return MIN_SURFACE_AREA_TAKEN_BY_AGENT;
    }

    /**
     * Returns the maximum surface area taken by an agent on a node or edge, which can influence how agents interact with their environment and with each other.
     * @return the maximum surface area taken by an agent
     */
    public double getMAX_SURFACE_AREA_TAKEN_BY_AGENT() {
        return MAX_SURFACE_AREA_TAKEN_BY_AGENT;
    }

    @Deprecated
    public double getSTRESS_MULTIPLIER() {
        return STRESS_MULTIPLIER;
    }

    @Deprecated
    public void setSTRESS_MULTIPLIER(double sTRESS_MULTIPLIER) {
        STRESS_MULTIPLIER = sTRESS_MULTIPLIER;
    }

    /**
     * Returns the multiplier for backtracking edge scores, which influences how agents evaluate the option of backtracking on an edge when making decisions.
     * @return the backtracking edge score multiplier
     */
    public double getBacktrackingEdgeScoreMultiplier() {
        return backtrackingEdgeScoreMultiplier;
    }

    /**
     * Sets the multiplier for backtracking edge scores, which influences how agents evaluate the option of backtracking on an edge when making decisions.
     * @param backtrackingEdgeScoreMultiplier the backtracking edge score multiplier to set
     */
    public void setBacktrackingEdgeScoreMultiplier(double backtrackingEdgeScoreMultiplier) {
        this.backtrackingEdgeScoreMultiplier = backtrackingEdgeScoreMultiplier;
    }

    /**
     * Returns the time step between edge decisions, which determines how frequently agents evaluate their options for edge traversal and make decisions while on edges.
     * @return the time step between edge decisions in seconds
     */
    public double getStressDecreaseRate() {
        return stressDecreaseRate;
    }

    /**
     * Returns the rate at which stress increases for agents, which influences how quickly agents become stressed in response to stress-inducing factors in their environment.
     * @return the stress increase rate
     */
    public double getStressIncreaseRate() {
        return stressIncreaseRate;
    }

    /**
     * Sets the rate at which stress decreases for agents, which influences how quickly agents recover from stress.
     * @param stressDecreaseRate the stress decrease rate to set
     */
    public void setStressDecreaseRate(double stressDecreaseRate) {
        this.stressDecreaseRate = stressDecreaseRate;
    }

    /**
     * Sets the rate at which stress increases for agents, which influences how quickly agents become stressed in response to stress-inducing factors in their environment.
     * @param stressIncreaseRate the stress increase rate to set
     */
    public void setStressIncreaseRate(double stressIncreaseRate) {
        this.stressIncreaseRate = stressIncreaseRate;
    }

    /**
     * Returns the time step between edge decisions, which determines how frequently agents evaluate 
     * their options for edge traversal and make decisions while on edges.
     * @return the time step between edge decisions in seconds
     */
    public double getTimeStepBetweenEdgeDecisions() {
        return timeStepBetweenEdgeDecisions;
    }

    /**
     * Generates a random speed for an agent within the defined bounds.
     * @param random the random number generator
     * @return a random speed value
     */
    public double generateRandomSpeed(Random random) {
        return randomDoubleBetween(random, MIN_AGENT_MAX_SPEED, MAX_RUNNING_SPEED);
    }

    /**
     * Generates a random stress tolerance for an agent within the defined bounds.
     * @param random the random number generator
     * @return a random stress tolerance value
     */
    public double generateRandomStressTolerance(Random random) {
        return randomDoubleBetween(random, MIN_STRESS_TOLERANCE, MAX_STRESS_TOLERANCE);
    }

    /**
     * Generates a random crowding tolerance for an agent within the defined bounds.
     * @param random the random number generator
     * @return a random crowding tolerance value
     */
    public double generateRandomCrowdingTolerance(Random random) {
        return randomDoubleBetween(random, MIN_CROWDING_TOLERANCE, MAX_CROWDING_TOLERANCE);
    }

    /**
     * Generates a random tendency to repeat the last decision for an agent within the defined bounds.
     * @param random the random number generator
     * @return a random repeat last decision tendency value
     */
    public double generateRandomRepeatLastDecisionTendency(Random random) {
        return randomDoubleBetween(random, MIN_REPEAT_LAST_DECISION_TENDENCY, MAX_REPEAT_LAST_DECISION_TENDENCY);
    }

    /**
     * Generates a random base own decision making factor for an agent within the defined bounds.
     * @param random the random number generator
     * @return a random base own decision making factor value
     */
    public double generateRandomBaseOwnDecisionMakingFactor(Random random) {
        return randomDoubleBetween(random, MIN_BASE_OWN_DECISION_MAKING_FACTOR, MAX_BASE_OWN_DECISION_MAKING_FACTOR);
    }

    /**
     * Generates a random health value for an agent within the defined bounds.
     * @param random the random number generator
     * @return a random health value
     */
    public int generateRandomHealth(Random random) {
        return randomIntBetween(random, MIN_HEALTH, MAX_HEALTH);
    }

    /**
     * Generates a random surface area taken by an agent within the defined bounds.
     * @param random the random number generator
     * @return a random surface area taken by an agent value
     */
    public double generateRandomSurfaceAreaTakenByAgent(Random random) {
        return randomDoubleBetween(random, MIN_SURFACE_AREA_TAKEN_BY_AGENT, MAX_SURFACE_AREA_TAKEN_BY_AGENT);
    }

    /**
     * Generates a random double value within the specified bounds.
     * @param random the random number generator
     * @param min the minimum value
     * @param max the maximum value
     * @return a random double value
     */
    private double randomDoubleBetween(Random random, double min, double max) {
        if (max < min) {
            throw new IllegalStateException("Maximum value must be greater than or equal to minimum value.");
        }
        return min + random.nextDouble() * (max - min);
    }

    /**
     * Generates a random integer value within the specified bounds.
     * @param random the random number generator
     * @param min the minimum value
     * @param max the maximum value
     * @return a random integer value
     */
    private int randomIntBetween(Random random, int min, int max) {
        if (max < min) {
            throw new IllegalStateException("Maximum value must be greater than or equal to minimum value.");
        }
        return min + random.nextInt((max - min) + 1);
    }

    /**
     * Resets the settings to their default values.
     */
    public void resetSettings() {
        AgentSettings defaultSettings = new AgentSettings();
        this.WALKING_SPEED = defaultSettings.WALKING_SPEED;
        this.RUNNING_SPEED = defaultSettings.RUNNING_SPEED;
        this.MAX_RUNNING_SPEED = defaultSettings.MAX_RUNNING_SPEED;
        this.WALK_SPEED_REDUCTION_FACTOR = defaultSettings.WALK_SPEED_REDUCTION_FACTOR;
        this.MIN_AGENT_MAX_SPEED = defaultSettings.MIN_AGENT_MAX_SPEED;
        this.MIN_STRESS_TOLERANCE = defaultSettings.MIN_STRESS_TOLERANCE;
        this.MAX_STRESS_TOLERANCE = defaultSettings.MAX_STRESS_TOLERANCE;
        this.MIN_CROWDING_TOLERANCE = defaultSettings.MIN_CROWDING_TOLERANCE;
        this.MAX_CROWDING_TOLERANCE = defaultSettings.MAX_CROWDING_TOLERANCE;
        this.MIN_REPEAT_LAST_DECISION_TENDENCY = defaultSettings.MIN_REPEAT_LAST_DECISION_TENDENCY;
        this.MAX_REPEAT_LAST_DECISION_TENDENCY = defaultSettings.MAX_REPEAT_LAST_DECISION_TENDENCY;
        this.MIN_BASE_OWN_DECISION_MAKING_FACTOR = defaultSettings.MIN_BASE_OWN_DECISION_MAKING_FACTOR;
        this.MAX_BASE_OWN_DECISION_MAKING_FACTOR = defaultSettings.MAX_BASE_OWN_DECISION_MAKING_FACTOR;
        this.MIN_HEALTH = defaultSettings.MIN_HEALTH;
        this.MAX_HEALTH = defaultSettings.MAX_HEALTH;
        this.MIN_SURFACE_AREA_TAKEN_BY_AGENT = defaultSettings.MIN_SURFACE_AREA_TAKEN_BY_AGENT;
        this.MAX_SURFACE_AREA_TAKEN_BY_AGENT = defaultSettings.MAX_SURFACE_AREA_TAKEN_BY_AGENT;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        AgentSettings other = (AgentSettings) obj;
        return Double.compare(other.WALKING_SPEED, WALKING_SPEED) == 0
                && Double.compare(other.RUNNING_SPEED, RUNNING_SPEED) == 0
                && Double.compare(other.MAX_RUNNING_SPEED, MAX_RUNNING_SPEED) == 0
                && Double.compare(other.WALK_SPEED_REDUCTION_FACTOR, WALK_SPEED_REDUCTION_FACTOR) == 0
                && Double.compare(other.MIN_AGENT_MAX_SPEED, MIN_AGENT_MAX_SPEED) == 0
                && Double.compare(other.MIN_STRESS_TOLERANCE, MIN_STRESS_TOLERANCE) == 0
                && Double.compare(other.MAX_STRESS_TOLERANCE, MAX_STRESS_TOLERANCE) == 0
                && Double.compare(other.MIN_CROWDING_TOLERANCE, MIN_CROWDING_TOLERANCE) == 0
                && Double.compare(other.MAX_CROWDING_TOLERANCE, MAX_CROWDING_TOLERANCE) == 0
                && Double.compare(other.MIN_REPEAT_LAST_DECISION_TENDENCY, MIN_REPEAT_LAST_DECISION_TENDENCY) == 0
                && Double.compare(other.MAX_REPEAT_LAST_DECISION_TENDENCY, MAX_REPEAT_LAST_DECISION_TENDENCY) == 0
                && Double.compare(other.MIN_BASE_OWN_DECISION_MAKING_FACTOR, MIN_BASE_OWN_DECISION_MAKING_FACTOR) == 0
                && Double.compare(other.MAX_BASE_OWN_DECISION_MAKING_FACTOR, MAX_BASE_OWN_DECISION_MAKING_FACTOR) == 0
                && MIN_HEALTH == other.MIN_HEALTH
                && MAX_HEALTH == other.MAX_HEALTH
                && Double.compare(other.MIN_SURFACE_AREA_TAKEN_BY_AGENT, MIN_SURFACE_AREA_TAKEN_BY_AGENT) == 0
                && Double.compare(other.MAX_SURFACE_AREA_TAKEN_BY_AGENT, MAX_SURFACE_AREA_TAKEN_BY_AGENT) == 0
                && Objects.equals(defaultNodeDecisionMakingFactors, other.defaultNodeDecisionMakingFactors);
    }

    @Override
    public int hashCode() {
        return Objects.hash(defaultNodeDecisionMakingFactors, WALKING_SPEED, RUNNING_SPEED, MAX_RUNNING_SPEED,
                WALK_SPEED_REDUCTION_FACTOR, MIN_AGENT_MAX_SPEED, MIN_STRESS_TOLERANCE, MAX_STRESS_TOLERANCE,
                MIN_CROWDING_TOLERANCE, MAX_CROWDING_TOLERANCE, MIN_REPEAT_LAST_DECISION_TENDENCY,
                MAX_REPEAT_LAST_DECISION_TENDENCY, MIN_BASE_OWN_DECISION_MAKING_FACTOR,
                MAX_BASE_OWN_DECISION_MAKING_FACTOR, MIN_HEALTH, MAX_HEALTH, MIN_SURFACE_AREA_TAKEN_BY_AGENT,
                MAX_SURFACE_AREA_TAKEN_BY_AGENT);
    }

    @Override
    public String toString() {
        return "AgentSettings{" +
                "decisionFactors=" + defaultNodeDecisionMakingFactors +
                ", WALKING_SPEED=" + WALKING_SPEED +
                ", RUNNING_SPEED=" + RUNNING_SPEED +
                ", MAX_RUNNING_SPEED=" + MAX_RUNNING_SPEED +
                ", MIN_AGENT_MAX_SPEED=" + MIN_AGENT_MAX_SPEED +
                ", MIN_STRESS_TOLERANCE=" + MIN_STRESS_TOLERANCE +
                ", MAX_STRESS_TOLERANCE=" + MAX_STRESS_TOLERANCE +
                ", MIN_CROWDING_TOLERANCE=" + MIN_CROWDING_TOLERANCE +
                ", MAX_CROWDING_TOLERANCE=" + MAX_CROWDING_TOLERANCE +
                ", MIN_REPEAT_LAST_DECISION_TENDENCY=" + MIN_REPEAT_LAST_DECISION_TENDENCY +
                ", MAX_REPEAT_LAST_DECISION_TENDENCY=" + MAX_REPEAT_LAST_DECISION_TENDENCY +
                ", MIN_BASE_OWN_DECISION_MAKING_FACTOR=" + MIN_BASE_OWN_DECISION_MAKING_FACTOR +
                ", MAX_BASE_OWN_DECISION_MAKING_FACTOR=" + MAX_BASE_OWN_DECISION_MAKING_FACTOR +
                ", MIN_HEALTH=" + MIN_HEALTH +
                ", MAX_HEALTH=" + MAX_HEALTH +
                ", MIN_SURFACE_AREA_TAKEN_BY_AGENT=" + MIN_SURFACE_AREA_TAKEN_BY_AGENT +
                ", MAX_SURFACE_AREA_TAKEN_BY_AGENT=" + MAX_SURFACE_AREA_TAKEN_BY_AGENT +
                '}';
    }
}
