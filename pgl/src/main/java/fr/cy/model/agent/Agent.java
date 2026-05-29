package fr.cy.model.agent;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import fr.cy.model.agent.behaviour.agentActions.AgentAction;
import fr.cy.model.agent.behaviour.decisions.AgentDecisionScore;
import fr.cy.model.agent.behaviour.decisions.AgentPossibleDecision;
import fr.cy.model.agent.behaviour.decisions.DecisionNodeContext;
import fr.cy.model.agent.behaviour.properties.AgentDecisionalProperties;
import fr.cy.model.agent.behaviour.properties.EmotionalState;
import fr.cy.model.graph.IdManager;
import fr.cy.model.graph.element.Edge;
import fr.cy.model.graph.element.GraphElement;
import fr.cy.model.graph.element.Node;
import fr.cy.model.stress.StressInducing;

/**
 * Represents an individual agent participating in the simulation.
 *
 * <p>The class currently stores basic physical attributes (speed, surface area),
 * behavioral parameters (stress and crowding tolerances) and decision-making
 * helpers used by the simulation. Many behavioral methods are placeholders and
 * should be implemented when simulation rules are defined.</p>
 */
public class Agent implements StressInducing {
    /** Unique identifier for the agent */
    private final int id;
    /** Name of the agent, for easier identification */
    private String name;
    /** Maximum speed of the agent, in units per time step */
    private double maxSpeed;
    /** Current speed of the agent, which may be reduced due to stress or crowding */
    // private double currentSpeed;

    /** Surface area taken by the agent, used to calculate crowding effects */
    private double surfaceAreaTakenByAgent = 0.5;

    /** Progress of the agent in the current component (either a node or an edge), in units */
    private double travelProgressPercentageInComponent;
    /** Number of nodes visited by the agent, used for statistics */
    private int nOfNodeVisited;

    /** Map to store the scores of different possible decisions for the agent, used in decision-making
     * This is a class attribute in order to avoid creating a new map for each agent at each decision step*/
    private final Map<AgentPossibleDecision, AgentDecisionScore> decisionsScore = new HashMap<>();
    /** The last selected decision by the agent */
    private AgentPossibleDecision lastSelectedDecision = null;
    /** Current behavioral state of the agent, used to influence decision-making and stress levels */
    private AgentDecisionalProperties behavioralState;

    /** Tolerance to crowding, between 0 and 1, above which the agent starts panicking */
    private double crowdingTolerance;
    /** Current edge of the graph where the agent is located */
    private Edge currentEdge;
    private boolean isOnNode = true; // True if the agent is currently on a node, false if on an edge
    /** Previous node visited by the agent, used in case of backtracking */
    private Node previousOrCurrentNode = null;
    private AgentAction currentAction = null; // The path the agent is currently following, if any

    /** Flag indicating whether the agent is alive or has been removed from the simulation */
    private boolean isAlive = true;

    /**  Static IdManager to generate unique identifiers for agents */
    private static IdManager idManager = new IdManager();

    public Agent(String name, Node startingNode, double maxSpeed, double stressTolerance, double crowdingTolerance,
            double baseOwnDecisionMakingFactor) {
        this.id = idManager.generateId();
        this.name = name;
        this.maxSpeed = maxSpeed;
        this.crowdingTolerance = crowdingTolerance;
        putOnNode(startingNode);
        this.behavioralState = new AgentDecisionalProperties(this.id, stressTolerance, baseOwnDecisionMakingFactor);
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
        this(name, null, maxSpeed, stressTolerance, crowdingTolerance, 0.5);
    }

