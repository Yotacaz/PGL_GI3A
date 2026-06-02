package fr.cy.model.agent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import java.io.*;

import fr.cy.model.agent.behaviour.agentActions.AgentAction;
import fr.cy.model.agent.behaviour.decisions.AgentDecisionScore;
import fr.cy.model.agent.behaviour.decisions.AgentPossibleNodeDecision;
import fr.cy.model.agent.behaviour.decisions.NodeDecisionContext;
import fr.cy.model.agent.behaviour.properties.AgentDecisionalProperties;
import fr.cy.model.agent.behaviour.properties.AgentPhysicalProperties;
import fr.cy.model.agent.behaviour.properties.EmotionalState;
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

    private AgentPhysicalProperties physicalProperties;

    /** Number of nodes visited by the agent, used for statistics */
    private int nOfNodeVisited;

    /**
     * Map to store the scores of different possible decisions for the agent, used
     * in decision-making
     * This is a class attribute in order to avoid creating a new map for each agent
     * at each decision step
     */
    private final Map<AgentPossibleNodeDecision, AgentDecisionScore> decisionsScore = new HashMap<>();

    /** The last selected decision by the agent */
    private AgentPossibleNodeDecision lastSelectedDecision = null;
    /**
     * Current behavioral state of the agent, used to influence decision-making and
     * stress levels
     */
    private AgentDecisionalProperties behavioralState;

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
    /** Static IdManager to generate unique identifiers for agents */
    private static IdManager idManager = new IdManager();

    public Agent(String name, Node startingNode, double maxSpeed, double stressTolerance, double crowdingTolerance,
            double baseOwnDecisionMakingFactor, double repeatLastDecisionTendency, int health,
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
    public Agent(String name, double maxSpeed, double stressTolerance, double crowdingTolerance) {
        this(name, null, maxSpeed, stressTolerance, crowdingTolerance, 0.5, 1.25, 100, 0.5);
    }

    AgentAction makeDecision(NodeDecisionContext decisionContext, AgentSettings agentSettings) {
        double totalScore = computeAgentDecisionsScore(agentSettings, decisionContext);
        // using the scores convert value to probabilities and select an action based on
        // these probabilities
        double randomValue = Math.random() * totalScore;
        double cumulativeProbability = 0.0;
        for (Map.Entry<AgentPossibleNodeDecision, AgentDecisionScore> entry : decisionsScore.entrySet()) {
            AgentDecisionScore decisionScore = entry.getValue();
            cumulativeProbability += decisionScore.getScore();
            if (randomValue <= cumulativeProbability) {
                AgentPossibleNodeDecision selectedDecision = entry.getKey();
                setLastSelectedDecision(selectedDecision);
                AgentAction action = selectedDecision.toAgentAction(decisionContext, this, decisionScore);
                setCurrentAction(action);
                return action;
            }
        }
        return null;
    }

    private double computeAgentDecisionsScore(AgentSettings agentSettings, NodeDecisionContext decisionContext) {
        // No need to clear the map as it is overwritten at each decision step
        // Precompute edge score multipliers once to avoid recalculation for each
        // decision
        List<Double> edgeScoreMultipliers = computeEdgeScoreMultipliers(decisionContext);

        double totalScore = 0.0;
        for (AgentPossibleNodeDecision possibleDecision : AgentPossibleNodeDecision.values()) {
            double factor = agentSettings.getDecisionMakingFactor(possibleDecision);
            AgentDecisionScore decisionScore = possibleDecision.computeScore(decisionContext, behavioralState, factor,
                    lastSelectedDecision, currentAction, edgeScoreMultipliers);
            decisionsScore.put(possibleDecision, decisionScore);
            totalScore += decisionScore.getScore();
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
    private List<Double> computeEdgeScoreMultipliers(NodeDecisionContext decisionContext) {
        List<Edge> outgoingEdges = decisionContext.getOutgoingEdges();
        List<Double> multipliers = new ArrayList<>(outgoingEdges.size());
        Node sourceNode = decisionContext.getSourceNode();
        if (!isOnNode()) {
            throw new IllegalStateException("Agent should be on a node to compute edge score multipliers");
        }
        Edge previousEdge = getCurrentOrPreviousEdge();
        for (Edge edge : decisionContext.getOutgoingEdges()) {

            double multiplier = edge.getScoreMultiplierForAgentGoingToNode(behavioralState,
                    edge.getOppositeNode(sourceNode));
            multipliers.add(multiplier);
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

    public double getEffectiveSpeed(AgentSettings agentSettings) {
        double maxElemSpeed = Double.MAX_VALUE;
        if (!isOnNode()) {
            assert previousOrCurrentNode != null || currentOrPreviousEdge == null
                    : "Agent on edge should have a previous or current node";
            maxElemSpeed = currentOrPreviousEdge == null ? Double.MAX_VALUE
                    : currentOrPreviousEdge.getMaxAgentSpeedInDirection(previousOrCurrentNode);
        }
        double agentMaxSpeed = getMaxSpeed();
        double effectiveMaxSpeed = Math.min(agentMaxSpeed, maxElemSpeed);
        assert effectiveMaxSpeed >= 0 : "Effective max speed should be non-negative";
        double speed = 0.0;
        double walkSpeedReductionFactor = agentSettings.getWALK_SPEED_REDUCTION_FACTOR();
        switch (behavioralState.getEmotionnalState()) {
            case CALM:
                speed = Math.min(agentMaxSpeed * walkSpeedReductionFactor, effectiveMaxSpeed);
                break;
            case SELFISH:
                speed = Math.min(agentMaxSpeed * walkSpeedReductionFactor * 1.5, effectiveMaxSpeed);
                // System.out.println("Agent " + id + " is selfish and tries to run at speed " +
                // speed);
                break;
            case PANICKING:
                speed = effectiveMaxSpeed;
                // System.out.println("Agent " + id + " is panicking and tries to run at max
                // speed " + speed);
                break;
            default:
                throw new IllegalStateException("Unexpected emotional state: " + behavioralState.getEmotionnalState());
        }
        return speed;
    }

    @Override
    public double getStressInducingImpact() {
        return Math.max(-0.5,
                Math.min(behavioralState.getStressLevel() * 0.1 + getEmotionalState().getStressInducedToOthers(), 1.0));
    }

    /**
     * Release the id when agent is removed from the simulation
     */
    void releaseId() { // should be called from agentManager
        idManager.releaseId(id);
    }

    /**
     * @return the unique identifier of the agent
     */
    public int getId() {
        return id;
    }

    void setCurrentAction(AgentAction currentAction) {
        this.currentAction = currentAction;
    }

    public String getName() {
        return name;
    }

    public void updateState() {
        updateStressLevel();
        behavioralState.updateEmotionnalState();
        updateHealth();
    }

    private double updateStressLevel() {
        GraphElement position = getCurrentGraphElement();
        double stressFromPosition = position != null ? position.getStressInducingImpact() : 0.0;
        double newStressLevel = getStressLevel() * (0.5 + stressFromPosition);
        setStressLevel(newStressLevel);
        return newStressLevel;
    }

    private void updateHealth() {
        GraphElement current = getCurrentGraphElement();
        if (current == null) {
            return;
        }
        // TODO + should this be before or after action
    }

    public int getnOfNodeVisited() {
        return nOfNodeVisited;
    }

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
        setIsOnNode(false);
    }

    public Edge getCurrentEdge() {
        return isOnNode() ? null : getCurrentOrPreviousEdge();
    }

    public Edge getCurrentEdgeOrNextEdgeIfOnNode() {
        if (currentAction == null) {
            return null;
        }
        return currentAction.getClosestTargetEdge();
    }

    public GraphElement getCurrentGraphElement() {
        return isOnNode ? getCurrentNode() : getCurrentOrPreviousEdge();
    }

    public double travelBy(double distance) {
        // Placeholder implementation - in a real implementation, this would update the
        // agent's position along the current edge
        return distance; // Return the actual distance traveled, which may be less than the requested
                         // distance if the agent reaches the end of the edge
    }

    /**
     * Returns the progress of the agent along the current edge, between 0 and 1, or
     * -1 if not applicable
     */
    public double getTravelProgressPercentageOnEdge() {
        AgentAction action = getCurrentAction();
        return action != null ? action.getEdgeProgress() : -1.0;
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

    public void setIsOnNode(boolean isOnNode) {
        this.isOnNode = isOnNode;
    }

    public boolean needToMakeDecision() {
        return isOnNode();
    }

    void setLastSelectedDecision(AgentPossibleNodeDecision lastSelectedDecision) {
        this.lastSelectedDecision = lastSelectedDecision;
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

    public void setHealth(int health) {
        physicalProperties.setHealth(health);
    }

    public int getHealth() {
        return physicalProperties.getHealth();
    }

    public void decreaseHealth(int amount) {
        physicalProperties.decreaseHealth(amount);
    }

    public void restoreHealth(int amount) {
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
                getTravelProgressPercentageOnEdge() == -1.0 ? 0.0 : getTravelProgressPercentageOnEdge() * 100.0,
                nOfNodeVisited);
    }

    public static void main(String[] args) {
        Agent agent = new Agent("TestAgent", 1.0, 0.5, 0.5);
        System.out.println(agent.toString());
    }
}
