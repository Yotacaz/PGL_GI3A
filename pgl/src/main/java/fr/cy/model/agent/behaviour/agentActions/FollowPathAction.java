package fr.cy.model.agent.behaviour.agentActions;

import java.util.Objects;

import fr.cy.model.agent.Agent;
import fr.cy.model.agent.AgentSettings;
import fr.cy.model.graph.element.Edge;
import fr.cy.model.graph.element.Node;
import fr.cy.model.pathfinding.GraphPath;

/**
 * Represents an action where an agent follows a predefined path.
 */
public class FollowPathAction extends AbstractMoveAction {
    /** The path that the agent will follow */
    private GraphPath path = null;
    /** The index of the current edge in the path */
    private int currentEdgeIndex = 0;

    /**
     * Creates a new FollowPathAction for the specified agent and path.
     * @param agent the agent that will perform this action
     * @param path the path to follow
     */
    public FollowPathAction(Agent agent, GraphPath path) {
        super(agent);
        this.path = Objects.requireNonNull(path, "Path cannot be null");
    }

    @Override
    public Edge getClosestTargetEdge() {
        if (path == null || currentEdgeIndex >= path.getEdges().size()) {
            return null; // No more edges to follow
        }
        return path.getEdgeAt(currentEdgeIndex);
    }

    @Override
    public Node getClosestTargetNode() {
        int i = currentEdgeIndex + (getAgent().isOnEdge() ? 1:0);
        if (path == null || i >= path.getNodes().size()) {
            return null; // No more nodes to follow
        }
        return path.getNodeAt(i);
    }

    @Override
    public boolean isCompleted() {
        return path != null && currentEdgeIndex >= path.getEdges().size();
    }

    @Override
    public double perform(AgentSettings agentSettings, double availableTime) {
        Edge currentEdge = getClosestTargetEdge();
        if (currentEdge == null) {
            setProgress(1.0); //no more edge
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

    /**
     * Returns the index of the current edge in the path.
     * @return the index of the current edge being followed
     */
    public int getCurrentEdgeIndex() {
        return currentEdgeIndex;
    }

    /**
     * Returns the index of the next node in the path.
     * @return the index of the next node to be reached
     */
    public int getNextNodeIndex() {
        return currentEdgeIndex + 1; //there is always 1 more node than there is edges and the first node is the starting point 
    }

    /**
     * Returns the path that the agent is following.
     * @return the {@code GraphPath} being followed by the agent
     */
    public GraphPath getPath() {
        return path;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), path, currentEdgeIndex);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        FollowPathAction other = (FollowPathAction) obj;
        return currentEdgeIndex == other.currentEdgeIndex && Objects.equals(path, other.path);
    }

    @Override
    public String toString() {
        return super.toString().replace("}", "") + ", path=" + (path == null ? "null" : path.toString()) +
                ", index=" + currentEdgeIndex + '}';
    }

}
