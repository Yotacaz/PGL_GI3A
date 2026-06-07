package fr.cy.model.agent.behaviour.agentActions;

import java.util.Objects;

import fr.cy.model.agent.Agent;
import fr.cy.model.agent.AgentSettings;
import fr.cy.model.agent.exceptions.AgentStateException;
import fr.cy.model.graph.element.Edge;
import fr.cy.model.graph.element.Node;

public abstract class AbstractMoveAction extends AgentAction {
    private static final long serialVersionUID = 1L;

    /** The progress of the agent along the current edge, between 0 and 1 */
    protected double edgeProgress = 0.0;

    public AbstractMoveAction(Agent agent) {
        super(agent);
    }

    public double getEdgeProgress() {
        return edgeProgress;
    }

    public void setEdgeProgress(double newEdgeProgress) {
        if (newEdgeProgress < 0.0) {
            throw new IllegalArgumentException("Edge progress must be positive");
        }
        this.edgeProgress = Math.min(newEdgeProgress, 1.0);
        agent.setCurrentEdgeProgress(edgeProgress); //keep in sync
    }

    public boolean isEdgeCompleted() {
        return getEdgeProgress() >= 0.999999; // consider edge completed when progress is close enough to 1.0 to avoid
        // floating-point issues
    }

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
