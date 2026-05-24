package fr.cy.model.agent.agentActions;

import java.util.Objects;

import fr.cy.model.graph.element.Edge;
import fr.cy.model.pathfinding.GraphPath;

public abstract class AbstractFollowPathAction implements AgentAction {
    private GraphPath path = null;
    private boolean isCompleted = false;
    private int currentEdgeIndex = 0;

    public AbstractFollowPathAction(GraphPath path) {
        this.path = Objects.requireNonNull(path, "Path cannot be null");
    }

    public Edge getCurrentEdge() {
        if (path == null || currentEdgeIndex >= path.getEdges().size()) {
            return null; // No more edges to follow
        }
        return path.getEdgeAt(currentEdgeIndex);
    }

    public Edge moveToNextEdge() {
        if (path == null || currentEdgeIndex >= path.getEdges().size()) {
            return null; // No more edges to follow
        }
        Edge nextEdge = path.getEdgeAt(currentEdgeIndex);
        currentEdgeIndex++;
        return nextEdge;
    }

    @Override
    public boolean isCompleted() {
        return path != null && currentEdgeIndex >= path.getEdges().size();
    }
}
