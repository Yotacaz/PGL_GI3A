package fr.cy.model.agent.behaviour.agentActions;

import java.util.Objects;

import fr.cy.model.agent.Agent;
import fr.cy.model.agent.AgentSettings;
import fr.cy.model.agent.exceptions.AgentStateException;
import fr.cy.model.graph.element.Edge;
import fr.cy.model.graph.element.Node;

/**
 * Abstract base class for agent actions that involve movement along edges.
 * 
 * <p>This class provides common functionality for all movement actions, including
 * progress tracking, edge traversal logic, and node transition handling. Concrete
 * subclasses implement specific types of movement behavior.</p>
 */
public abstract class AbstractMoveAction extends AgentAction {
    private static final long serialVersionUID = 1L;

    /**
     * The progress of the agent along the current edge, between 0 and 1.
     * A value of 0 indicates the agent is at the starting node, while 1 indicates
     * the agent has reached the destination node.
     */
    protected double edgeProgress = 0.0;

    /**
     * Creates a new AbstractMoveAction for the specified agent.
     * 
     * @param agent the agent that will perform this movement action
     */
    public AbstractMoveAction(Agent agent) {
        super(agent);
    }

    /**
     * Gets the current progress along the edge being traversed.
     * 
     * @return the edge progress (0.0 to 1.0)
     */
    public double getEdgeProgress() {
        return edgeProgress;
    }

    /**
     * Sets the progress along the current edge.
     * 
     * @param newEdgeProgress the new edge progress (must be >= 0.0, will be clamped to 1.0)
     * @throws IllegalArgumentException if newEdgeProgress is negative
     */
    public void setEdgeProgress(double newEdgeProgress) {
        if (newEdgeProgress < 0.0) {
            throw new IllegalArgumentException("Edge progress must be positive");
        }
        this.edgeProgress = Math.min(newEdgeProgress, 1.0);
        agent.setCurrentEdgeProgress(edgeProgress); //keep in sync
    }

    /**
     * Checks if the edge traversal is complete.
     * 
     * @return true if the edge progress is close enough to 1.0 (accounting for floating-point precision),
     *         false otherwise
     */
    public boolean isEdgeCompleted() {
        return getEdgeProgress() >= 0.999999; // consider edge completed when progress is close enough to 1.0 to avoid
        // floating-point issues
    }

    /**
     * Handles the movement of the agent along the specified edge for the given available time.
     * 
     * <p>This method calculates the distance the agent can travel based on their effective speed
     * and the available time, updates the edge progress, and handles the transition to the
     * destination node if the edge is completed.</p>
     * 
     * @param agentSettings the agent settings containing speed factors and other parameters
     * @param edge the edge along which the agent is moving
     * @param availableTime the time available for this movement (in simulation time units)
     * @return the time actually consumed by this movement
     * @throws AgentStateException if the agent is in an invalid state for edge traversal
     * @throws NullPointerException if the edge parameter is null
     */
    protected double travelAlongEdge(AgentSettings agentSettings, Edge edge, double availableTime) {
        Agent agent = getAgent();
        Objects.requireNonNull(edge, "invalid param");
        if (!agent.isOnEdge()) {
            agent.putOnEdge(edge);
        } else if (!edge.equals(agent.getCurrentEdge())) {
            throw new AgentStateException("agent is trying to travel on an edge while being in another one");
        }

        if (availableTime <= 0.0) {
            return 0.0;
        }

        double speed = agent.getEffectiveSpeed(agentSettings);
        double edgeLength = edge.getLength();
        if (edgeLength <= 0.00) {
            setEdgeProgress(1.0);
            goToNextNodeIfEdgeCompleted();
            return 0.0; // No time consumed if the edge has no length
        }
        double progress = getEdgeProgress();
        if (speed <= 0) {
            return availableTime;
        }

        double remainingDistance = edgeLength * (1.0 - progress);
        double distanceTraveled = Math.min(speed * availableTime, remainingDistance);
        double timeConsumed = distanceTraveled / speed;
        double newProgress = progress + (distanceTraveled / edgeLength);
        setEdgeProgress(newProgress);

        goToNextNodeIfEdgeCompleted();

        return timeConsumed;
    }

    /**
     * Transitions the agent to the next node if edge traversal is complete.
     * 
     * <p>This method checks if the edge has been fully traversed and, if so,
     * moves the agent to the destination node and updates the visited node count.</p>
     * 
     * @throws AgentStateException if the agent is not on an edge when edge completion is detected
     */
    private void goToNextNodeIfEdgeCompleted() {
        if (isEdgeCompleted()) {
            agent.incrementNodeVisited();
            if (agent.isOnEdge()) {
                // Node nextNode = currentEdge.getOppositeNode(agent.getPreviousOrCurrentNode());
                Node nextNode = getClosestTargetNode();
                agent.putOnNode(nextNode);
                assert getProgress() > 0.99;
            } else {
                throw new AgentStateException("Current edge cannot be null when edge is completed");
            }
        }
    }

}
