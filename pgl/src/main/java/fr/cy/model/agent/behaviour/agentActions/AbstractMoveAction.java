package fr.cy.model.agent.behaviour.agentActions;

import java.util.Objects;

import fr.cy.model.agent.Agent;
import fr.cy.model.agent.AgentSettings;
import fr.cy.model.graph.element.Edge;
import fr.cy.model.graph.element.Node;

public abstract class AbstractMoveAction extends AgentAction {
    private static final long serialVersionUID = 1L;
    /** The progress of the agent along the current edge, between 0 and 1 */
    private double edgeProgress = 0.0;

    public AbstractMoveAction(Agent agent) {
        super(agent);
    }

    public double getEdgeProgress() {
        return edgeProgress;
    }

    public void setEdgeProgress(double edgeProgress) {
        if (edgeProgress < 0.0) {
            throw new IllegalArgumentException("Edge progress must be positive");
        }
        this.edgeProgress = Math.min(edgeProgress, 1.0);
    }

    public boolean isEdgeCompleted() {
        return edgeProgress >= 0.999999; // consider edge completed when progress is close enough to 1.0 to avoid
                                         // floating-point issues
    }

    protected double travelAlongEdge(AgentSettings agentSettings, Edge edge, double availableTime) {
        Agent agent = getAgent();

        agent.putOnEdge(edge);

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
            Agent agent = getAgent();
            agent.incrementNodeVisited();
            Edge currentEdge = agent.getCurrentEdge();
            if (currentEdge != null) {
                Node nextNode = currentEdge.getOppositeNode(agent.getPreviousOrCurrentNode());
                agent.putOnNode(nextNode);
            }
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), edgeProgress);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        AbstractMoveAction other = (AbstractMoveAction) obj;
        return Double.compare(edgeProgress, other.edgeProgress) == 0;
    }

    @Override
    public String toString() {
        return super.toString().replace("}", "") + ", edgeProgress=" + String.format("%.3f", edgeProgress) + '}';
    }

}
