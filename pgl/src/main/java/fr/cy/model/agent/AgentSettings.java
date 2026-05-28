package fr.cy.model.agent;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

import fr.cy.model.agent.behaviour.decisions.AgentPossibleDecision;

public class AgentSettings {
    /** Factors used to influence agent decision-making */
    private final Map<AgentPossibleDecision, Double> defaultDecisionMakingFactors = new EnumMap<>(
            AgentPossibleDecision.class);
    {
        // Initialize decision-making factors for each decision type
        defaultDecisionMakingFactors.put(AgentPossibleDecision.FOLLOW_CROWD, 2.0);
        defaultDecisionMakingFactors.put(AgentPossibleDecision.RANDOM, 1.0);
        defaultDecisionMakingFactors.put(AgentPossibleDecision.FOLLOW_RECOMMENDED_PATH, 1.5);
        defaultDecisionMakingFactors.put(AgentPossibleDecision.FOLLOW_SHORTEST_PATH, 0.05);
        // decisionMakingFactors.put(FollowShortestPathAction.class, 0.2);
        // decisionMakingFactors.put(NicestPathAction.class, 0.5);
    }

    private double WALKING_SPEED = 1.0;
    private double RUNNING_SPEED = 2.0;
    private double WALK_SPEED_REDUCTION_FACTOR = RUNNING_SPEED / WALKING_SPEED; 
    public Map<AgentPossibleDecision, Double> getImmutableDecisionMakingFactors() {
        return Collections.unmodifiableMap(defaultDecisionMakingFactors);
    }

    /**
     * Retrieves the decision-making factor for a given agent decision type
     *
     * @param decision The type of agent decision to retrieve the factor for
     * @return The decision-making factor associated with the specified decision type
     */
    public double getDecisionMakingFactor(AgentPossibleDecision decision) {
        return defaultDecisionMakingFactors.getOrDefault(decision, Double.NEGATIVE_INFINITY);
    }

    public double getRUNNING_SPEED() {
        return RUNNING_SPEED;
    }

    public double getWALKING_SPEED() {
        return WALKING_SPEED;
    }

    public double getWALK_SPEED_REDUCTION_FACTOR() {
        return WALK_SPEED_REDUCTION_FACTOR;
    }

}
