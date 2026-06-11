package fr.cy.model.agent;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.ToDoubleFunction;

import fr.cy.model.agent.behaviour.agentActions.AgentAction;
import fr.cy.model.agent.behaviour.agentActions.FollowSingleEdgeAction;
import fr.cy.model.agent.behaviour.decisions.AgentNodeDecisionScore;
import fr.cy.model.agent.behaviour.decisions.AgentPossibleEdgeDecision;
import fr.cy.model.agent.behaviour.decisions.AgentPossibleNodeDecision;
import fr.cy.model.agent.behaviour.decisions.EdgeContext;
import fr.cy.model.agent.behaviour.decisions.NodeContext;
import fr.cy.model.agent.behaviour.properties.AgentDecisionalProperties;
import fr.cy.model.agent.behaviour.properties.AgentPhysicalProperties;
import fr.cy.model.agent.behaviour.properties.EmotionalState;
import fr.cy.model.agent.exceptions.AgentStateException;
import fr.cy.model.graph.GraphException;
import fr.cy.model.graph.element.Edge;
import fr.cy.model.graph.element.GraphElement;
import fr.cy.model.graph.element.Node;
import fr.cy.model.stress.StressInducing;
import fr.cy.util.IdManager;

/**
 * Represents an individual agent participating in the simulation.
 *
 * <p>
 * The class currently stores basic physical attributes (speed, surface area),
 * behavioral parameters (stress and crowding tolerances) and decision-making
 * helpers used by the simulation. Many behavioral methods are placeholders and
 * should be implemented when simulation rules are defined.
 * </p>
 */
