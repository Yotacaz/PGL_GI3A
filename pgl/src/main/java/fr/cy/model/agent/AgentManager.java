package fr.cy.model.agent;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import java.io.*;

import fr.cy.model.agent.behaviour.agentActions.AgentAction;
import fr.cy.model.agent.behaviour.decisions.DecisionNodeContext;
import fr.cy.model.graph.element.Edge;
import fr.cy.model.graph.element.Node;
import fr.cy.model.agent.behaviour.decisions.DecisionContextProvider;
import fr.cy.model.simulation.SimulationSettings;

/**
 * Manager responsible for higher-level operations on {@link Agent} instances.
 * Currently holds configuration values used when evaluating decisions.
 */
public class AgentManager implements Serializable {

    private static final long serialVersionUID = 1L;


    private AgentSettings agentSettings = new AgentSettings();
    private List<Agent> agents;
    private List<Agent> deadAgents = new ArrayList<>();
    private DecisionContextProvider decisionContextProvider;
    private AgentGenerator agentGenerator;
    private final SimulationSettings simulationSettings;
    // private Map<Agent, AgentAction> agentActionsPreviousTick = new HashMap<>();

    public AgentManager(List<Agent> agents, DecisionContextProvider decisionContextProvider,
            AgentGenerator agentGenerator, SimulationSettings simulationSettings) {
        this.agents = agents;
        this.decisionContextProvider = decisionContextProvider;
        this.agentGenerator = agentGenerator;
        this.simulationSettings = simulationSettings;
    }

    public AgentManager(DecisionContextProvider decisionContextProvider, AgentGenerator agentGenerator,
            SimulationSettings simulationSettings) {
        this(new ArrayList<>(), decisionContextProvider, agentGenerator, simulationSettings);
    }

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
     * @param factor The decision-making factor to filter agents, typically between
     *               0 and 1
     * @return A list of agents that match the specified decision-making factor
     *         (never null)
     */
    private void sortAgentsByOwnDecisionMakingFactor() {
        agents.sort(new AgentByOwnDecisionMakingComparator());
    }

    public AgentSettings getAgentSettings() {
        return agentSettings;
    }

    public void generateRandomsAgents(int count) {
        for (int i = 0; i < count; i++) {
            Agent newAgent = agentGenerator.generateRandomAgentOnRandomNode("Agent" + (agents.size() + 1));
            agents.add(newAgent);
        }
    }

    public void generateAgentOnEdge(String baseName, Edge edge, double edgeProgress) {
        Agent newAgent = agentGenerator.generateAgent(baseName, edge, edgeProgress);
        agents.add(newAgent);
    }

    public void generateAgentOnNode(String baseName, Node node) {
        Agent newAgent = agentGenerator.generateAgent(baseName, node);
        agents.add(newAgent);
    }

    /**
     * Main update method for the agent manager, called at each tick of the
     * simulation.
     * It updates the stress levels of agents and processes their decisions and
     * actions.
     */
    public void tick() {
        tick(simulationSettings.getTickDuration());
    }

    public void tick(double tickDuration) {
        updateAgentsState();
        moveAgents(tickDuration);
    }

    private void moveAgents(double tickDuration) {
        decisionContextProvider.clearCache();
        sortAgentsByOwnDecisionMakingFactor(); // should be relatively fast since the list is almost sorted

        // generate and register decisions for all agents before performing any action,
        // to ensure that all agents have the same information when making their
        // decisions
        for (Agent agent : agents) {
            if (agent.isOnNode()) {
                DecisionNodeContext decisionContext = decisionContextProvider.getContext(agent);
                AgentAction action = agent.makeDecision(decisionContext, agentSettings);
                decisionContextProvider.registerChosenAction(agent, action);
            }
        }

        for (Agent agent : agents) {
            double remainingTime = tickDuration;
            while (remainingTime > 0.0) {
                if (agent.getCurrentAction() == null || (agent.getCurrentAction().isCompleted() && agent.isOnNode())) {
                    if (agent.getCurrentAction() != null) {
                        agent.setCurrentAction(null);
                    }
                    if (!agent.isOnNode()) {
                        break;
                    }
                    DecisionNodeContext decisionContext = decisionContextProvider.getContext(agent);
                    if (decisionContext == null) {
                        break;
                    }
                    AgentAction action = agent.makeDecision(decisionContext, agentSettings);
                    decisionContextProvider.registerChosenAction(agent, action);
                    if (action == null) {
                        break;
                    }
                }

                double consumed = agent.performCurrentAction(agentSettings, remainingTime);
                if (consumed <= 1E-10) {
                    break;
                }
                remainingTime -= consumed;

                if (agent.getCurrentAction() != null && agent.getCurrentAction().isCompleted()) {
                    agent.setCurrentAction(null);
                }

                if (remainingTime <= 0.0) {
                    break;
                }
            }
        }
    }

    public List<Agent> getAgents() {
        return agents;
    }

    /**
     * Removes the specified agent from the graph and resets its state,
     * but does not release its ID or add it to the list of dead agents.
     *
     * @param agent the agent to remove
     * @return the removed agent
     */
    public Agent removeAgentFromGraph(Agent agent) {
        agents.remove(agent);
        agent.putOnNode(null);
        agent.setCurrentAction(null);
        agent.setStressLevel(0);
        return agent;
    }

    /** Kills the agent and removes the main agent list, but keeps it in the list of dead agents for statistics purposes */
    public Agent killAgent(Agent agent) {
        agent.kill();
        deadAgents.add(agent);
        agents.remove(agent);
        return agent;
    }

    /** @return the unmodifiable list of dead agents */
    public List<Agent> getDeadAgents() {
        return Collections.unmodifiableList(deadAgents);
    }

    /**
     * Clears the list of dead agents and releases their IDs
     * <b>Agents removed with this method should not be used anymore</b>
     */
    public void clearDeadAgents() {
        for (Agent agent : deadAgents) {
            agent.releaseId();
        }
        deadAgents.clear();
    }

    /**
     * Deletes the agent from the graph and releases its ID, but does not add it to
     * the list of dead agents
     * <b>Agents removed with this method should not be used anymore</b>
     * 
     * @return the removed agent
     */
    public Agent deleteAgent(Agent agent) {
        agent.releaseId();
        return removeAgentFromGraph(agent);
    }

    /**
     * Updates the state of all agents in the simulation (including stress levels).
     * This method requires that the graph elements have their stress-inducing
     * factors updated beforehand
     */
    private void updateAgentsState() {
        for (Agent agent : agents) {
            agent.updateState();
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        AgentManager other = (AgentManager) obj;
        return Objects.equals(agentSettings, other.agentSettings) && Objects.equals(agents, other.agents)
                && Objects.equals(decisionContextProvider, other.decisionContextProvider);
    }

    @Override
    public int hashCode() {
        return Objects.hash(agentSettings, agents, decisionContextProvider);
    }

    @Override
    public String toString() {
        return "AgentManager{" +
                "agentSettings=" + agentSettings +
                ", agentsCount=" + (agents == null ? 0 : agents.size()) +
                ", hasDecisionProvider=" + (decisionContextProvider != null) +
                '}';
    }

}
