package fr.cy.model.agent;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import fr.cy.model.agent.behaviour.AgentState;
import fr.cy.model.agent.behaviour.agentActions.AgentAction;
import fr.cy.model.agent.behaviour.agentActions.FollowAgentAction;
import fr.cy.model.agent.behaviour.agentActions.RandomAgentAction;
import fr.cy.model.agent.behaviour.decisions.AgentDecisionScore;
import fr.cy.model.agent.behaviour.decisions.AgentPossibleDecision;
import fr.cy.model.agent.behaviour.decisions.DecisionContext;
import fr.cy.model.agent.behaviour.personalityTraits.AgentPersonalityTrait;
import fr.cy.model.graph.IdManager;
import fr.cy.model.graph.element.Edge;
import fr.cy.model.graph.element.GraphElement;
import fr.cy.model.graph.element.Node;
import fr.cy.model.pathfinding.GraphPath;
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

    /** Flag indicating whether the agent is alive or has been removed from the simulation */
    private boolean isAlive = true;

    /** Surface area taken by the agent, used to calculate crowding effects */
    private double surfaceAreaTakenByAgent = 0.5;

    /** Progress of the agent in the current component (either a node or an edge), in units */
    private double travelProgressPercentageInComponent;
    /** Number of nodes visited by the agent, used for statistics */
    private int nOfNodeVisited;
    /** Maximum accumulated stress experienced by the agent during its journey, used
    for statistics */
    private double maxAccumulatedStress = 0;

    /** Factor representing the agent's own decision-making ability, between 0 and
     *  1, where 0 means the agent will always follow the crowd */
    private double baseOwnDecisionMakingFactor;

    /** List of personality traits that can influence the agent's behavior */
    private final Set<AgentPersonalityTrait> personalityTraits = new HashSet<>(); //TODO: implement feature

    /** Map to store the scores of different possible decisions for the agent, used in decision-making
     * This is a class attribute in order to avoid creating a new map for each agent at each decision step*/
    private final Map<AgentPossibleDecision, AgentDecisionScore> decisionsScore = new EnumMap<>(
            AgentPossibleDecision.class);

    /** Current state of the agent, which can be CALM, SELFISH, or PANICKING */
    private AgentState state = AgentState.CALM;
    /** Stress level of the agent, between 0 and 1 */
    private double stressLevel = 0.0;
    /** Tolerance to stress, between 0 and 1, above which the agent starts panicking */
    private double stressTolerance;

    /** Tolerance to crowding, between 0 and 1, above which the agent starts panicking */
    private double crowdingTolerance;
    /** Current edge of the graph where the agent is located */
    private Edge currentEdge;
    private boolean isOnNode = true; // True if the agent is currently on a node, false if on an edge
    /** Previous node visited by the agent, used in case of backtracking */
    private Node previousNode = null;
    private AgentAction currentAction = null; // The path the agent is currently following, if any

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
        this.stressTolerance = stressTolerance;
        this.crowdingTolerance = crowdingTolerance;
        this.baseOwnDecisionMakingFactor = 0.5; // FIXME: temporary, should be between 0 and 1
    }

    AgentAction makeDecision(DecisionContext decisionContext) {
        // Placeholder implementation - in a real implementation, this would use the decision context to determine the appropriate action
        currentAction = new RandomAgentAction(this);
        return currentAction;
    }

    void performCurrentAction() {
        if (currentAction != null) {
            currentAction.perform(this);
        }
    }

    public Map<AgentPossibleDecision, AgentDecisionScore> evaluatePossibleDecision(AgentPossibleDecision decision) {
        // TODO: Placeholder implementation, should evaluate the decision based on the agent's state and environment
        double baseScore = Math.random() < baseOwnDecisionMakingFactor ? 1.0 : 0.0; // Randomly decide based on own decision-making factor
        decisionsScore.put(decision, new AgentDecisionScore(baseScore, true));
        return decisionsScore;
    }

    public double getCurrentOwnDecisionMakingFactor() {
        return baseOwnDecisionMakingFactor * stressLevel;
    }

    /**
     * Update the state of the agent based on its current stress level and
     * tolerance.
     * 
     * @return the new state of the agent after the update
     */
    public AgentState updateState() {
        Optional<AgentState> optState = AgentState.fromdouble(stressLevel, stressTolerance);
        if (optState.isEmpty()) {
            System.err.println("Warning: Agent " + id + " has an invalid stress level of " + stressLevel
                    + " with a tolerance of " + stressTolerance + ". Defaulting to PANICKING state.");
        }
        state = optState.orElse(AgentState.PANICKING); // Default to PANICKING if no state matches
        return state;
    }

    @Override
    public double getStressInducingFactor() {
        return getStressLevel() * 0.5; // FIXME: temporary
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

    /**
     * @return the current state of the agent
     */
    public AgentState getState() {
        return state;
    }

    public String getName() {
        return name;
    }

    public double getStressLevel() {
        return stressLevel;
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

    // public double getCurrentSpeed() {
    //     return currentSpeed;
    // }

    public double getMaxSpeed() {
        return maxSpeed;
    }

    public double getStressTolerance() {
        return stressTolerance;
    }

    public double getMaxAccumulatedStress() {
        return maxAccumulatedStress;
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

    /**
     * @return the base own decision-making factor (0..1)
     */
    public double getBaseOwnDecisionMakingFactor() {
        return baseOwnDecisionMakingFactor;
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

    public Edge getCurrentEdgeOrNextEdgeIfOnNode(){
        if(currentAction == null) {
            return null;
        }
        return currentAction.getCurrentEdgeOrNextEdgeIfOnNode();
    }

    public AgentAction getCurrentAction() {
        return currentAction;
    }
}
