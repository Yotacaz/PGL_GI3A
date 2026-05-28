package fr.cy.model.agent.behaviour.agentActions;

import fr.cy.model.agent.Agent;
import fr.cy.model.agent.AgentSettings;
import fr.cy.model.graph.element.Edge;
import fr.cy.model.simulation.Simulation;

public abstract class AbstractMoveAction extends AgentAction {
    /** The progress of the agent along the current edge, between 0 and 1 */
    private double edgeProgress = 0.0;

    public AbstractMoveAction(Agent agent) {
        super(agent);
    }

    public double getEdgeProgress() {
        return edgeProgress;
    }

    public void setEdgeProgress(double edgeProgress) {
        if (edgeProgress < 0) {
            throw new IllegalArgumentException("Edge progress must be positive");
        }
        this.edgeProgress = Math.min(edgeProgress, 1.0);
    }

    public boolean isEdgeCompleted() {
        return edgeProgress >= 1.0;
    }

    protected double travelAlongEdge(AgentSettings agentSettings, Edge edge) {
        Agent agent = getAgent();

        agent.setCurrentEdge(edge);
        agent.setIsOnNode(false);

        double speed = agent.getEffectiveSpeed(agentSettings);
        double edgeLength = edge.getLength();
        double progress = getEdgeProgress();
        double remainingDistance = edgeLength * (1.0 - progress); // distance left before starting traveling
        remainingDistance = Math.max(0, remainingDistance - speed * Simulation.TICK_DURATION); // Distance left to travel after this tick
        double newProgress = 1.0 - (remainingDistance / edgeLength);
        setEdgeProgress(newProgress);
        if (isEdgeCompleted()) {
            agent.setPreviousNode(edge.getOppositeNode(agent.getCurrentNode()));
            agent.setCurrentEdge(null); // Agent has reached the end of the edge
            agent.setIsOnNode(true);
        }

        double timeConsumed = Simulation.TICK_DURATION;
        if (speed <= 0) {
            return timeConsumed; // If speed is zero or negative, we consider the whole tick is consumed
        }
        double distanceTraveled = Math.min(speed * Simulation.TICK_DURATION, remainingDistance);
        timeConsumed = distanceTraveled / speed;
        return timeConsumed;
    }

}
