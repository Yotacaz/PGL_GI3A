package fr.cy.model.agent;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import fr.cy.model.agent.decisions.AgentDecisionScore;
import fr.cy.model.agent.decisions.AgentPossibleDecision;
import fr.cy.model.agent.personalityTraits.AgentPersonalityTrait;
import fr.cy.model.graph.IdManager;
import fr.cy.model.graph.element.GraphElement;
import fr.cy.model.graph.element.Node;
import fr.cy.model.stress.StressInducing;

public class Agent implements StressInducing {
    /** Unique identifier for the agent */
    private int id;
    /** Name of the agent, for easier identification */
    private String name;
    /** Maximum speed of the agent, in units per time step */
    private int maxSpeed;
    /** Current speed of the agent, which may be reduced due to stress or crowding */
    private int currentSpeed;

    /** Flag indicating whether the agent is alive or has been removed from the simulation */
    private boolean isAlive = true;

    /** Surface area taken by the agent, used to calculate crowding effects */
    private double surfaceAreaTakenByAgent = 0.5;

    /** Progress of the agent in the current component (either a node or an edge), in units */
    private int travelProgressInComponent;
    /** Number of nodes visited by the agent, used for statistics */
    private int nOfNodeVisited;
    /** Maximum accumulated stress experienced by the agent during its journey, used
    for statistics */
    private double maxAccumulatedStress = 0;

    /** Factor representing the agent's own decision-making ability, between 0 and 
     *  1, where 0 means the agent will always follow the crowd */
    private double baseOwnDescisionMakingFactor;

    /** List of personality traits that can influence the agent's behavior */
    private List<AgentPersonalityTrait> personalityTraits = new ArrayList<>();

    /** Map to store the scores of different possible decisions for the agent, used in decision-making
     * This is a class attribute in order to avoid creating a new map for each agent at each decision step*/
    private Map<AgentPossibleDecision, AgentDecisionScore> decisionsScore = new EnumMap<>(AgentPossibleDecision.class);

    /** Current state of the agent, which can be CALM, SELFISH, or PANICKING */
    private AgentState state = AgentState.CALM;
    /** Stress level of the agent, between 0 and 1 */
    private double stressLevel = 0.0;
    /** Tolerance to stress, between 0 and 1, above which the agent starts panicking */
    private double stressTolerance;

    /** Tolerance to crowding, between 0 and 1, above which the agent starts panicking */
    private double crowdingTolerance;
    /** Current component of the graph where the agent is located (either a node or an edge) */
    private GraphElement currentComponent;
    /** Previous node visited by the agent, used in case of backtracking */
    private Node previousNode = null;

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
        this.currentSpeed = maxSpeed; // Start at max speed
        this.stressTolerance = stressTolerance;
        this.crowdingTolerance = crowdingTolerance;
        this.baseOwnDescisionMakingFactor = 0.5; // FIXME: temporary, should be between 0 and 1
    }

    public Map<AgentPossibleDecision, AgentDecisionScore> evaluatePossibleDecision(AgentPossibleDecision decision) {
        // TODO: Placeholder implementation, should evaluate the decision based on the agent's state and environment
        double baseScore = Math.random() < baseOwnDescisionMakingFactor ? 1.0 : 0.0; // Randomly decide based on own decision-making factor
        decisionsScore.put(decision, new AgentDecisionScore(baseScore, true));
        return decisionsScore;
    }

    public double getCurrentOwnDecisionMakingFactor() {
        return baseOwnDescisionMakingFactor * stressLevel;
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

    public double getCrowdingTolerance() {
        return crowdingTolerance;
    }

    public int getCurrentSpeed() {
        return currentSpeed;
    }

    public int getMaxSpeed() {
        return maxSpeed;
    }

    public double getStressTolerance() {
        return stressTolerance;
    }

    public double getMaxAccumulatedStress() {
        return maxAccumulatedStress;
    }

    public int getTravelProgressInComponent() {
        return travelProgressInComponent;
    }

    public double getSurfaceAreaTakenByAgent() {
        return surfaceAreaTakenByAgent;
    }

    public double getBaseOwnDescisionMakingFactor() {
        return baseOwnDescisionMakingFactor;
    }

    public boolean isAlive() {
        return isAlive;
    }
}
