package fr.cy.model.agent;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import fr.cy.model.agent.behaviour.agentActions.AgentAction;
import fr.cy.model.agent.behaviour.decisions.DecisionNodeContext;
import fr.cy.model.agent.behaviour.properties.AgentDecisionalProperties;
import fr.cy.model.agent.behaviour.properties.AgentPhysicalProperties;
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

    private boolean isFirstTick = true;

    private AgentSettings agentSettings = AgentSettings.getInstance();
    private List<Agent> agentsOnGraph;
    private List<Agent> deadAgents = new ArrayList<>();
    private List<Agent> evacuatedAgents = new ArrayList<>();    //TODO
    private DecisionContextProvider decisionContextProvider;
    private AgentGenerator agentGenerator;
    private final SimulationSettings simulationSettings;
    // private Map<Agent, AgentAction> agentActionsPreviousTick = new HashMap<>();

    /** For storing initial snapshots of agents (for reset functionality) */
    private List<AgentSnapshot> initialAgentSnapshots = null;

    public AgentManager(List<Agent> agents, DecisionContextProvider decisionContextProvider,
            AgentGenerator agentGenerator, SimulationSettings simulationSettings) {
        this.agentsOnGraph = agents;
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
        agentsOnGraph.sort(new AgentByOwnDecisionMakingComparator());
    }

    public AgentSettings getAgentSettings() {
        return agentSettings;
    }

    public void generateRandomsAgents(int count) {
        for (int i = 0; i < count; i++) {
            Agent newAgent = agentGenerator.generateRandomAgentOnRandomNode("Agent" + (agentsOnGraph.size() + 1));
            agentsOnGraph.add(newAgent);
        }
    }

    public void generateAgentOnEdge(String baseName, Edge edge, double edgeProgress) {
        Agent newAgent = agentGenerator.generateAgent(baseName, edge, edgeProgress);
        agentsOnGraph.add(newAgent);
    }

    public void generateAgentsOnNode(String baseName, Node node, int count) {
        for (int i = 0; i < count; i++) {
            Agent newAgent = agentGenerator.generateAgent(baseName + (i + 1), node);
            agentsOnGraph.add(newAgent);
        }
    }

    public void generateAgentOnNode(String baseName, Node node) {
        Agent newAgent = agentGenerator.generateAgent(baseName, node);
        agentsOnGraph.add(newAgent);
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
        if (isFirstTick) {
            setInitialState();
            isFirstTick = false;
        }
        updateAgentsState();
        moveAgents(tickDuration);
    }

    private void moveAgents(double tickDuration) {
        decisionContextProvider.clearCache();
        sortAgentsByOwnDecisionMakingFactor(); // should be relatively fast since the list is almost sorted

        // generate and register decisions for all agents before performing any action,
        // to ensure that all agents have the same information when making their
        // decisions
        for (Agent agent : agentsOnGraph) {
            if (agent.isOnNode()) {
                DecisionNodeContext decisionContext = decisionContextProvider.getContext(agent);
                AgentAction action = agent.makeDecision(decisionContext, agentSettings);
                decisionContextProvider.registerChosenAction(agent, action);
            }
        }

        for (Agent agent : agentsOnGraph) {
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

    public List<Agent> getAgentsOnGraph() {
        return agentsOnGraph;
    }

    /**
     * Removes the specified agent from the graph and resets its state,
     * but does not release its ID or add it to the list of dead agents.
     *
     * @param agent the agent to remove
     * @return the removed agent
     */
    public Agent removeAgentFromGraph(Agent agent) {
        if (!agentsOnGraph.remove(agent)) {
            if (deadAgents.remove(agent) != true) {
                throw new IllegalArgumentException("Agent not found in either agents or deadAgents list");
            }
        }

        agent.putOnNode(null);
        agent.setCurrentAction(null);
        agent.setStressLevel(0);
        return agent;
    }

    /**
     * Kills the agent and removes the main agent list, but keeps it in the list of
     * dead agents for statistics purposes
     */
    public Agent killAgent(Agent agent) {
        agent.kill();
        deadAgents.add(agent);
        agentsOnGraph.remove(agent);
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
        for (Agent agent : agentsOnGraph) {
            agent.updateState();
        }
    }

    /**
     * Captures the current state of the AgentManager as the initial state.
     * This state can be restored later by calling reset().
     * Must be called before any significant changes to agents occur.
     */
    public void setInitialState() {

        // Create snapshots of all current agents
        this.initialAgentSnapshots = new ArrayList<>();
        for (Agent agent : this.agentsOnGraph) {
            this.initialAgentSnapshots.add(new AgentSnapshot(agent));
        }
    }

    /**
     * Resets the AgentManager to its initial state (as set by setInitialState()).
     * This includes:
     * - Removing all current agents and releasing their IDs
     * - Clearing dead agents list
     * - Recreating agents from snapshots with restored state
     *
     */
    public void reset() {
        if (this.initialAgentSnapshots == null) {
            setInitialState();
        }

        // do not reset the settings
        for (int i = this.agentsOnGraph.size() - 1; i >= 0; i--) {
            deleteAgent(this.agentsOnGraph.get(i));
        }
        this.agentsOnGraph.clear();
        for (int i = this.deadAgents.size() - 1; i >= 0; i--) {
            deleteAgent(this.deadAgents.get(i));
        }
        this.deadAgents.clear();

        // Recreate agents from snapshots
        for (AgentSnapshot snapshot : this.initialAgentSnapshots) {
            Agent restoredAgent = snapshot.restoreToAgent();
            this.agentsOnGraph.add(restoredAgent);
        }
    }

    public void resetSettings() {
        this.agentSettings.resetSettings();
    }

    /**
     * Inner class to capture and store the state of an agent at a point in time.
     * This allows agents to be restored to a previous state, including their
     * position, action, and behavioral properties.
     */
    private static class AgentSnapshot implements Serializable {
        private static final long serialVersionUID = 1L;

        // Agent identity and name
        private final String name;

        // Physical properties
        private final double maxSpeed;
        private final int maxHealth;
        private final int currentHealth;
        private final double surfaceAreaTakenByAgent;

        // Behavioral/decisional properties
        private final double stressTolerance;
        private final double baseOwnDecisionMakingFactor;
        private final double repeatLastDecisionTendency;
        private final double crowdingTolerance;
        private final double stressLevel;

        // Position and state
        private final Node previousOrCurrentNode;
        private final Edge currentOrPreviousEdge;
        private final boolean isOnNode;
        private final int nOfNodeVisited;

        // Action and related state
        private final AgentAction currentAction;

        /**
         * Creates a snapshot of the given agent's current state
         */
        AgentSnapshot(Agent agent) {
            this.name = agent.getName();

            // Physical properties
            AgentPhysicalProperties physProps = agent.getPhysicalProperties();
            this.maxSpeed = agent.getMaxSpeed();
            this.maxHealth = physProps.getMaxHealth();
            this.currentHealth = agent.getHealth();
            this.surfaceAreaTakenByAgent = agent.getSurfaceAreaTakenByAgent();

            // Behavioral properties
            AgentDecisionalProperties behavProps = agent.getBehavioralState();
            this.stressTolerance = behavProps.getStressTolerance();
            this.baseOwnDecisionMakingFactor = agent.getBaseOwnDecisionMakingFactor();
            this.repeatLastDecisionTendency = behavProps.getRepeatLastDecisionFactor();
            this.crowdingTolerance = agent.getCongestionTolerance();
            this.stressLevel = agent.getStressLevel();

            // Position and state
            this.previousOrCurrentNode = agent.getPreviousOrCurrentNode();
            this.currentOrPreviousEdge = agent.getCurrentOrPreviousEdge();
            this.isOnNode = agent.isOnNode();
            this.nOfNodeVisited = agent.getnOfNodeVisited();

            // Current action (will be serialized as-is)
            this.currentAction = agent.getCurrentAction();
        }

        /**
         * Restores an agent from this snapshot.
         * Creates a new agent with the same properties and state as when the snapshot
         * was taken. Note: The restored agent will have a new ID (since IDs are
         * managed by the static IdManager).
         *
         * @return a new agent restored from this snapshot
         */
        Agent restoreToAgent() {
            // Create a new agent with the remembered parameters
            Agent agent = new Agent(
                    this.name,
                    this.previousOrCurrentNode,
                    this.maxSpeed,
                    this.stressTolerance,
                    this.crowdingTolerance,
                    this.baseOwnDecisionMakingFactor,
                    this.repeatLastDecisionTendency,
                    this.maxHealth,
                    this.surfaceAreaTakenByAgent);

            // Restore health if it differs from max health
            if (this.currentHealth != this.maxHealth) {
                agent.setHealth(this.currentHealth);
            }

            // Restore stress level
            agent.setStressLevel(this.stressLevel);

            // Restore crowding tolerance
            agent.setCongestionTolerance(this.crowdingTolerance);

            // Restore number of nodes visited
            for (int i = 0; i < this.nOfNodeVisited; i++) {
                agent.incrementNodeVisited();
            }

            // Restore position
            if (!this.isOnNode && this.currentOrPreviousEdge != null) {
                // Agent was on an edge, restore that state
                agent.putOnEdge(this.currentOrPreviousEdge);
                // Restore the action if it exists
                if (this.currentAction != null) {
                    agent.setCurrentAction(this.currentAction);
                }
            } else if (this.isOnNode && this.previousOrCurrentNode != null) {
                // Agent was on a node, ensure it's on the node
                agent.putOnNode(this.previousOrCurrentNode);
            }
            // If agent is neither on node nor edge, it's unplaced (which is the agent's
            // initial state)

            return agent;
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        AgentManager other = (AgentManager) obj;
        return Objects.equals(agentSettings, other.agentSettings) && Objects.equals(agentsOnGraph, other.agentsOnGraph)
                && Objects.equals(decisionContextProvider, other.decisionContextProvider);
    }

    @Override
    public int hashCode() {
        return Objects.hash(agentSettings, agentsOnGraph, decisionContextProvider);
    }

    @Override
    public String toString() {
        return "AgentManager{" +
                "agentSettings=" + agentSettings +
                ", agentsCount=" + (agentsOnGraph == null ? 0 : agentsOnGraph.size()) +
                ", hasDecisionProvider=" + (decisionContextProvider != null) +
                '}';
    }

}
