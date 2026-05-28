package fr.cy.model.agent;

import java.util.Comparator;
import java.util.List;

import fr.cy.model.agent.behaviour.agentActions.AgentAction;
import fr.cy.model.agent.behaviour.decisions.DecisionNodeContext;
import fr.cy.model.agent.behaviour.decisions.DecisionContextProvider;

/**
 * Manager responsible for higher-level operations on {@link Agent} instances.
 * Currently holds configuration values used when evaluating decisions.
 */
public class AgentManager {
    private AgentSettings agentSettings = new AgentSettings();
    private List<Agent> agents;
    private DecisionContextProvider decisionContextProvider;
    // private Map<Agent, AgentAction> agentActionsPreviousTick = new HashMap<>();

    private class AgentByOwnDecisionMakingComparator implements Comparator<Agent> {
        @Override
        public int compare(Agent a1, Agent a2) {
            // Higher decision-making score means higher priority (comes first)
            return Double.compare(a2.getCurrentOwnDecisionMakingFactor(), a1.getCurrentOwnDecisionMakingFactor());
        }
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
        updateStress();
        moveAgents();
    }
    public void moveAgents(){
        decisionContextProvider.clearCache();
        sortAgentsByOwnDecisionMakingFactor(); //should be relatively fast since the list is almost sorted

        for (Agent agent : agents) {
            if (agent.isOnNode()) {
                DecisionNodeContext decisionContext = decisionContextProvider.getContext(agent);
                AgentAction action = agent.makeDecision(decisionContext, agentSettings);
            }
        }

        for (Agent agent : agents) {
            double performed = agent.performCurrentAction(agentSettings);
        }

    }

    public List<Agent> getAgents() {
        return agents;
    }

    public void updateStress() {
        for (Agent agent : agents) {
            double stressLevel = agent.getStressLevel();

            //TODO: Update stress level based on current conditions (e.g., proximity to fire, congestion, etc.)

        }
    }

}