    AgentAction makeDecision(DecisionNodeContext decisionContext, AgentSettings agentSettings) {
        double totalScore = computeAgentDecisionsScore(agentSettings, decisionContext);
        //using the scores convert value to probabilities and select an action based on these probabilities
        double randomValue = Math.random() * totalScore;
        double cumulativeProbability = 0.0;
        for (Map.Entry<AgentPossibleDecision, AgentDecisionScore> entry : decisionsScore.entrySet()) {
            AgentDecisionScore decisionScore = entry.getValue();
            cumulativeProbability += decisionScore.getScore();
            if (randomValue <= cumulativeProbability) {
                AgentPossibleDecision selectedDecision = entry.getKey();
                setLastSelectedDecision(selectedDecision);
                AgentAction action = selectedDecision.toAgentAction(decisionContext, this, decisionScore);
                setCurrentAction(action);
                return action;
            }
        }
        return null;
    }

    private double computeAgentDecisionsScore(AgentSettings agentSettings, DecisionNodeContext decisionContext) {
        //No need to clear the map as it is overwritten at each decision step
        double totalScore = 0.0;
        for (AgentPossibleDecision possibleDecision : AgentPossibleDecision.values()) {
            double factor = agentSettings.getDecisionMakingFactor(possibleDecision);
            AgentDecisionScore decisionScore = possibleDecision.computeScore(decisionContext, behavioralState, factor);
            decisionsScore.put(possibleDecision, decisionScore);
            totalScore += decisionScore.getScore();
        }
        return totalScore;
    }

    double performCurrentAction(AgentSettings agentSettings) {
        if (currentAction != null) {
            return currentAction.perform(agentSettings);
        }
        return 0.0;
    }

