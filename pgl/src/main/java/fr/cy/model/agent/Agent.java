package fr.cy.model.agent;

import java.util.HashMap;
import java.util.Map;

import fr.cy.model.agent.behaviour.agentActions.AgentAction;
import fr.cy.model.agent.behaviour.decisions.AgentDecisionScore;
import fr.cy.model.agent.behaviour.decisions.AgentPossibleDecision;
import fr.cy.model.agent.behaviour.decisions.DecisionNodeContext;
import fr.cy.model.agent.behaviour.properties.AgentDecisionalProperties;
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

    private AgentDecisionalProperties agentState;

    /** Tolerance to crowding, between 0 and 1, above which the agent starts panicking */
    private double crowdingTolerance;
    /** Current edge of the graph where the agent is located */
    private Edge currentEdge;
    private boolean isOnNode = true; // True if the agent is currently on a node, false if on an edge
    /** Previous node visited by the agent, used in case of backtracking */
    private Node previousNode = null;
    private AgentAction currentAction = null; // The path the agent is currently following, if any

    /** Flag indicating whether the agent is alive or has been removed from the simulation */
    private boolean isAlive = true;

    /**  Static IdManager to generate unique identifiers for agents */
    private static IdManager idManager = new IdManager();

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
    public Agent(String name, int maxSpeed, double stressTolerance, double crowdingTolerance) {
        this.id = idManager.generateId();
        this.name = name;
        this.maxSpeed = maxSpeed;
        this.crowdingTolerance = crowdingTolerance;
        this.agentState = new AgentDecisionalProperties(this.id, stressTolerance, 0.5);
    }

    AgentAction makeDecision(DecisionNodeContext decisionContext, AgentSettings agentSettings) {
        computeAgentDecisionsScore(agentSettings, decisionContext);
        return null; //TODO
    }

    private double computeAgentDecisionsScore(AgentSettings agentSettings, DecisionNodeContext decisionContext) {
        // real implementation :
        double totalScore = 0.0;
        for (AgentPossibleDecision possibleDecision : AgentPossibleDecision.values()) {
            AgentDecisionScore decisionScore = possibleDecision.computeScore(decisionContext, agentState);
            decisionsScore.put(possibleDecision, decisionScore);
            totalScore += decisionScore.getScore();
        }
        return totalScore;

    }

    void performCurrentAction() {
        if (currentAction != null) {
            currentAction.perform(this);
        }
    }

    @Override
    public double getStressInducingFactor() {
        return agentState.getStressLevel() * 0.5; // FIXME: temporary
    }

    /**
     * Release the id when agent is removed from the simulation
     */
    public void releaseId() {
        idManager.releaseId(id);
    }

    /**
     * @return the unique identifier of the agent
     */
    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public GraphElement getPosition() {
        return isOnNode ? getPreviousNode() : getCurrentEdge();
    }

    public double getStressLevel() {
        return agentState.getStressLevel();
    }

    public int getnOfNodeVisited() {
        return nOfNodeVisited;
    }

    public Node getPreviousNode() {
        return previousNode;
    }

    public Node getCurrentNode() {
        return isOnNode ? getPreviousNode() : null;
    }

    public GraphElement getCurrentGraphElement() {
        return isOnNode ? getCurrentNode() : getCurrentEdge();
    }

    public double getCrowdingTolerance() {
        return crowdingTolerance;
    }

    public double getCurrentOwnDecisionMakingFactor() {
        return agentState.getCurrentOwnDecisionMakingFactor();
    }

    // public double getCurrentSpeed() {
    //     return currentSpeed;
    // }

    public double getMaxSpeed() {
        return maxSpeed;
    }

    public double getMaxAccumulatedStress() {
        return agentState.getMaxAccumulatedStress();
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
        agentState.setStressLevel(stressLevel);
    }

    /**
     * @return the base own decision-making factor (0..1)
     */
    public double getBaseOwnDecisionMakingFactor() {
        return agentState.getBaseOwnDecisionMakingFactor();
    }

    public boolean isAlive() {
        return isAlive;
    }

    public boolean isOnNode() {
        return isOnNode;
    }

    public boolean needToMakeDecision() {
        return isOnNode();
    }

    public Edge getCurrentEdge() {
        return currentEdge;
    }

    public Edge getCurrentEdgeOrNextEdgeIfOnNode() {
        if (currentAction == null) {
            return null;
        }
        return currentAction.getCurrentEdgeOrNextEdgeIfOnNode();
    }

    public AgentAction getCurrentAction() {
        return currentAction;
    }
}
