package fr.cy.model.agent.behaviour.agentActions;

import java.util.Objects;

import fr.cy.model.agent.Agent;
import fr.cy.model.agent.AgentSettings;
import fr.cy.model.graph.element.Edge;
import fr.cy.model.pathfinding.GraphPath;

public class FollowPathAction extends AbstractMoveAction {
    private GraphPath path = null;
    private int currentEdgeIndex = 0;

    public FollowPathAction(Agent agent, GraphPath path) {
        super(agent);
        this.path = Objects.requireNonNull(path, "Path cannot be null");
    }

    @Override
    public Edge getClosestTargetGraphElement() {
        if (path == null || currentEdgeIndex >= path.getEdges().size()) {
            return null; // No more edges to follow
        }
        return path.getEdgeAt(currentEdgeIndex);
    }

    @Override
    public boolean isCompleted() {
        return path != null && currentEdgeIndex >= path.getEdges().size();
    }

    @Override
    public double perform(AgentSettings agentSettings, double availableTime) {
        Edge currentEdge = getClosestTargetGraphElement();
        if (currentEdge == null) {
            setProgress(1.0);   //no more edge
            return 0.0; //nothing done = no time consumed
        }
        //no looping done for each edge of the path because we want to allow the agent to make another decision
        double consumedTime = travelAlongEdge(agentSettings, currentEdge, availableTime);
        if (isEdgeCompleted()) {
            setEdgeProgress(0.0);
            currentEdgeIndex++;
            setProgress((double) currentEdgeIndex / path.getEdges().size());
        }
        return consumedTime;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), path, currentEdgeIndex);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!super.equals(obj)) return false;
        if (getClass() != obj.getClass()) return false;
        FollowPathAction other = (FollowPathAction) obj;
        return currentEdgeIndex == other.currentEdgeIndex && Objects.equals(path, other.path);
    }

    @Override
    public String toString() {
        return super.toString().replace("}", "") + ", path=" + (path == null ? "null" : path.toString()) + 
                ", index=" + currentEdgeIndex + '}';
    }

}