    @Override
    public double getStressInducingFactor() {
        return behavioralState.getStressLevel() * 0.5; // FIXME: temporary
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

    public GraphElement getPosition() {
        return isOnNode ? getPreviousOrCurrentNode() : getCurrentEdge();
    }

    public double getStressLevel() {
        return behavioralState.getStressLevel();
    }

    public EmotionalState getState() {
        return behavioralState.getEmotionnalState();
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

    public void setPreviousOrCurrentNode(Node previousNode) {
        this.previousOrCurrentNode = previousNode;
    }

    public void putOnNode(Node currentNode) {
        Node previousNode = getPreviousOrCurrentNode();
        if (previousNode != null) {
            previousNode.removeAgent(this);
        }
        Edge currentEdge = getCurrentEdge();
        if (currentEdge != null) {
            assert !isOnNode : "incorrect state: agent cannot be on an edge and on a node at the same time";
            currentEdge.removeAgent(this);
        }

        if (currentNode != null) {
            currentNode.addAgent(this);
        }
        this.previousOrCurrentNode = currentNode;
        setCurrentEdge(null);
        setIsOnNode(true);
    }

    public Node getCurrentNode() {
        return isOnNode ? getPreviousOrCurrentNode() : null;
    }

    public Edge getCurrentEdge() {
        return currentEdge;
    }

    public void setCurrentEdge(Edge currentEdge) {
        this.currentEdge = currentEdge;
    }

    public void putOnEdge(Edge edge) {
        Edge currentEdge = getCurrentEdge();
        if (currentEdge != null) {
            assert !isOnNode
                    : "incorrect state: agent cannot be on an edge and on a node at the same time (was on an edge but isOnNode was true)";
            currentEdge.removeAgent(this);
        }
        if (getCurrentNode() != null) {
            assert isOnNode
                    : "incorrect state: agent cannot be on an edge and on a node at the same time (was on a node but isOnNode was false)";
            getCurrentNode().removeAgent(this);
        }
        if (edge != null) {
            edge.addAgent(this);
        }
        setCurrentEdge(edge);
        setIsOnNode(false);
    }

    public Edge getCurrentEdgeOrNextEdgeIfOnNode() {
        if (currentAction == null) {
            return null;
        }
        return currentAction.getCurrentEdgeOrNextEdgeIfOnNode();
    }

    public GraphElement getCurrentGraphElement() {
        return isOnNode ? getCurrentNode() : getCurrentEdge();
    }

    public double getCrowdingTolerance() {
        return crowdingTolerance;
    }

    public double getCurrentOwnDecisionMakingFactor() {
        return behavioralState.getCurrentOwnDecisionMakingFactor();
    }

    public double getMaxSpeed() {
        return maxSpeed;
    }

    public double getMaxAccumulatedStress() {
        return behavioralState.getMaxAccumulatedStress();
    }

    public double travelBy(double distance) {
        // Placeholder implementation - in a real implementation, this would update the agent's position along the current edge
        return distance; // Return the actual distance traveled, which may be less than the requested distance if the agent reaches the end of the edge
    }

    public double getTravelProgressPercentageInComponent() {
        return travelProgressPercentageInComponent;
    }

    public double setTravelProgressPercentageInComponent(double newTravelProgressInComponent) {
        this.travelProgressPercentageInComponent = newTravelProgressInComponent;
        return this.travelProgressPercentageInComponent;
    }

    public double getSurfaceAreaTakenByAgent() {
        return surfaceAreaTakenByAgent;
    }

    public void setStressLevel(double stressLevel) {
        behavioralState.setStressLevel(stressLevel);
    }

    /**
     * @return the base own decision-making factor (0..1)
     */
    public double getBaseOwnDecisionMakingFactor() {
        return behavioralState.getBaseOwnDecisionMakingFactor();
    }

    public boolean isAlive() {
        return isAlive;
    }

    public boolean isOnNode() {
        return isOnNode;
    }

    public void setIsOnNode(boolean isOnNode) {
        this.isOnNode = isOnNode;
    }

    public boolean needToMakeDecision() {
        return isOnNode();
    }

    void setLastSelectedDecision(AgentPossibleDecision lastSelectedDecision) {
        this.lastSelectedDecision = lastSelectedDecision;
    }

    public AgentAction getCurrentAction() {
        return currentAction;
    }

    public AgentPossibleDecision getLastSelectedDecision() {
        return lastSelectedDecision;
    }

    public double getEffectiveSpeed(AgentSettings agentSettings) {
        double maxEdgeSpeed = currentEdge != null ? currentEdge.getMaxAgentSpeedInDirection(previousOrCurrentNode)
                : Double.POSITIVE_INFINITY;
        double agentMaxSpeed = getMaxSpeed();
        double effectiveMaxSpeed = Math.min(agentMaxSpeed, maxEdgeSpeed);
        assert effectiveMaxSpeed >= 0 : "Effective max speed should be non-negative";
        double speed = 0.0;
        behavioralState.updateEmotionnalState();
        double walkSpeedReductionFactor = agentSettings.getWALK_SPEED_REDUCTION_FACTOR();
        switch (behavioralState.getEmotionnalState()) {
            case CALM:
                speed = Math.min(agentMaxSpeed * walkSpeedReductionFactor, effectiveMaxSpeed);
                break;
            case SELFISH:
                speed = Math.min(agentMaxSpeed * walkSpeedReductionFactor * 1.5, effectiveMaxSpeed);
                break;
            case PANICKING:
                speed = effectiveMaxSpeed;
                break;
            default:
                throw new IllegalStateException("Unexpected emotional state: " + behavioralState.getEmotionnalState());
        }
        return speed;
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
            position = currentEdge == null ? "Edge[?]" : "Edge[" + currentEdge.getId() + "]";
        }
        String state = behavioralState == null ? "unknown" : behavioralState.getEmotionnalState().name();
        double stress = behavioralState == null ? 0.0 : behavioralState.getStressLevel();
        String action = currentAction == null ? "idle" : currentAction.getClass().getSimpleName();
        return String.format("Agent[%d] %s — %s | state=%s (%.0f%%) action=%s pos=%s progress=%.1f%% visited=%d",
                id,
                name == null ? "<unnamed>" : name,
                isAlive ? "alive" : "dead",
                state,
                stress * 100.0,
                action,
                position,
                travelProgressPercentageInComponent * 100.0,
                nOfNodeVisited);
    }

    public static void main(String[] args) {
        Agent agent = new Agent("TestAgent", 1.0, 0.5, 0.5);
        System.out.println(agent.toString());
    }
}
