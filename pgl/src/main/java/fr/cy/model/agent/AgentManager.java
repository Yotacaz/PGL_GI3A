package fr.cy.model.agent;

import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.cy.model.agent.behaviour.agentActions.AgentAction;
import fr.cy.model.agent.behaviour.decisions.AgentPossibleDecision;
import fr.cy.model.agent.behaviour.decisions.DecisionContext;
import fr.cy.model.agent.behaviour.decisions.DecisionContextProvider;
import fr.cy.model.graph.element.Edge;
import fr.cy.model.graph.element.GraphElement;

/**
 * Manager responsible for higher-level operations on {@link Agent} instances.
 * Currently holds configuration values used when evaluating decisions.
 */
public class AgentManager {

    private List<Agent> agents;
    private DecisionContextProvider decisionContextProvider;
    // private Map<Agent, AgentAction> agentActionsPreviousTick = new HashMap<>();

    public List<Agent> getAgentsByOwnDecisionMakingFactor(double factor) {
        return Collections.emptyList();
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
        decisionMakingFactors.put(AgentPossibleDecision.NICEST_PATH, 0.5);
    }

    /**
     * Retrieves the decision-making factor for a given agent decision type
     *
     * @param decision The type of agent decision to retrieve the factor for
     * @return The decision-making factor associated with the specified decision type
     */
    public double getDecisionMakingFactor(AgentPossibleDecision decision) {
        return decisionMakingFactors.getOrDefault(decision, Double.NEGATIVE_INFINITY);
    }

    /**
     * Retrieves a list of agents whose base own decision-making factor matches
     * the provided factor. 
     *
     * @param factor The decision-making factor to filter agents, typically between 0 and 1
     * @return A list of agents that match the specified decision-making factor (never null)
     */
    public void sortAgentsByOwnDecisionMakingFactor() {
        agents.sort(new AgentByOwnDecisionMakingComparator());
    }

    public void tick() {
        sortAgentsByOwnDecisionMakingFactor(); //should be relatively fast since the list is almost sorted
        for (Agent agent : agents) {
            if (agent.isOnNode()) {

            }
            double maxAgentSpeed = agent.getMaxSpeed();
            DecisionContext decisionContext = decisionContextProvider.constructContext(agent);
            AgentAction action = agent.makeDecision(decisionContext);
        }

        for (Agent agent : agents) {
            agent.performCurrentAction();
        }
    }

}