public class Agent implements StressInducing, Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * Unique identifier for the agent
     */
    private final int id;

    /**
     * Name of the agent, for easier identification
     */
    private String name;

    /**
     * The physical properties of the agent including speed, health, and surface
     * area
     */
    private final AgentPhysicalProperties physicalProperties;

    /**
     * Number of nodes visited by the agent, used for statistics
     */
    private int nOfNodeVisited;

    /**
     * Map to store the scores of different possible decisions for the agent, used
     * in decision-making. This is a class attribute in order to avoid creating a
     * new map for each agent at each decision step
     */
    private final Map<AgentPossibleNodeDecision, AgentNodeDecisionScore> decisionsScore = new HashMap<>();

    /**
     * Map to store the scores of different possible edge decisions for the agent
     */
    private final Map<AgentPossibleEdgeDecision, Double> edgeDecisionsScore = new HashMap<>();

    /**
     * Map to track how many times each node decision was selected, for statistics
     */
    private final Map<AgentPossibleNodeDecision, Long> nOfTimesNodeDecisionWasSelected = new HashMap<>();
    /**
     * Map to track how many times each edge decision was selected, for statistics
     */
    private final Map<AgentPossibleEdgeDecision, Long> nOfTimesEdgeDecisionWasSelected = new HashMap<>();

    /**
     * The last decision selected on a node by the agent
     */
    private AgentPossibleNodeDecision lastSelectedDecision = null;

    /**
     * The last decision selected on an edge by the agent
     */
    private AgentPossibleEdgeDecision lastSelectedEdgeDecision = null;

    /**
     * Current behavioral state of the agent, used to influence decision-making and
     * stress levels
     */
    private AgentDecisionalProperties behavioralState;

    /**
     * Current progress along the edge, between 0 and 1
     */
    private double currentEdgeProgress;

    /**
     * Current or previous edge of the graph where the agent is located
     */
    private Edge previousOrCurrentEdge;

    /**
     * True if the agent is currently on a node, false if on an edge
     */
    private boolean isOnNode = true; // True if the agent is currently on a node, false if on an edge

    /**
     * Current node or previous node visited by the agent, used in case of
     * backtracking
     */
    private Node previousOrCurrentNode = null;

    /**
     * The current action being performed by the agent, which can be null if the
     * agent is idle on a node <b>ONLY</b>. null on an edge would be an invalid
     * state
     */
    private AgentAction currentAction = null;

    /**
     * Index of this agent inside the "agents" list of a {@code Node}.
     * <p>
     */
    private int indexInNodeAgentsList = -1;

    /**
     * Index of this agent inside the edge-forward direction list of an
     * {@link fr.cy.model.graph.element.Edge} (start -> end).
     * <p>
     * This value is meaningful only when {@link #isOnEdge()} is true and the
     * agent direction is start -> end.
     */
    private int indexInEdgeAgentsListForward = -1;

    /**
     * Index of this agent inside the edge-backward direction list of an
     * {@link fr.cy.model.graph.element.Edge} (end -> start).
     * <p>
     * This value is meaningful only when {@link #isOnEdge()} is true and the
     * agent direction is end -> start.
     */
    private int indexInEdgeAgentsListBackward = -1;

    public int getIndexInNodeAgentsList() {
        return indexInNodeAgentsList;
    }

    void setIndexInNodeAgentsList(int indexInNodeAgentsList) {
        this.indexInNodeAgentsList = indexInNodeAgentsList;
    }

    // Edge indices must be accessible from fr.cy.model.graph.element.Edge
    // (different package), so they are public.

    public int getIndexInEdgeAgentsListForward() {
        return indexInEdgeAgentsListForward;
    }

    void setIndexInEdgeAgentsListForward(int indexInEdgeAgentsListForward) {
        this.indexInEdgeAgentsListForward = indexInEdgeAgentsListForward;
    }

    public int getIndexInEdgeAgentsListBackward() {
        return indexInEdgeAgentsListBackward;
    }

    void setIndexInEdgeAgentsListBackward(int indexInEdgeAgentsListBackward) {
        this.indexInEdgeAgentsListBackward = indexInEdgeAgentsListBackward;
    }

    void resetAllGraphElementIndices() {
        indexInNodeAgentsList = -1;
        indexInEdgeAgentsListForward = -1;
        indexInEdgeAgentsListBackward = -1;
    }

    /**
     * Static IdManager to generate unique identifiers for agents
     */
    private static IdManager idManager = new IdManager();

    /**
     * Constructs a new Agent with the specified parameters.
     *
     * @param name                        the name of the agent
     * @param startingNode                the starting node where the agent will be
     *                                    placed
     * @param maxSpeed                    the maximum speed of the agent in units
     *                                    per time step
     * @param stressTolerance             the stress tolerance of the agent, between
     *                                    0 and 1
     * @param crowdingTolerance           the crowding tolerance of the agent,
     *                                    between 0 and 1
     * @param baseOwnDecisionMakingFactor the base decision-making factor (0..1)
     * @param repeatLastDecisionTendency  the tendency to repeat the last decision
     * @param health                      the initial health of the agent
     * @param surfaceAreaTakenByAgent     the surface area taken by the agent
     */
    Agent(String name, Node startingNode, double maxSpeed, double stressTolerance, double crowdingTolerance,
            double baseOwnDecisionMakingFactor, double repeatLastDecisionTendency, double health,
            double surfaceAreaTakenByAgent) {
        this.id = idManager.generateId();
        this.name = name;
        this.behavioralState = new AgentDecisionalProperties(this.id, stressTolerance, baseOwnDecisionMakingFactor,
                repeatLastDecisionTendency, crowdingTolerance);
        this.physicalProperties = new AgentPhysicalProperties(maxSpeed, health, health, surfaceAreaTakenByAgent);
        if (startingNode != null) {
            tpToNode(startingNode);
        } else {
            this.isOnNode = false; // Agent starts unplaced, not on a node
        }
    }

    /**
     * Constructor to create a new agent with the specified parameters.
     *
     * @param name              the name of the agent
     * @param maxSpeed          the maximum speed of the agent in units per time
     *                          step
     * @param stressTolerance   the stress tolerance of the agent, between 0 and 1,
     *                          above which the agent starts panicking
     * @param crowdingTolerance the crowding tolerance of the agent, between 0 and
     *                          1, above which the agent starts panicking
     * <p>Should not be multithreaded, as it uses a static {@code IdManager}.</p>
     */
    @Deprecated
    public Agent(String name, double maxSpeed, double stressTolerance, double crowdingTolerance) {
        this(name, null, maxSpeed, stressTolerance, crowdingTolerance, 0.5, 1.25, 100, 0.5);
    }

    /**
     * Makes a decision for the agent when it is on a node.
     *
     * @param decisionContext the context containing information about the current
     *                        node and possible actions
     * @param agentSettings   the general agent settings
     * @return the AgentAction to be performed based on the decision
     */
    AgentAction makeNodeDecision(NodeContext decisionContext, AgentSettings agentSettings) {
        if (decisionContext.getAccessibleElements().isEmpty()) {
            return getCurrentAction(); // No accessible elements, cannot make a decision
        }
        double totalScore = computeNodeDecisionsScore(agentSettings, decisionContext);
        return makeDecision(totalScore, decisionsScore, AgentPossibleNodeDecision.WAIT,
                AgentNodeDecisionScore::getScore,
                (selectedDecision, decisionScore) -> {
                    setLastSelectedDecision(selectedDecision);
                    return setActionFromNodeDecision(selectedDecision, decisionContext, decisionScore);
                });
    }

    /**
     * Makes a decision for the agent when it is on an edge.
     *
     * @param decisionContext the context containing information about the current
     *                        edge and possible actions
     * @param agentSettings   the general agent settings
     * @return the AgentAction to be performed based on the decision
     */
    AgentAction makeEdgeDecision(EdgeContext decisionContext, AgentSettings agentSettings) {
        if (decisionContext.getAccessibleElements().isEmpty()) {
            return getCurrentAction(); // No accessible elements, cannot make a decision
        }
        double totalScore = computeAgentEdgeDecisionsScore(agentSettings, decisionContext);
        return makeDecision(totalScore, edgeDecisionsScore, AgentPossibleEdgeDecision.CONTINUE,
                Double::doubleValue,
                (selectedDecision, decisionScore) -> {
                    setLastSelectedEdgeDecision(selectedDecision);
                    return setActionFromDecision(selectedDecision, decisionContext, decisionScore);
                });
    }

    /**
     * Generic method to make a decision based on scores and create an action.
     *
     * @param <D>              the type of decision
     * @param <S>              the type of score
     * @param totalScore       the total score of all decisions
     * @param decisionScores   the map of decisions to their scores
     * @param fallbackDecision the fallback decision to use if total score is <= 0
     * @param scoreExtractor   function to extract the score from the score object
     * @param actionFactory    function to create an action from a decision and its
     *                         score
     * @return the AgentAction to be performed, or null if no decision could be made
     */
    private <D, S> AgentAction makeDecision(double totalScore, Map<D, S> decisionScores, D fallbackDecision,
            ToDoubleFunction<S> scoreExtractor, BiFunction<D, S, AgentAction> actionFactory) {
        if (totalScore <= 0.0) {
            S fallbackScore = decisionScores.get(fallbackDecision);
            if (fallbackScore == null) {
                return null;
            }
            return actionFactory.apply(fallbackDecision, fallbackScore);
        }

        // Use the scores as weights and select an action proportionally to them.
        double randomValue = Math.random() * totalScore;
        double cumulativeProbability = 0.0;
        for (Map.Entry<D, S> entry : decisionScores.entrySet()) {
            S decisionScore = entry.getValue();
            cumulativeProbability += scoreExtractor.applyAsDouble(decisionScore);
            if (randomValue < cumulativeProbability) {
                return actionFactory.apply(entry.getKey(), decisionScore);
            }
        }
        return null;
    }

    /**
     * Set the current action of the agent based on the selected decision and its
     * score, and return the action.
     * 
     * @param decision        the selected decision for which to set the action
     * @param decisionContext the context of the decision, containing information
     *                        about the current node and possible actions
     * @param decisionScore   the score associated with the selected decision, which
     *                        may influence the action's parameters
     * @return the AgentAction corresponding to the selected decision, or null if
     *         the action cannot be created
     */
    private AgentAction setActionFromNodeDecision(AgentPossibleNodeDecision decision, NodeContext decisionContext,
            AgentNodeDecisionScore decisionScore) {
        AgentAction action = decision.toAgentAction(decisionContext, this, decisionScore);
        nOfTimesNodeDecisionWasSelected.put(decision, nOfTimesNodeDecisionWasSelected.getOrDefault(decision, 0L) + 1);
        setCurrentAction(action);
        return action;
    }

    /**
     * Sets the current action of the agent based on the selected decision and its
     * score, and return the action.
     * 
     * @param decision        the selected decision for which to set the action
     * @param decisionContext the context of the decision, containing information
     *                        about the current edge and possible actions
     * @param decisionScore   the score associated with the selected decision, which
     *                        may influence the action's parameters
     * @return the AgentAction corresponding to the selected decision, or null if
     *         the action cannot be created
     */
    private AgentAction setActionFromDecision(AgentPossibleEdgeDecision decision, EdgeContext decisionContext,
            double decisionScore) {
        AgentAction action = decision.toAgentAction(this);
        nOfTimesEdgeDecisionWasSelected.put(decision, nOfTimesEdgeDecisionWasSelected.getOrDefault(decision, 0L) + 1);
        setCurrentAction(action);
        return action;
    }

    /**
     * Computes the scores for all possible node decisions.
     *
     * @param agentSettings   the general agent settings
     * @param decisionContext the context containing information about the current
     *                        node
     * @return the total score of all possible decisions
     */
    private double computeNodeDecisionsScore(AgentSettings agentSettings, NodeContext decisionContext) {
        // No need to clear the map as it is overwritten at each decision step
        // Precompute edge score multipliers once to avoid recalculation for each
        // decision
        List<Double> edgeScoreMultipliers = computeEdgeScoreMultipliersForNodeDecision(decisionContext);

        double totalScore = 0.0;
        for (AgentPossibleNodeDecision possibleDecision : AgentPossibleNodeDecision.values()) {
            double factor = agentSettings.getDecisionMakingFactor(possibleDecision);
            AgentNodeDecisionScore decisionScore = possibleDecision.computeScore(decisionContext, this,
                    factor,
                    lastSelectedDecision, currentAction, edgeScoreMultipliers);
            decisionsScore.put(possibleDecision, decisionScore);
            totalScore += decisionScore.getScore();
        }
        return totalScore;
    }

    /**
     * Computes the scores for all possible edge decisions.
     *
     * @param agentSettings   the general agent settings
     * @param decisionContext the context containing information about the current
     *                        edge
     * @return the total score of all possible edge decisions
     * @throws AgentStateException if the agent is not on an edge
     */
    private double computeAgentEdgeDecisionsScore(AgentSettings agentSettings, EdgeContext decisionContext) {
        if (!isOnEdge())
            throw new AgentStateException("cannot make an edge decision when not on edge");
        Map<Node, Double> nodeScoreMultipliers = computeNodeScoreMultipliersForEdgeDecision(decisionContext);
        Node currentTargetNode = Objects.requireNonNull(getCurrentNodeOrNextNodeIfOnEdge());
        double totalScore = 0.0;
        for (AgentPossibleEdgeDecision possibleDecision : AgentPossibleEdgeDecision.values()) {
            double factor = agentSettings.getDecisionMakingFactor(possibleDecision);
            double decisionScore = possibleDecision.computeScore(currentTargetNode, behavioralState,
                    lastSelectedEdgeDecision, currentAction, nodeScoreMultipliers) * factor;
            edgeDecisionsScore.put(possibleDecision, decisionScore);
            totalScore += decisionScore;
        }
        return totalScore;
    }

    /**
     * Compute edge score multipliers for all outgoing edges from the current node.
     * These multipliers are based on the agent's behavioral state and the
     * destination nodes.
     * 
     * @param decisionContext the context containing the source node and outgoing
     *                        edges
     * @return a list of score multipliers in the same order as the outgoing edges
     */
    private List<Double> computeEdgeScoreMultipliersForNodeDecision(NodeContext decisionContext) {
        List<Edge> outgoingEdges = decisionContext.getOutgoingEdges();
        List<Double> multipliers = new ArrayList<>(outgoingEdges.size());
        Node sourceNode = decisionContext.getSourceNode();
        if (!isOnNode()) {
            throw new AgentStateException("Agent should be on a node to compute edge score multipliers");
        }
        Edge previousEdge = getPreviousOrCurrentEdge();
        AgentSettings agentSettings = AgentSettings.getInstance();
        for (Edge edge : decisionContext.getOutgoingEdges()) {

            double multiplier = edge.getScoreMultiplierForAgentGoingToNode(behavioralState,
                    edge.getOppositeNode(sourceNode));
            if (edge.equals(previousEdge)) {
                multiplier *= agentSettings.getBacktrackingEdgeScoreMultiplier();
            }
            multipliers.add(multiplier);
        }
        return multipliers;
    }

    /**
     * Computes score multipliers for accessible nodes when making edge decisions.
     *
     * @param decisionContext the context containing accessible nodes
     * @return a map of nodes to their score multipliers
     * @throws AgentStateException if the agent is not on an edge
     */
    private Map<Node, Double> computeNodeScoreMultipliersForEdgeDecision(EdgeContext decisionContext) {
        if (!isOnEdge()) {
            throw new AgentStateException("Agent should be on a node to compute edge score multipliers");
        }
        List<Node> accessibleNodes = decisionContext.getAccessibleNodes();
        Map<Node, Double> multipliers = new HashMap<>(accessibleNodes.size());
        double backtrackingMult = AgentSettings.getInstance().getBacktrackingEdgeScoreMultiplier();
        if (this.getCurrentNodeOrNextNodeIfOnEdge() == null) {
            this.getCurrentNodeOrNextNodeIfOnEdge();
        }
        Node targetNode = Objects.requireNonNull(this.getCurrentNodeOrNextNodeIfOnEdge());
        Edge currentEdge = Objects.requireNonNull(getCurrentEdge());
        Node previousTargetNode = Objects.requireNonNull(currentEdge.getOppositeNode(targetNode));
        for (Node node : accessibleNodes) {
            double multiplier = node.getScoreMultiplierForAgent(behavioralState);
            if (node.equals(previousTargetNode)) {
                multiplier *= backtrackingMult;
            }
            multipliers.put(node, multiplier);
        }
        return multipliers;
    }

    /**
     * Perform the current action of the agent for a given duration, updating the
     * agent's position and state accordingly.
     * 
     * @param agentSettings the general agent's settings, used to determine
     *                      effective speed and other factors influencing the action
     *                      performance
     * @param availableTime the remaining time available for the current tick, in
     *                      tick units
     * @return the time effectively consumed by performing the action, which may be
     *         less than the available time if the action completes or if the agent
     *         reaches the end of an edge
     */
    double performCurrentAction(AgentSettings agentSettings, double availableTime) {
        GraphElement position = getCurrentGraphElement();
        if (position == null || currentAction == null) {
            return 0.0;
        }
        return currentAction.perform(agentSettings, availableTime);
    }

    /**
     * @return the effective speed (m/s) of the agent considering it's state only
     */
    public double getEffectiveSpeedOutsideOfGraph() {
        double agentMaxSpeed = getMaxSpeed();
        double walkSpeedReductionFactor = AgentSettings.getInstance().getWALK_SPEED_REDUCTION_FACTOR();
        double effectiveSpeed = 0.0;
        switch (behavioralState.getEmotionnalState()) {
            case CALM:
                effectiveSpeed = agentMaxSpeed * walkSpeedReductionFactor * 0.75;
                break;
            case SELFISH:
                effectiveSpeed = agentMaxSpeed * walkSpeedReductionFactor;
                break;
            case PANICKING:
                effectiveSpeed = agentMaxSpeed; // Running
                break;
            default:
                throw new IllegalStateException("Unexpected emotional state: " + behavioralState.getEmotionnalState());
        }
        return effectiveSpeed;
    }

    /**
     * @return the effective speed of the agent, considering its state and the
     *         environment
     */
    public double getEffectiveSpeed(AgentSettings agentSettings) {
        double maxElemSpeed = Double.MAX_VALUE;
        if (!isOnNode()) {
            assert previousOrCurrentNode != null || previousOrCurrentEdge == null
                    : "Agent on edge should have a previous or current node";
            maxElemSpeed = previousOrCurrentEdge == null ? Double.MAX_VALUE
                    : previousOrCurrentEdge.getMaxAgentSpeedInDirection(previousOrCurrentNode);
        }
        double agentMaxSpeed = getEffectiveSpeedOutsideOfGraph();
        double effectiveSpeed = Math.min(agentMaxSpeed, maxElemSpeed);
        assert effectiveSpeed >= 0 : "Effective max speed should be non-negative";
        return effectiveSpeed;
    }

    @Override
    public double getStressInducingImpact() {
        return Math.max(-0.5,
                Math.min(behavioralState.getStressLevel() * 0.1 + getEmotionalState().getStressInducedToOthers(), 1.0));
    }

    /**
     * Release the id when agent is removed from the simulation.
     * This method should be called from the AgentManager when an agent is removed.
     */
    void releaseId() { // should be called from agentManager
        idManager.releaseId(id);
    }

    /**
     * Gets the unique identifier of the agent.
     * 
     * @return the unique identifier of the agent
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the current action being performed by the agent.
     * 
     * @param currentAction the action to set as current, can be null if the agent
     *                      is idle
     */
    public void setCurrentAction(AgentAction currentAction) {
        this.currentAction = currentAction;
    }

    /**
     * Gets the name of the agent.
     * 
     * @return the name of the agent
     */
    public String getName() {
        return name;
    }

    /**
     * Updates the agent's state for the given tick duration.
     *
     * @param tickDuration the duration of the current tick in simulation time units
     */
    public void updateState(double tickDuration) {
        // System.out.println(currentEdgeProgress);
        updateStressLevel(tickDuration);
        behavioralState.updateEmotionnalState();
        updateHealth(tickDuration);
    }

    /**
     * Updates the agent's stress level based on current conditions.
     *
     * @param tickDuration the duration of the current tick in simulation time units
     * @return the updated stress level
     */
    private double updateStressLevel(double tickDuration) {
        GraphElement position = getCurrentGraphElement();

        double stressFromPosition = position != null ? position.getStressInducingImpact() : 0.0;
        double stressFromHealth = (1.0 - getHealthPercentage()) * 0.5;
        double toleranceFactor = 1.0 + getStressTolerance();

        double targetStress = Math.min(
                (stressFromPosition + stressFromHealth) / toleranceFactor,
                1.0);

        double currentStress = getStressLevel();
        AgentSettings settings = AgentSettings.getInstance();
        double stressIncreaseRate = settings.getStressIncreaseRate();
        double stressDecreaseRate = settings.getStressDecreaseRate();

        if (targetStress > currentStress) { // formula for stress increase
            currentStress += (targetStress - currentStress)
                    * stressIncreaseRate
                    * tickDuration;
        } else { // formula for stress deacrease
            currentStress -= (currentStress - targetStress)
                    * stressDecreaseRate
                    * tickDuration;
        }

        currentStress = Math.max(0.0, Math.min(1.0, currentStress));

        setStressLevel(currentStress);
        return currentStress;
    }

    /**
     * Updates the agent's health based on environmental damage.
     *
     * @param tickDuration the duration of the current tick in simulation time units
     * @return the amount of damage taken during this update
     */
    private double updateHealth(double tickDuration) {
        GraphElement current = getCurrentGraphElement();
        if (current == null) {
            return 0.0;
        }
        double damage = current.getDamageForAgent(this, tickDuration);
        // double dps = damage / tickDuration;
        // System.out.println("Agent " + getId() + " takes " + damage + " damage (" +
        // dps + " DPS) from "
        // + current.getClass().getSimpleName() + " with stress level " +
        // getStressLevel());
        decreaseHealth(damage);
        return damage;
    }

    /**
     * Gets the number of nodes visited by the agent.
     * 
     * @return the number of nodes visited by the agent
     */
    public int getnOfNodeVisited() {
        return nOfNodeVisited;
    }

    /**
     * Increments the count of visited nodes by 1.
     * This should be called when the agent successfully moves to a new node.
     */
    public void incrementNodeVisited() {
        nOfNodeVisited++;
    }

    /**
     * Gets the previous or current node visited by the agent.
     *
     * @return the previous or current node, or null if none
     */
    public Node getPreviousOrCurrentNode() {
        return previousOrCurrentNode;
    }

    /**
     * Remove from graph element list but keep references,
     * should be used carefully as can break isOnNode and isOnEdge
     */
    private void removeFromGraphElemButKeepReferences() {
        GraphElement currentElement = getCurrentGraphElement();
        if (currentElement != null) {
            currentElement.removeAgent(this);
        }
        this.isOnNode = false;
        this.currentEdgeProgress = -1.0;
    }

    /**
     * Removes the agent from the graph, clearing all references.
     */
    void removeFromGraph() {
        removeFromGraphElemButKeepReferences();
        this.previousOrCurrentNode = null;
        this.previousOrCurrentEdge = null;
    }

    /**
     * Places the agent on a node.
     * Should be used when agent is on neighbouring edges or at initialization
     * 
     * @param currentNode the node to place the agent on
     * @return true if the agent was successfully placed on the node, false if the
     *         node is at capacity and cannot accept more agents
     */
    public boolean putOnNode(Node currentNode) {
        Objects.requireNonNull(currentNode,
                "currentNode cannot be null when putting agent on node, call removeFromGraph() instead");
        if (!currentNode.addAgent(this)) {
            return false;
        }

        removeFromGraphElemButKeepReferences();
        this.previousOrCurrentNode = currentNode;
        this.currentEdgeProgress = -1.0;
        setIsOnNode(true);
        return true;
    }

    public void onPreviousEdgeDeleted() {
        if (isOnEdge()) {
            throw new AgentStateException(
                    "Cannot call onPreviousEdgeDeleted when on edge, call onCurrentGraphElementDeleted instead");
        }
        // current node will be kept, just need to clear the previous edge reference
        previousOrCurrentEdge = null;
    }

    /**
     * Forces the agent to be placed on a node, regardless of capacity.
     * And resets the agent's current action, previous edge and edge progress.
     *
     * @param node the node to place the agent on
     * @return true if the agent was successfully placed on the node, false
     *         otherwise
     */
    public boolean tpToNode(Node node) {
        Objects.requireNonNull(node,
                "node cannot be null when force putting agent on node, call removeFromGraph() instead");
        if (!node.forceAddAgent(this)) {
            throw new GraphException("Failed to force add agent to node, this should not happen");
        }
        removeFromGraph();
        this.previousOrCurrentNode = node;
        // this.previousOrCurrentEdge = null;
        this.currentEdgeProgress = -1.0;
        this.currentAction = null;
        setIsOnNode(true);
        return true;
    }

    /**
     * Places by force the agent on an edge, regardless of capacity.
     * The agent will start following the edge from the defined start node to the
     * end node at the defined progress.
     * This method should be use when wanting to teleport an agent on an edge that
     * is not necessary adjacent to its current position
     * 
     * @param edge                  the edge to place the agent on
     * @param progressFromEdgeStart the progress from the start of the edge to place
     *                              the agent at, between 0 and 1
     * @return true if the agent was successfully placed on the edge
     */
    public boolean tpToEdge(Edge edge, Node startingNode, double progressFromEdgeStart) {
        Objects.requireNonNull(edge,
                "edge cannot be null when force putting agent on edge, call removeFromGraph() instead");
        if (!edge.forceAddAgent(this)) {
            throw new GraphException("Failed to force add agent to edge, this should not happen");
        }
        removeFromGraph();
        this.previousOrCurrentEdge = edge;
        this.currentAction = new FollowSingleEdgeAction(this, edge, edge.getEnd(), progressFromEdgeStart);
        if (edge.isDirected()) {
            if (!edge.getStart().equals(startingNode)) {
                throw new IllegalArgumentException("Starting node must be the start of the edge for directed edges");
            }
        }
        this.previousOrCurrentNode = startingNode;
        this.currentEdgeProgress = progressFromEdgeStart;
        // do not reset current edge progress
        setIsOnNode(false);
        return true;
    }

    /**
     * @return the current node the agent is on, or {@code null} if the agent is not
     *         on a node
     */
    public Node getCurrentNode() {
        return isOnNode ? getPreviousOrCurrentNode() : null;
    }

    /**
     * @return the current or previous edge the agent is on, or {@code null} if the
     *         agent is not on an edge
     */
    public Edge getPreviousOrCurrentEdge() {
        return previousOrCurrentEdge;
    }

    /**
     * Places the agent on an edge. It does not reset the current action
     * Should be used when the agent is on an neighbouring node
     * 
     * @param edge the edge to place the agent on
     */
    public boolean putOnEdge(Edge edge) {
        Objects.requireNonNull(edge, "edge cannot be null when putting agent on edge, call removeFromGraph() instead");
        if (!edge.addAgent(this)) {
            return false;
        }
        removeFromGraphElemButKeepReferences();
        this.previousOrCurrentEdge = edge;
        currentEdgeProgress = 0.0;
        setIsOnNode(false);
        return true;
    }

    /**
     * @return the current edge the agent is on, or {@code null} if the agent is not
     *         on an edge
     */
    public Edge getCurrentEdge() {
        return isOnNode() ? null : getPreviousOrCurrentEdge();
    }

    /**
     * @return the current edge the agent is on, or the next edge if the agent is
     *         not on an edge
     *         This method can return null if the agent has no ongoing action and is
     *         on a node
     */
    public Edge getCurrentEdgeOrNextEdgeIfOnNode() {
        if (currentAction == null) {
            if (isOnEdge())
                throw new AgentStateException("agent action is null but is on edge");
            return null;
        }
        return currentAction.getClosestTargetEdge();
    }

    /**
     * @return the current node the agent is on, or the next node if the agent is
     *         not on a node
     *         This method can technically return null but should not
     */
    public Node getCurrentNodeOrNextNodeIfOnEdge() {
        if (currentAction == null) {
            if (isOnEdge())
                throw new AgentStateException("agent action is null while being on edge");
            return getCurrentNode();
        }
        return currentAction.getClosestTargetNode();
    }

    /**
     * @return the current graph element the agent is on, or {@code null} if the
     *         agent is not on a graph element
     */
    public GraphElement getCurrentGraphElement() {
        return isOnNode ? getCurrentNode() : getPreviousOrCurrentEdge();
    }

    /**
     * Returns the progress of the agent along the current edge, between 0 and 1, or
     * -1 if not applicable
     */
    public double getCurrentEdgeProgress() {
        return isOnEdge() ? currentEdgeProgress : -1.0;
    }

    /**
     * Sets the progress of the agent along the current edge. The value should be
     * between 0 and 1, where 0 means just started on the edge and 1 means reached
     * the end of the edge. If the value is outside this range, it will be clamped.
     *
     * @param edgeProgress the progress to set, between 0 and 1
     */
    public void setCurrentEdgeProgress(double edgeProgress) {
        if (edgeProgress < 0.0) {
            throw new IllegalArgumentException("Edge progress must be positive");
        }
        currentEdgeProgress = Math.min(edgeProgress, 1.0);
    }

    /**
     * Checks if the agent has been evacuated (reached an exit node).
     *
     * @return true if the agent is on an exit node, false otherwise
     */
    public boolean isEvacuated() {
        return getCurrentNode() != null && getCurrentNode().isExit();
    }

    /**
     * Checks if the agent is currently on a node.
     * 
     * @return true if the agent is on a node and has a valid current or previous
     *         node reference, false otherwise
     */
    public boolean isOnNode() {
        return isOnNode && getPreviousOrCurrentNode() != null;
    }

    /**
     * Checks if the agent is currently on an edge.
     * 
     * @return true if the agent is on an edge and has a valid current or previous
     *         edge reference, false otherwise
     */
    public boolean isOnEdge() {
        return !isOnNode && getPreviousOrCurrentEdge() != null;
    }

    /**
     * Checks if the agent is currently on a graph element (node or edge).
     * 
     * @return true if the agent is on a node or an edge, false otherwise
     */
    public boolean isOnGraph() {
        return isOnNode() || isOnEdge();
    }

    /**
     * Sets whether the agent is on a node.
     * 
     * @param isOnNode true if the agent is on a node, false otherwise
     */
    private void setIsOnNode(boolean isOnNode) {
        this.isOnNode = isOnNode;
    }

    /**
     * Checks if the agent needs to make a decision.
     *
     * @return true if the agent is on a node and needs to make a decision, false
     *         otherwise
     */
    public boolean needToMakeDecision() {
        return isOnNode();
    }

    /**
     * Sets the last selected decision for the agent.
     * 
     * @param lastSelectedDecision the last selected decision
     */
    void setLastSelectedDecision(AgentPossibleNodeDecision lastSelectedDecision) {
        this.lastSelectedDecision = lastSelectedDecision;
    }

    /**
     * Sets the last selected edge decision for the agent.
     * 
     * @param lastSelectedEdgeDecision the last selected edge decision
     */
    void setLastSelectedEdgeDecision(AgentPossibleEdgeDecision lastSelectedEdgeDecision) {
        this.lastSelectedEdgeDecision = lastSelectedEdgeDecision;
    }

    /**
     * Gets the current action being performed by the agent.
     * 
     * @return the current action, or null if the agent is idle
     */
    public AgentAction getCurrentAction() {
        return currentAction;
    }

    /**
     * Gets the last selected node decision for the agent.
     * 
     * @return the last selected node decision, or null if no decision has been made
     */
    public AgentPossibleNodeDecision getLastSelectedDecision() {
        return lastSelectedDecision;
    }

    /**
     * Gets the last selected edge decision for the agent.
     * 
     * @return the last selected edge decision, or null if no decision has been made
     */
    public AgentPossibleEdgeDecision getLastSelectedEdgeDecision() {
        return lastSelectedEdgeDecision;
    }

    // Behavioral properties related methods

    /** @return the base own decision-making factor (0..1) */
    public double getBaseOwnDecisionMakingFactor() {
        return behavioralState.getBaseOwnDecisionMakingFactor();
    }

    /**
     * Gets the congestion tolerance of the agent.
     * 
     * @return the congestion tolerance
     */
    public double getCongestionTolerance() {
        return behavioralState.getCongestionTolerance();
    }

    /**
     * Gets the stress tolerance of the agent.
     * 
     * @return the stress tolerance (between 0 and 1)
     */
    public double getStressTolerance() {
        return behavioralState.getStressTolerance();
    }

    /**
     * Sets the congestion tolerance of the agent.
     * 
     * @param congestionTolerance the congestion tolerance to set
     */
    public void setCongestionTolerance(double congestionTolerance) {
        behavioralState.setCongestionTolerance(congestionTolerance);
    }

    /**
     * Gets the current own decision-making factor of the agent.
     * 
     * @return the current own decision-making factor (0..1)
     */
    public double getCurrentOwnDecisionMakingFactor() {
        return behavioralState.getCurrentOwnDecisionMakingFactor();
    }

    /**
     * Gets the maximum accumulated stress of the agent.
     * 
     * @return the maximum accumulated stress
     */
    public double getMaxAccumulatedStress() {
        return behavioralState.getMaxAccumulatedStress();
    }

    /**
     * Sets the stress level of the agent.
     * 
     * @param stressLevel the stress level to set
     */
    public void setStressLevel(double stressLevel) {
        behavioralState.setStressLevel(stressLevel);
    }

    /**
     * Gets the current stress level of the agent.
     * 
     * @return the stress level
     */
    public double getStressLevel() {
        return behavioralState.getStressLevel();
    }

    /**
     * Adds the specified amount of stress to the agent's current stress level.
     * 
     * @param stress the amount of stress to add
     */
    public void addStress(double stress) {
        setStressLevel(getStressLevel() + stress);
    }

    /**
     * @return the emotional state (calm, selfish, panicking)
     */
    public EmotionalState getEmotionalState() {
        return behavioralState.getEmotionnalState();
    }

    // Physical properties related methods

    /**
     * @return the surface area taken by the agent, used for congestion calculations
     */
    public double getSurfaceAreaTakenByAgent() {
        return physicalProperties.getSurfaceAreaTakenByAgent();
    }

    /** @return the maximum speed of the agent in m/s */
    public double getMaxSpeed() {
        return physicalProperties.getMaxSpeed();
    }

    /**
     * Sets the health of the agent.
     * 
     * @param health the health to set
     */
    public void setHealth(double health) {
        physicalProperties.setHealth(health);
    }

    /**
     * Gets the health of the agent.
     * 
     * @return the health
     */
    public double getHealth() {
        return physicalProperties.getHealth();
    }

    /**
     * Decreases the health of the agent.
     * 
     * @param amount the amount by which to decrease the health
     */
    public void decreaseHealth(double amount) {
        physicalProperties.decreaseHealth(amount);
    }

    /**
     * Restores the health of the agent.
     * 
     * @param amount the amount by which to restore the health
     */
    public void restoreHealth(double amount) {
        physicalProperties.restoreHealth(amount);
    }

    /**
     * Kills the agent by setting its health to zero.
     */
    void kill() {
        physicalProperties.kill();
    }

    /**
     * Gets the health percentage of the agent.
     * 
     * @return the health percentage
     */
    public double getHealthPercentage() {
        return physicalProperties.getHealthPercentage();
    }

    /**
     * Checks if the agent is alive (health > 0).
     * 
     * @return true if the agent is alive, false if the agent is dead (health is 0 or less).

     */
    public boolean isAlive() {
        return physicalProperties.isAlive();
    }


    /**
     * @return the physical properties of the agent
     */
    public AgentPhysicalProperties getPhysicalProperties() {
        return physicalProperties;
    }

    /**
     * @return the behavioral/decisional state of the agent
     */
    public AgentDecisionalProperties getBehavioralState() {
        return behavioralState;
    }

    /**
     * Gets the number of times a specific node decision was selected by the agent.
     * 
     * @param decision the node decision for which to get the count
     * @return the number of times the specified node decision was selected, or 0 if
     *         it was never selected
     */
    public long getTimesNodeDecisionWasSelected(AgentPossibleNodeDecision decision) {
        return nOfTimesNodeDecisionWasSelected.getOrDefault(decision, 0L);
    }

    /**
     * Gets the number of times a specific edge decision was selected by the agent.
     * 
     * @param decision the edge decision for which to get the count
     * @return the number of times the specified edge decision was selected, or 0 if
     *         it was never selected
     */
    public long getTimesEdgeDecisionWasSelected(AgentPossibleEdgeDecision decision) {
        return nOfTimesEdgeDecisionWasSelected.getOrDefault(decision, 0L);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Agent other = (Agent) obj;
        return this.id == other.id;
    }

    @Override
    public String toString() {
        String position;
        if (isOnNode) {
            position = previousOrCurrentNode == null ? "Node[?]" : "Node[" + previousOrCurrentNode.getId() + "]";
        } else {
            position = previousOrCurrentEdge == null ? "Edge[?]" : "Edge[" + previousOrCurrentEdge.getId() + "]";
        }
        String state = behavioralState == null ? "unknown" : behavioralState.getEmotionnalState().name();
        double stress = behavioralState == null ? 0.0 : behavioralState.getStressLevel();
        // String action = currentAction == null ? "idle" :
        // currentAction.getClass().getSimpleName();
        String lastDecision = lastSelectedDecision == null ? "none" : lastSelectedDecision.toString();
        return String.format("Agent[%d] %s — %s | state=%s (%.0f%%) lastDecision=%s pos=%s progress=%.1f%% visited=%d",
                id,
                name == null ? "<unnamed>" : name,
                isAlive() ? "alive" : "dead",
                state,
                stress * 100.0,
                lastDecision,
                position,
                getCurrentEdgeProgress() == -1.0 ? 0.0 : getCurrentEdgeProgress() * 100.0,
                nOfNodeVisited);
    }

}
