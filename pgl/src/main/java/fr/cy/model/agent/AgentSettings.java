package fr.cy.model.agent;

import java.io.Serializable;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

import fr.cy.model.agent.behaviour.decisions.AgentPossibleNodeDecision;

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

    /** Factors used to influence agent decision-making */
    private static final AgentSettings instance = new AgentSettings();

    /** @return the singleton instance of AgentSettings */
    public static AgentSettings getInstance() {
        return instance;
    }

    /** Immutable map of default decision-making factors for each possible agent decision type */
    private final Map<AgentPossibleNodeDecision, Double> defaultDecisionMakingFactors = new EnumMap<>(
            AgentPossibleNodeDecision.class);
    {
        // Initialize decision-making factors for each decision type
        defaultDecisionMakingFactors.put(AgentPossibleNodeDecision.FOLLOW_CROWD, 2.0);
        defaultDecisionMakingFactors.put(AgentPossibleNodeDecision.FOLLOW_LESS_CROWDED_PATH, 0.1);
        defaultDecisionMakingFactors.put(AgentPossibleNodeDecision.FOLLOW_RECOMMENDED_PATH, 1.5);
        defaultDecisionMakingFactors.put(AgentPossibleNodeDecision.FOLLOW_SHORTEST_PATH, 0.05);
        defaultDecisionMakingFactors.put(AgentPossibleNodeDecision.NICEST_PATH, 0.2);
        defaultDecisionMakingFactors.put(AgentPossibleNodeDecision.RANDOM, 0.5);
        defaultDecisionMakingFactors.put(AgentPossibleNodeDecision.CONTINUE_LAST_ACTION, 2.5);
        defaultDecisionMakingFactors.put(AgentPossibleNodeDecision.WAIT, 1.0);
    }

    /* Default speeds and tolerances for agents, can be overridden by individual agents */
    /** Walking speed for agents */
    private double WALKING_SPEED = 1.0;
    /** Running speed for agents */
    private double RUNNING_SPEED = 3.0;
    /** Maximum running speed for agents, derived from RUNNING_SPEED */
    private double MAX_RUNNING_SPEED = RUNNING_SPEED * 1.5;
    /** Reduction factor applied to speed when walking instead of running, derived from WALKING_SPEED and RUNNING_SPEED */
    private double WALK_SPEED_REDUCTION_FACTOR = WALKING_SPEED / RUNNING_SPEED;

    /** Minimum max speed for every agent */
    private double MIN_AGENT_MAX_SPEED = 1.0;

    /** Minimum stress tolerance for every agent */
    private double MIN_STRESS_TOLERANCE = 0.0;
    /** Maximum stress tolerance for every agent */
    private double MAX_STRESS_TOLERANCE = 1.0;
    /** Minimum crowding tolerance for every agent */
    private double MIN_CROWDING_TOLERANCE = 0.0;
    /** Maximum crowding tolerance for every agent */
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

    private double STRESS_MULTIPLIER = 40.0;

    private double backtrackingEdgeScoreMultiplier = 0.5;

    private double timeStepDurationForSpaceOccupationEstimation = 0.1; // in seconds

    public Map<AgentPossibleNodeDecision, Double> getImmutableDecisionMakingFactors() {
        return Collections.unmodifiableMap(defaultDecisionMakingFactors);
    }

    /**
     * Retrieves the decision-making factor for a given agent decision type.
     *
     * @param decision The type of agent decision to retrieve the factor for
     * @return The decision-making factor associated with the specified decision type
     */
    public double getDecisionMakingFactor(AgentPossibleNodeDecision decision) {
        return defaultDecisionMakingFactors.get(decision);
    }

    private void updateWALK_SPEED_REDUCTION_FACTOR() {
        this.WALK_SPEED_REDUCTION_FACTOR = WALKING_SPEED / RUNNING_SPEED;
    }

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

    private void validateAgentMaxSpeedCoherence(double minAgentMaxSpeed, double maxRunningSpeed) {
        if (minAgentMaxSpeed <= 0) {
            throw new IllegalArgumentException("Minimum agent max speed must be greater than zero.");
        }
        if (maxRunningSpeed < minAgentMaxSpeed) {
            throw new IllegalArgumentException(
                    "Maximum running speed must be greater than or equal to minimum agent max speed.");
        }
    }

    public void setRUNNING_SPEED(double runningSpeed) {
        validateSpeedCoherence(WALKING_SPEED, runningSpeed);
        double candidateMaxRunningSpeed = runningSpeed * 1.5;
        validateAgentMaxSpeedCoherence(MIN_AGENT_MAX_SPEED, candidateMaxRunningSpeed);
        this.RUNNING_SPEED = runningSpeed;
        this.MAX_RUNNING_SPEED = candidateMaxRunningSpeed;
        updateWALK_SPEED_REDUCTION_FACTOR();
    }

    public void setWALKING_SPEED(double walkingSpeed) {
        validateSpeedCoherence(walkingSpeed, RUNNING_SPEED);
        this.WALKING_SPEED = walkingSpeed;
        updateWALK_SPEED_REDUCTION_FACTOR();
    }

    public void setMIN_AGENT_MAX_SPEED(double minAgentMaxSpeed) {
        validateAgentMaxSpeedCoherence(minAgentMaxSpeed, MAX_RUNNING_SPEED);
        this.MIN_AGENT_MAX_SPEED = minAgentMaxSpeed;
    }

    public double getRUNNING_SPEED() {
        return RUNNING_SPEED;
    }

    public double getMAX_RUNNING_SPEED() {
        return MAX_RUNNING_SPEED;
    }

    public double getMIN_AGENT_MAX_SPEED() {
        return MIN_AGENT_MAX_SPEED;
    }

    public double getWALKING_SPEED() {
        return WALKING_SPEED;
    }

    public double getWALK_SPEED_REDUCTION_FACTOR() {
        return WALK_SPEED_REDUCTION_FACTOR;
    }

    public double getMIN_STRESS_TOLERANCE() {
        return MIN_STRESS_TOLERANCE;
    }

    public double getMAX_STRESS_TOLERANCE() {
        return MAX_STRESS_TOLERANCE;
    }

    public double getMIN_CROWDING_TOLERANCE() {
        return MIN_CROWDING_TOLERANCE;
    }

    public double getMAX_CROWDING_TOLERANCE() {
        return MAX_CROWDING_TOLERANCE;
    }

    public double getMIN_REPEAT_LAST_DECISION_TENDENCY() {
        return MIN_REPEAT_LAST_DECISION_TENDENCY;
    }

    public double getMAX_REPEAT_LAST_DECISION_TENDENCY() {
        return MAX_REPEAT_LAST_DECISION_TENDENCY;
    }

    public double getMIN_BASE_OWN_DECISION_MAKING_FACTOR() {
        return MIN_BASE_OWN_DECISION_MAKING_FACTOR;
    }

    public double getMAX_BASE_OWN_DECISION_MAKING_FACTOR() {
        return MAX_BASE_OWN_DECISION_MAKING_FACTOR;
    }

    public int getMIN_HEALTH() {
        return MIN_HEALTH;
    }

    public int getMAX_HEALTH() {
        return MAX_HEALTH;
    }

    public double getMIN_SURFACE_AREA_TAKEN_BY_AGENT() {
        return MIN_SURFACE_AREA_TAKEN_BY_AGENT;
    }

    public double getMAX_SURFACE_AREA_TAKEN_BY_AGENT() {
        return MAX_SURFACE_AREA_TAKEN_BY_AGENT;
    }

    public double getSTRESS_MULTIPLIER() {
        return STRESS_MULTIPLIER;
    }

    public void setSTRESS_MULTIPLIER(double sTRESS_MULTIPLIER) {
        STRESS_MULTIPLIER = sTRESS_MULTIPLIER;
    }

    public double getBacktrackingEdgeScoreMultiplier() {
        return backtrackingEdgeScoreMultiplier;
    }

    public void setBacktrackingEdgeScoreMultiplier(double backtrackingEdgeScoreMultiplier) {
        this.backtrackingEdgeScoreMultiplier = backtrackingEdgeScoreMultiplier;
    }

    public double getTimeStepDurationForSpaceOccupationEstimation() {
        return timeStepDurationForSpaceOccupationEstimation;
    }

    public double generateRandomSpeed(Random random) {
        return randomDoubleBetween(random, MIN_AGENT_MAX_SPEED, MAX_RUNNING_SPEED);
    }

    public double generateRandomStressTolerance(Random random) {
        return randomDoubleBetween(random, MIN_STRESS_TOLERANCE, MAX_STRESS_TOLERANCE);
    }

    public double generateRandomCrowdingTolerance(Random random) {
        return randomDoubleBetween(random, MIN_CROWDING_TOLERANCE, MAX_CROWDING_TOLERANCE);
    }

    public double generateRandomRepeatLastDecisionTendency(Random random) {
        return randomDoubleBetween(random, MIN_REPEAT_LAST_DECISION_TENDENCY, MAX_REPEAT_LAST_DECISION_TENDENCY);
    }

    public double generateRandomBaseOwnDecisionMakingFactor(Random random) {
        return randomDoubleBetween(random, MIN_BASE_OWN_DECISION_MAKING_FACTOR, MAX_BASE_OWN_DECISION_MAKING_FACTOR);
    }

    public int generateRandomHealth(Random random) {
        return randomIntBetween(random, MIN_HEALTH, MAX_HEALTH);
    }

    public double generateRandomSurfaceAreaTakenByAgent(Random random) {
        return randomDoubleBetween(random, MIN_SURFACE_AREA_TAKEN_BY_AGENT, MAX_SURFACE_AREA_TAKEN_BY_AGENT);
    }

    private double randomDoubleBetween(Random random, double min, double max) {
        if (max < min) {
            throw new IllegalStateException("Maximum value must be greater than or equal to minimum value.");
        }
        return min + random.nextDouble() * (max - min);
    }

    private int randomIntBetween(Random random, int min, int max) {
        if (max < min) {
            throw new IllegalStateException("Maximum value must be greater than or equal to minimum value.");
        }
        return min + random.nextInt((max - min) + 1);
    }

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
                && Objects.equals(defaultDecisionMakingFactors, other.defaultDecisionMakingFactors);
    }

    @Override
    public int hashCode() {
        return Objects.hash(defaultDecisionMakingFactors, WALKING_SPEED, RUNNING_SPEED, MAX_RUNNING_SPEED,
                WALK_SPEED_REDUCTION_FACTOR, MIN_AGENT_MAX_SPEED, MIN_STRESS_TOLERANCE, MAX_STRESS_TOLERANCE,
                MIN_CROWDING_TOLERANCE, MAX_CROWDING_TOLERANCE, MIN_REPEAT_LAST_DECISION_TENDENCY,
                MAX_REPEAT_LAST_DECISION_TENDENCY, MIN_BASE_OWN_DECISION_MAKING_FACTOR,
                MAX_BASE_OWN_DECISION_MAKING_FACTOR, MIN_HEALTH, MAX_HEALTH, MIN_SURFACE_AREA_TAKEN_BY_AGENT,
                MAX_SURFACE_AREA_TAKEN_BY_AGENT);
    }

    @Override
    public String toString() {
        return "AgentSettings{" +
                "decisionFactors=" + defaultDecisionMakingFactors +
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
