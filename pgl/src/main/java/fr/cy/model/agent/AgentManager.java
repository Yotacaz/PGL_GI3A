package fr.cy.model.agent;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import fr.cy.model.agent.decisions.AgentPossibleDecision;

public class AgentManager {

    /** Retrieves a list of agents based on their own decision-making factor 
     * @param factor The decision-making factor to filter agents, typically between 0 and 1
     * @return A list of agents that match the specified decision-making factor
    */
    public List<Agent> getAgentsByOwnDescisionMakingFactor(double factor) {
        return null; // Placeholder return statement
    }

    /** Factors used to influence agent decision-making */
    private Map<AgentPossibleDecision, Double> decisionMakingFactors = new EnumMap<>(AgentPossibleDecision.class);
    {
        // Initialize decision-making factors for each decision type
        decisionMakingFactors.put(AgentPossibleDecision.FOLLOW_CROWD, 2.0);
        decisionMakingFactors.put(AgentPossibleDecision.FOLLOW_LESS_CROWDED_PATH, 1.0);
        decisionMakingFactors.put(AgentPossibleDecision.FOLLOW_RECOMMENDED_PATH, 1.5);
        decisionMakingFactors.put(AgentPossibleDecision.RANDOM, 0.05);
        decisionMakingFactors.put(AgentPossibleDecision.FOLLOW_SHORTEST_PATH, 0.2);
    }

    /** Retrieves the decision-making factor for a given agent decision type
     * @param decision The type of agent decision to retrieve the factor for
     * @return The decision-making factor associated with the specified decision type
    */
    public double getDecisionMakingFactor(AgentPossibleDecision decision) {
        return decisionMakingFactors.getOrDefault(decision, Double.NEGATIVE_INFINITY);
    }
}
