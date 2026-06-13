package fr.cy.model.agent;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import fr.cy.model.agent.behaviour.agentActions.AgentAction;
import fr.cy.model.agent.behaviour.agentActions.WaitBeforeOtherAction;
import fr.cy.model.agent.context.ContextProvider;
import fr.cy.model.agent.context.EdgeContext;
import fr.cy.model.agent.context.NodeContext;
import fr.cy.model.agent.exceptions.AgentStateException;
import fr.cy.model.agent.properties.AgentDecisionalProperties;
import fr.cy.model.agent.properties.AgentPhysicalProperties;
import fr.cy.model.agent.properties.AgentProfile;
import fr.cy.model.agent.properties.AgentProfileRegistry;
import fr.cy.model.graph.Graph;
import fr.cy.model.graph.element.Edge;
import fr.cy.model.graph.element.Node;
import fr.cy.model.simulation.SimulationSettings;

/**
 * Manager responsible for higher-level operations on {@link Agent} instances.
 * 
 * <p>
 * This class handles agent creation, management, and decision-making processes.
 * It maintains lists of active, dead, and evacuated agents, and provides
 * methods
 * for generating agents with various configurations.
 * </p>
 * 
 * <p>
 * Currently holds configuration values used when evaluating decisions and
 * manages the overall agent lifecycle during simulation.
 * </p>
 */
