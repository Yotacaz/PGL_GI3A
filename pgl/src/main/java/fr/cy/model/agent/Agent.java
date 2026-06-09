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
import fr.cy.model.agent.behaviour.decisions.AgentNodeDecisionScore;
import fr.cy.model.agent.behaviour.decisions.AgentPossibleEdgeDecision;
import fr.cy.model.agent.behaviour.decisions.AgentPossibleNodeDecision;
import fr.cy.model.agent.behaviour.decisions.EdgeContext;
import fr.cy.model.agent.behaviour.decisions.NodeContext;
import fr.cy.model.agent.behaviour.properties.AgentDecisionalProperties;
import fr.cy.model.agent.behaviour.properties.AgentPhysicalProperties;
import fr.cy.model.agent.behaviour.properties.EmotionalState;
import fr.cy.model.agent.exceptions.AgentStateException;
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
    /** Unique identifier for the agent */
    private final int id;
    /** Name of the agent, for easier identification */
    private String name;

    private final AgentPhysicalProperties physicalProperties;

    /** Number of nodes visited by the agent, used for statistics */
    private int nOfNodeVisited;

    /**
     * Map to store the scores of different possible decisions for the agent, used
     * in decision-making. This is a class attribute in order to avoid creating a 
     * new map for each agent at each decision step
     */
    private final Map<AgentPossibleNodeDecision, AgentNodeDecisionScore> decisionsScore = new HashMap<>();
    private final Map<AgentPossibleEdgeDecision, Double> edgeDecisionsScore = new HashMap<>();

    /** The last selected decision by the agent */
    private AgentPossibleNodeDecision lastSelectedDecision = null;
    private AgentPossibleEdgeDecision lastSelectedEdgeDecision = null;
    /**
     * Current behavioral state of the agent, used to influence decision-making and
     * stress levels
     */
    private AgentDecisionalProperties behavioralState;

    private double currentEdgeProgress;
    /** Current or previous edge of the graph where the agent is located */
    private Edge currentOrPreviousEdge;
    /** True if the agent is currently on a node, false if on an edge */
    private boolean isOnNode = true; // True if the agent is currently on a node, false if on an edge
    /**
     * Current node or previous node visited by the agent, used in case of
     * backtracking
     */
    private Node previousOrCurrentNode = null;
    /**
     * The current action being performed by the agent, which can be null if the
     * agent is idle
     */
    private AgentAction currentAction = null;

    private int indexInGraphElemList = -1; //TODO

    private int indexInDirectionnalList = -1; //TODO
    /** Static IdManager to generate unique identifiers for agents */
    private static IdManager idManager = new IdManager();

    public Agent(String name, Node startingNode, double maxSpeed, double stressTolerance, double crowdingTolerance,
            double baseOwnDecisionMakingFactor, double repeatLastDecisionTendency, double health,
            double surfaceAreaTakenByAgent) {
        this.id = idManager.generateId();
        this.name = name;
        this.behavioralState = new AgentDecisionalProperties(this.id, stressTolerance, baseOwnDecisionMakingFactor,
                repeatLastDecisionTendency, crowdingTolerance);
        this.physicalProperties = new AgentPhysicalProperties(maxSpeed, health, health, surfaceAreaTakenByAgent);
        putOnNode(startingNode);
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
     * @apiNote should not be multithreaded, as it uses a static IdManager
     */
    @Deprecated
    public Agent(String name, double maxSpeed, double stressTolerance, double crowdingTolerance) {
        this(name, null, maxSpeed, stressTolerance, crowdingTolerance, 0.5, 1.25, 100, 0.5);
    }

    AgentAction makeNodeDecision(NodeContext decisionContext, AgentSettings agentSettings) {
        double totalScore = computeNodeDecisionsScore(agentSettings, decisionContext);
        return makeDecision(totalScore, decisionsScore, AgentPossibleNodeDecision.WAIT,
                AgentNodeDecisionScore::getScore,
                (selectedDecision, decisionScore) -> {
                    setLastSelectedDecision(selectedDecision);
                    return setActionFromNodeDecision(selectedDecision, decisionContext, decisionScore);
                });
    }

    AgentAction makeEdgeDecision(EdgeContext decisionContext, AgentSettings agentSettings) {
        double totalScore = computeAgentEdgeDecisionsScore(agentSettings, decisionContext);
        return makeDecision(totalScore, edgeDecisionsScore, AgentPossibleEdgeDecision.CONTINUE,
                Double::doubleValue,
                (selectedDecision, decisionScore) -> {
                    setLastSelectedEdgeDecision(selectedDecision);
                    return setActionFromDecision(selectedDecision, decisionContext, decisionScore);
                });
    }

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
     * Set the current action of the agent based on the selected decision and its score, and return the action.
     * @param decision the selected decision for which to set the action
     * @param decisionContext the context of the decision, containing information about the current node and possible actions
     * @param decisionScore the score associated with the selected decision, which may influence the action's parameters
     * @return the AgentAction corresponding to the selected decision, or null if the action cannot be created
     */
    private AgentAction setActionFromNodeDecision(AgentPossibleNodeDecision decision, NodeContext decisionContext,
            AgentNodeDecisionScore decisionScore) {
        AgentAction action = decision.toAgentAction(decisionContext, this, decisionScore);
        setCurrentAction(action);
        return action;
    }

    private AgentAction setActionFromDecision(AgentPossibleEdgeDecision decision, EdgeContext decisionContext,
            double decisionScore) {
        AgentAction action = decision.toAgentAction(this);
        setCurrentAction(action);
        return action;
    }

    private double computeNodeDecisionsScore(AgentSettings agentSettings, NodeContext decisionContext) {
        // No need to clear the map as it is overwritten at each decision step
        // Precompute edge score multipliers once to avoid recalculation for each
        // decision
        List<Double> edgeScoreMultipliers = computeEdgeScoreMultipliersForNodeDecision(decisionContext);

        double totalScore = 0.0;
        for (AgentPossibleNodeDecision possibleDecision : AgentPossibleNodeDecision.values()) {
            double factor = agentSettings.getDecisionMakingFactor(possibleDecision);
            AgentNodeDecisionScore decisionScore = possibleDecision.computeScore(decisionContext, behavioralState,
                    factor,
                    lastSelectedDecision, currentAction, edgeScoreMultipliers);
            decisionsScore.put(possibleDecision, decisionScore);
            totalScore += decisionScore.getScore();
        }
        return totalScore;
    }

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
        Edge previousEdge = getCurrentOrPreviousEdge();
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
                effectiveSpeed = agentMaxSpeed; //Running
                break;
            default:
                throw new IllegalStateException("Unexpected emotional state: " + behavioralState.getEmotionnalState());
        }
        return effectiveSpeed;
    }

    public double getEffectiveSpeed(AgentSettings agentSettings) {
        double maxElemSpeed = Double.MAX_VALUE;
        if (!isOnNode()) {
            assert previousOrCurrentNode != null || currentOrPreviousEdge == null
                    : "Agent on edge should have a previous or current node";
            maxElemSpeed = currentOrPreviousEdge == null ? Double.MAX_VALUE
                    : currentOrPreviousEdge.getMaxAgentSpeedInDirection(previousOrCurrentNode);
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
     * @param currentAction the action to set as current, can be null if the agent is idle
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

    public void updateState(double tickDuration) {
        // System.out.println(currentEdgeProgress);
        updateStressLevel(tickDuration);
        behavioralState.updateEmotionnalState();
        updateHealth(tickDuration);
    }

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

        if (targetStress > currentStress) { //formula for stress increase
            currentStress += (targetStress - currentStress)
                    * stressIncreaseRate
                    * tickDuration;
        } else { //formula for stress deacrease
            currentStress -= (currentStress - targetStress)
                    * stressDecreaseRate
                    * tickDuration;
        }

        currentStress = Math.max(0.0, Math.min(1.0, currentStress));

        setStressLevel(currentStress);
        return currentStress;
    }

    private double updateHealth(double tickDuration) {
        GraphElement current = getCurrentGraphElement();
        if (current == null) {
            return 0.0;
        }
        double damage = current.getDamage(tickDuration);
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

    public Node getPreviousOrCurrentNode() {
        return previousOrCurrentNode;
    }

    private void removeFromGraphElemButKeepReferences() {
        GraphElement currentElement = getCurrentGraphElement();
        if (currentElement != null) {
            currentElement.removeAgent(this);
        }
    }

    void removeFromGraph() {
        removeFromGraphElemButKeepReferences();
        this.previousOrCurrentNode = null;
        this.currentOrPreviousEdge = null;
    }

    public void putOnNode(Node currentNode) {
        removeFromGraphElemButKeepReferences();
        if (currentNode != null) {
            currentNode.addAgent(this);
        }
        this.previousOrCurrentNode = currentNode;
        // this.currentEdgeProgress = -1.0;
        setIsOnNode(true);
    }

    /**
     * @return the current node the agent is on, or {@code null} if the agent is not
     *         on a node
     */
    public Node getCurrentNode() {
        return isOnNode ? getPreviousOrCurrentNode() : null;
    }

    public Edge getCurrentOrPreviousEdge() {
        return currentOrPreviousEdge;
    }

    public void putOnEdge(Edge edge) {
        removeFromGraphElemButKeepReferences();
        if (edge != null) {
            edge.addAgent(this);
        }
        this.currentOrPreviousEdge = edge;
        //do not reset current edge progress
        setIsOnNode(false);
    }

    public Edge getCurrentEdge() {
        return isOnNode() ? null : getCurrentOrPreviousEdge();
    }

    public Edge getCurrentEdgeOrNextEdgeIfOnNode() {
        if (currentAction == null) {
            if (!isOnNode())
                throw new AgentStateException("agent action is null but is on node");
            return null;
        }
        return currentAction.getClosestTargetEdge();
    }

    public Node getCurrentNodeOrNextNodeIfOnEdge() {
        if (currentAction == null) {
            if (!isOnNode())
                throw new AgentStateException("agent action is null but is not on node");
            return getCurrentNode();
        }
        return currentAction.getClosestTargetNode();
    }

    public GraphElement getCurrentGraphElement() {
        return isOnNode ? getCurrentNode() : getCurrentOrPreviousEdge();
    }

    /**
     * Returns the progress of the agent along the current edge, between 0 and 1, or
     * -1 if not applicable
     */
    public double getCurrentEdgeProgress() {
        return isOnEdge() ? currentEdgeProgress : -1.0;
    }

    public void setCurrentEdgeProgress(double edgeProgress) {
        if (edgeProgress < 0.0) {
            throw new IllegalArgumentException("Edge progress must be positive");
        }
        currentEdgeProgress = Math.min(edgeProgress, 1.0);
    }

    public boolean isEvacuated() {
        return getCurrentNode() != null && getCurrentNode().isExit();
    }

    public boolean isOnNode() {
        return isOnNode && getPreviousOrCurrentNode() != null;
    }

    public boolean isOnEdge() {
        return !isOnNode && getCurrentOrPreviousEdge() != null;
    }

    public boolean isOnGraph() {
        return isOnNode() || isOnEdge();
    }

    private void setIsOnNode(boolean isOnNode) {
        this.isOnNode = isOnNode;
    }

    public boolean needToMakeDecision() {
        return isOnNode();
    }

    void setLastSelectedDecision(AgentPossibleNodeDecision lastSelectedDecision) {
        this.lastSelectedDecision = lastSelectedDecision;
    }

    void setLastSelectedEdgeDecision(AgentPossibleEdgeDecision lastSelectedEdgeDecision) {
        this.lastSelectedEdgeDecision = lastSelectedEdgeDecision;
    }

    public AgentAction getCurrentAction() {
        return currentAction;
    }

    public AgentPossibleNodeDecision getLastSelectedDecision() {
        return lastSelectedDecision;
    }

    // Behavioral properties related methods

    /** @return the base own decision-making factor (0..1) */
    public double getBaseOwnDecisionMakingFactor() {
        return behavioralState.getBaseOwnDecisionMakingFactor();
    }

    public double getCongestionTolerance() {
        return behavioralState.getCongestionTolerance();
    }

    public double getStressTolerance() {
        return behavioralState.getStressTolerance();
    }

    public void setCongestionTolerance(double congestionTolerance) {
        behavioralState.setCongestionTolerance(congestionTolerance);
    }

    public double getCurrentOwnDecisionMakingFactor() {
        return behavioralState.getCurrentOwnDecisionMakingFactor();
    }

    public double getMaxAccumulatedStress() {
        return behavioralState.getMaxAccumulatedStress();
    }

    public void setStressLevel(double stressLevel) {
        behavioralState.setStressLevel(stressLevel);
    }

    public double getStressLevel() {
        return behavioralState.getStressLevel();
    }

    public void addStress(double stress) {
        setStressLevel(getStressLevel() + stress);
    }

    public EmotionalState getEmotionalState() {
        return behavioralState.getEmotionnalState();
    }

    // Physical properties related methods

    public double getSurfaceAreaTakenByAgent() {
        return physicalProperties.getSurfaceAreaTakenByAgent();
    }

    public double getMaxSpeed() {
        return physicalProperties.getMaxSpeed();
    }

    public void setHealth(double health) {
        physicalProperties.setHealth(health);
    }

    public double getHealth() {
        return physicalProperties.getHealth();
    }

    public void decreaseHealth(double amount) {
        physicalProperties.decreaseHealth(amount);
    }

    public void restoreHealth(double amount) {
        physicalProperties.restoreHealth(amount);
    }

    void kill() {
        physicalProperties.kill();
    }

    public double getHealthPercentage() {
        return physicalProperties.getHealthPercentage();
    }

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
            position = currentOrPreviousEdge == null ? "Edge[?]" : "Edge[" + currentOrPreviousEdge.getId() + "]";
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
