package fr.cy.model.agent.behaviour.agentActions;

import java.util.Objects;

import fr.cy.model.agent.Agent;
import fr.cy.model.graph.element.Edge;
import fr.cy.model.pathfinding.GraphPath;

public abstract class AbstractFollowPathAction extends AgentAction {
    private GraphPath path = null;
    private int currentEdgeIndex = 0;

    public AbstractFollowPathAction(Agent agent, GraphPath path) {
        super(agent);
        this.path = Objects.requireNonNull(path, "Path cannot be null");
    }
    @Override
    public Edge getCurrentEdgeOrNextEdgeIfOnNode() {
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