public class AgentManager implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Indicates whether the current tick is the first one */
    private boolean isFirstTick = true;

    /** The settings for managing agents */
    private final AgentSettings agentSettings = AgentSettings.getInstance();
    /** The list of agents to evacuate */
    private final List<Agent> agentsToEvacuate;
    /** The list of dead agents */
    private final List<Agent> deadAgents = new ArrayList<>();
    /** The list of evacuated agents */
    private final List<Agent> evacuatedAgents = new ArrayList<>();
    /** Provider for decision contexts used by agents during decision-making */
    private final ContextProvider decisionContextProvider;
    /**
     * Generator for creating agents with random attributes and placing them in the
     * graph
     */
    private final AgentGenerator agentGenerator;
    /** The settings for managing agents and the simulation */
    private final SimulationSettings simulationSettings;

    // private Map<Agent, AgentAction> agentActionsPreviousTick = new HashMap<>();

    /** For storing initial snapshots of agents (for reset functionality) */
    private List<AgentSnapshot> initialAgentSnapshots = null;

    // /**
    // * The time elapsed since the last edge decision was made, used to prompt edge
    // * decisions
    // */
    // private double timeSinceLastEdgeDecision = 0.0;

    /**
     * Creates a new AgentManager with the specified lists of agents and
     * dependencies.
     * 
     * @param agents                  the initial list of agents to manage (will be
     *                                copied, so the original list can be modified
     *                                independently)
     * @param decisionContextProvider the provider for decision contexts used by
     *                                agents during decision-making
     * @param agentGenerator          the generator for creating agents with random
     *                                attributes and placing them in the graph
     * @param simulationSettings      the settings for managing agents and the
     *                                simulation
     */
    private AgentManager(List<Agent> agents, ContextProvider decisionContextProvider,
            AgentGenerator agentGenerator, SimulationSettings simulationSettings) {
        this.agentsToEvacuate = agents;
        this.decisionContextProvider = decisionContextProvider;
        this.agentGenerator = agentGenerator;
        this.simulationSettings = simulationSettings;
    }

    /**
     * Creates a new AgentManager with the specified dependencies and an empty
     * initial list of agents, then generates random agents.
     * 
     * @param count                   the number of random agents to generate
     * @param decisionContextProvider the provider for decision contexts used by
     *                                agents during decision-making
     * @param agentGenerator          the generator for creating agents with random
     *                                attributes and placing them in the graph
     * @param simulationSettings      the settings for managing agents and the
     *                                simulation
     */
    public AgentManager(int count, ContextProvider decisionContextProvider,
            AgentGenerator agentGenerator, SimulationSettings simulationSettings) {
        this(new ArrayList<>(), decisionContextProvider, agentGenerator, simulationSettings);
        generateRandomsAgents(count);
    }

    /**
     * Creates a new AgentManager with the specified dependencies and an empty
     * initial list of agents.
     * 
     * @param decisionContextProvider the provider for decision contexts used by
     *                                agents during decision-making
     * @param agentGenerator          the generator for creating agents with random
     *                                attributes and placing them in the graph
     * @param simulationSettings      the settings for managing agents and the
     *                                simulation
     */
    public AgentManager(ContextProvider decisionContextProvider, AgentGenerator agentGenerator,
            SimulationSettings simulationSettings) {
        this(new ArrayList<>(), decisionContextProvider, agentGenerator, simulationSettings);
    }

    /**
     * A comparator for sorting agents based on their own decision-making factors.
     */
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
        agentsToEvacuate.sort(new AgentByOwnDecisionMakingComparator());
    }

    /**
     * Gets the current agent settings used by this manager.
     * 
     * @return the agent settings instance
     */
    public AgentSettings getAgentSettings() {
        return agentSettings;
    }

    /**
     * Generates a specified number of random agents and adds them to the
     * simulation.
     * 
     * <p>
     * Each agent is placed on a random node in the graph and given a unique name
     * based on the current count of agents.
     * </p>
     * 
     * @param count the number of random agents to generate
     */
    public void generateRandomsAgents(int count) {
        for (int i = 0; i < count; i++) {
            Agent newAgent = agentGenerator.generateRandomAgentOnRandomNode("Agent" + (agentsToEvacuate.size() + 1));
            agentsToEvacuate.add(newAgent);
        }
    }

    /**
     * Generates an agent on a specific edge at a given progress position.
     * 
     * @param baseName     the base name for the agent
     * @param edge         the edge where the agent should be placed
     * @param edgeProgress the progress along the edge (0.0 to 1.0)
     */
    public void generateAgentOnEdge(String baseName, Edge edge, double edgeProgress) {
        Agent newAgent = agentGenerator.generateAgent(baseName, edge, edgeProgress);
        agentsToEvacuate.add(newAgent);
    }

    /**
     * Generates multiple agents on the same node.
     * 
     * @param baseName the base name for the agents
     * @param node     the node where agents should be placed
     * @param count    the number of agents to generate
     */
    public void generateAgentsOnNode(String baseName, Node node, int count) {
        for (int i = 0; i < count; i++) {
            Agent newAgent = agentGenerator.generateAgent(baseName + (i + 1), node);
            agentsToEvacuate.add(newAgent);
        }
    }

    /**
     * Generates multiple agents on the specified node and associates them to the
     * provided profile. Passing {@code null} uses {@link AgentProfile#DEFAULT}.
     *
     * @param baseName base name for created agents
     * @param node     target node
     * @param count    number of agents
     * @param profile  profile to associate to created agents
     */
    public void generateAgentsOnNode(String baseName, Node node, int count, AgentProfile profile) {
        for (int i = 0; i < count; i++) {
            Agent newAgent = agentGenerator.generateAgent(baseName + (i + 1), node);
            agentsToEvacuate.add(newAgent);
            AgentProfileRegistry.setProfile(newAgent, profile);
        }
    }

    /**
     * Generates a single agent on the specified node and associates it to the
     * provided profile.
     */
    public void generateAgentOnNode(String baseName, Node node, AgentProfile profile) {
        Agent newAgent = agentGenerator.generateAgent(baseName, node);
        agentsToEvacuate.add(newAgent);
        AgentProfileRegistry.setProfile(newAgent, profile);
    }

    /**
     * Generates a single agent on a specific node.
     * 
     * @param baseName the base name for the agent
     * @param node     the node where the agent should be placed
     */
    public void generateAgentOnNode(String baseName, Node node) {
        Agent newAgent = agentGenerator.generateAgent(baseName, node);
        agentsToEvacuate.add(newAgent);
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

    /**
     * Main update method for the agent manager, called at each tick of the
     * simulation with a specified tick duration.
     * It updates the stress levels of agents and processes their decisions and
     * actions.
     *
     * @param tickDuration the duration of the tick in seconds, used for updating
     *                     agent states and actions
     */
    public void tick(double tickDuration) {
        if (isFirstTick) {
            setInitialState();
            isFirstTick = false;
        }
        updateAgentsState(tickDuration);
        moveAgents(tickDuration);
    }

    private void moveAgents(double tickDuration) {
        decisionContextProvider.clearCache();
        sortAgentsByOwnDecisionMakingFactor();
        // rebuildEdgeSegments();
        makeAgentsDecisions(tickDuration);

        // move agents
        for (Agent agent : agentsToEvacuate) {

            AgentAction currentAction = agent.getCurrentAction();
            if (currentAction == null) {
                continue;
            }

            double consumed = agent.performCurrentAction(agentSettings, tickDuration);
            if (consumed <= 1E-32) {
                continue;
            }
        }

    }

    private void makeAgentsDecisions(double tickDuration) {
        // generate and register decisions for all agents before performing any action,
        // to ensure that all agents have the same information when making their
        // decisions
        for (Agent agent : agentsToEvacuate) {
            if (agent.getCurrentAction() != null && agent.getCurrentAction().isCompleted()) {
                if (agent.isOnEdge()) {
                    throw new AgentStateException("Agent " + agent.getName()
                            + " has a completed action but is still on edge " + agent.getCurrentEdge());
                }
                agent.setCurrentAction(null);
            }
            if (agent.isOnNode()) {

                // HEAVY CONGESTION PENALTY
                if (!agent.getCurrentNode().canAgentLeave(agent)) {
                    continue; // Penality 2 cycles
                }

                NodeContext decisionContext = decisionContextProvider.getNodeContext(agent.getCurrentNode());
                if (decisionContext == null) {
                    continue;
                }
                AgentAction action = agent.makeNodeDecision(decisionContext, agentSettings);
                boolean registered = decisionContextProvider.registerChosenAction(agent, action);
                if (action == null) {
                    continue;
                } else if (!registered) {
                    agent.setCurrentAction(new WaitBeforeOtherAction(agent, tickDuration, action));
                }

            } else if (agent.isOnEdge()) {
                EdgeContext decisionContext = decisionContextProvider.getEdgeContext(agent.getCurrentEdge());
                if (decisionContext == null) {
                    continue;
                }
                AgentAction action = agent.makeEdgeDecision(decisionContext, agentSettings);

                boolean registered = decisionContextProvider.registerChosenAction(agent, action);
                if (action == null) {
                    throw new AgentStateException("Agent " + agent.getName() + " is on edge " + agent.getCurrentEdge()
                            + " but made a null decision");
                } else if (!registered) {
                    agent.setCurrentAction(new WaitBeforeOtherAction(agent, tickDuration, action));
                }
            } else {
                agent.isOnGraph();
                throw new AgentStateException("Agent " + agent.getName()
                        + " is neither on a node nor on an edge but on the list of agents to evacuate");
            }
        }
    }

    /** @return the unmodifiable list of agents that are still to evacuate */
    public List<Agent> getAgentsToEvacuate() {
        return Collections.unmodifiableList(agentsToEvacuate);
    }

    /** @return the unmodifiable list of evacuated agents */
    public List<Agent> getEvacuatedAgents() {
        return Collections.unmodifiableList(evacuatedAgents);
    }

    /** @return the unmodifiable list of dead agents */
    public List<Agent> getDeadAgents() {
        return Collections.unmodifiableList(deadAgents);
    }

    /**
     * Evacuates the specified agent and moves them to the list of evacuated agents.
     *
     * @param agent the agent to evacuate
     * @return the evacuated agent
     */
    public Agent evacuateAgent(Agent agent) {
        if (!agentsToEvacuate.remove(agent)) {
            throw new IllegalArgumentException("Agent not found in alive agents list");
        }
        evacuatedAgents.add(agent);
        agent.removeFromGraph();
        return agent;
    }

    /**
     * Removes the specified agent from the graph,
     * but does not release its ID or add it to the list of dead agents.
     *
     * @param agent the agent to remove
     * @return the removed agent or null if the agent was not found in the list of
     *         agents to evacuate
     */
    public Agent removeAgentFromGraph(Agent agent) {
        if (!agentsToEvacuate.remove(agent)) {
            if (deadAgents.remove(agent) != true && evacuatedAgents.remove(agent) != true) {
                return null; // Agent not found in any list, nothing to remove
            }
        }
        agent.removeFromGraph();
        return agent;
    }

    /**
     * Kills the agent and removes the main agent list, but keeps it in the list of
     * dead agents for statistics purposes and does not remove him from the graph
     * 
     * @return the killed agent
     */
    public Agent killAgent(Agent agent) {
        agent.kill();
        deadAgents.add(agent);
        agentsToEvacuate.remove(agent);
        return agent;
    }

    /**
     * Clears the list of dead agents and releases their IDs
     * <b>Agents removed with this method should not be used anymore</b>
     */
    public void clearDeadAgents() {
        for (int i = deadAgents.size() - 1; i >= 0; i--) {
            Agent agent = deadAgents.get(i);
            deleteAgent(agent);
        }
        deadAgents.clear();
    }

    /**
     * Clears the list of evacuated agents and releases their IDs
     * <b>Agents removed with this method should not be used anymore</b>
     */
    public void clearAgentsToEvacuate() {
        for (int i = agentsToEvacuate.size() - 1; i >= 0; i--) {
            Agent agent = agentsToEvacuate.get(i);
            deleteAgent(agent);
        }
        agentsToEvacuate.clear();
    }

    /**
     * Deletes the agent from the graph and releases its ID, but does not add it to
     * the list of dead agents
     * <b>Agents removed with this method should not be used anymore</b>
     */
    public void deleteAgent(Agent agent) {
        agent.releaseId();
        removeAgentFromGraph(agent);
    }

    /**
     * Updates the state of all agents in the simulation (including stress levels).
     * This method requires that the graph elements have their stress-inducing
     * factors updated beforehand
     */
    private void updateAgentsState(double tickDuration) {
        List<Agent> agentsToKill = new ArrayList<>();
        List<Agent> agentsToPutInEvacuated = new ArrayList<>();
        for (Agent agent : agentsToEvacuate) {
            agent.updateState(tickDuration);
            if (agent.isEvacuated()) {
                agentsToPutInEvacuated.add(agent);
                continue;
            }
            if (!agent.isAlive()) {
                agentsToKill.add(agent);
                continue;
            }

        }
        for (Agent agent : agentsToKill) {
            killAgent(agent);
        }
        for (Agent agent : agentsToPutInEvacuated) {
            evacuateAgent(agent);
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
        for (Agent agent : this.agentsToEvacuate) {
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
    public void reset(Graph graph) {
        if (this.initialAgentSnapshots == null) {
            return; // No initial state set, nothing to reset to
        }

        // do not reset the settings
        clearAgentsToEvacuate();
        clearDeadAgents();
        this.agentsToEvacuate.clear();

        // Recreate agents from snapshots and validate their existence
        for (AgentSnapshot snapshot : this.initialAgentSnapshots) {
            Agent restoredAgent = snapshot.restoreToAgent();
            boolean isPositionValid = false;

            if (restoredAgent.isOnNode() && restoredAgent.getCurrentNode() != null) {
                isPositionValid = graph.getNodes().contains(restoredAgent.getCurrentNode());
            } else if (restoredAgent.isOnEdge() && restoredAgent.getPreviousOrCurrentEdge() != null) {
                isPositionValid = graph.getEdges().contains(restoredAgent.getPreviousOrCurrentEdge());
            }

            if (isPositionValid) {
                this.agentsToEvacuate.add(restoredAgent);
            }
        }
    }

    /**
     * Resets the agent settings to their default values.
     * This does not affect existing agents, but will change the settings used for
     * future agent generation and decision evaluation.
     */
    public void resetSettings() {
        this.agentSettings.resetSettings();
    }

    // RELOCATION

    /**
     * Intercepts agents transiting along an edge and forces them to retreat
     * to their departure node safely.
     * 
     * @param edge             The edge currently being evacuated and deleted.
     * 
     * @param nodeBeingDeleted The node triggering this edge deletion (if any).
     */
    public void relocateAgentsOnEdge(Edge edge, Node nodeBeingDeleted) {
        List<Agent> agentsToMove = edge.getAgents();

        for (int i = agentsToMove.size() - 1; i >= 0; i--) {
            Agent agent = agentsToMove.get(i);
            Node target = Objects.requireNonNull(agent.getCurrentNodeOrNextNodeIfOnEdge(),
                    "Target node cannot be null when agent is on edge, this should not happen");
            Node departure = Objects.requireNonNull(agent.getPreviousOrCurrentNode(),
                    "Previous node cannot be null when agent is on edge, this should not happen");

            // If the departure node is the one being destroyed, push agents forward instead
            Node safeDestination = Objects.requireNonNull(departure.equals(nodeBeingDeleted) ? target : departure,
                    "Safe destination cannot be null, this should not happen");

            if (safeDestination != null) {
                agent.tpToNode(safeDestination);

                agent.setCurrentAction(null);
            } else {
                throw new IllegalStateException("Failed to determine safe destination for agent: " + agent.getName());
            }
        }
    }

    /**
     * Evacuates agents standing on a node by randomly scattering them across
     * available adjacent neighboring nodes.
     * 
     * @param node                    The node currently collapsing.
     * @param targetNodeForRelocation The node to which agents should be relocated.
     */
    public void relocateAgentsOnNode(Node node, Node targetNodeForRelocation) {
        for (Edge edge : node.getEdges()) {
            relocateAgentsOnEdge(edge, node);
        }

        List<Agent> agentsToMove = node.getAgents();

        // EDGE CASE: Completely isolated node (platform collapse)
        if (targetNodeForRelocation == null) {
            for (int i = agentsToMove.size() - 1; i >= 0; i--) {
                Agent agent = agentsToMove.get(i);
                // Completely scrub the agent from all global simulation lists
                deleteAgent(agent);
            }
            return;
        }

        for (int i = agentsToMove.size() - 1; i >= 0; i--) {
            Agent agent = agentsToMove.get(i);
            agent.tpToNode(targetNodeForRelocation);
            agent.setCurrentAction(null);

        }
    }

    public double getAverageNumberOfTicksToExitForEvacuatedAgents() {
        if (evacuatedAgents.isEmpty()) {
            return 0.0;
        }
        double totalTicks = 0.0;
        for (Agent agent : evacuatedAgents) {
            totalTicks += agent.getnOfTickAliveUntilExited();
        }
        return totalTicks / evacuatedAgents.size();
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
        private final double maxHealth;
        private final double currentHealth;
        private final double surfaceAreaTakenByAgent;

        // Behavioral/decisional properties
        private final double stressTolerance;
        private final double baseOwnDecisionMakingFactor;
        private final double repeatLastDecisionTendency;
        private final double crowdingTolerance;
        private final double stressLevel;

        // Position and state
        private final double currentEdgeProgress;
        private final Node previousOrCurrentNode;
        private final Edge currentOrPreviousEdge;
        private final boolean isOnNode;
        private final int nOfNodeVisited;
        private final int nOfTickAliveUntilExited;
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
            this.currentOrPreviousEdge = agent.getPreviousOrCurrentEdge();
            this.isOnNode = agent.isOnNode();
            this.nOfNodeVisited = agent.getnOfNodeVisited();
            this.nOfTickAliveUntilExited = agent.getnOfTickAliveUntilExited();
            this.currentEdgeProgress = agent.getCurrentEdgeProgress();

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
            agent.setnOfNodeVisited(this.nOfNodeVisited);

            // Restore number of ticks alive until exited
            agent.setnOfTickAliveUntilExited(this.nOfTickAliveUntilExited);

            // Restore position
            if (!this.isOnNode && this.currentOrPreviousEdge != null) {
                // Agent was on an edge, restore that state
                agent.tpToEdge(this.currentOrPreviousEdge, this.previousOrCurrentNode, this.currentEdgeProgress);
                // Restore the action if it exists
                if (this.currentAction != null) {
                    agent.setCurrentAction(this.currentAction);
                }
            } else if (this.isOnNode && this.previousOrCurrentNode != null) {
                // Agent was on a node, ensure it's on the node
                agent.tpToNode(this.previousOrCurrentNode);
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
        return Objects.equals(agentSettings, other.agentSettings)
                && Objects.equals(agentsToEvacuate, other.agentsToEvacuate)
                && Objects.equals(decisionContextProvider, other.decisionContextProvider);
    }

    @Override
    public int hashCode() {
        return Objects.hash(agentSettings, agentsToEvacuate, decisionContextProvider);
    }

    @Override
    public String toString() {
        return "AgentManager{" +
                "agentSettings=" + agentSettings +
                ", agentsCount=" + (agentsToEvacuate == null ? 0 : agentsToEvacuate.size()) +
                ", hasDecisionProvider=" + (decisionContextProvider != null) +
                '}';
    }

}
