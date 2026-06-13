package fr.cy.model.graph.element;

import java.util.ArrayList;
import java.util.List;
import fr.cy.model.agent.properties.AgentDecisionalProperties;

/**
 * Represents a node in the graph structure.
 * <p>
 * A node is located at a specific (x, y) coordinate in a 2D plane.
 * It inherits common properties from {@link GraphElement} such as unique
 * identification, fire state, and congestion tracking.
 * </p>
 * * @author GI3A
 * 
 * @version 1.0
 */
public class Node extends GraphElement {

    private final int id;
    private double x;
    private double y;
    private boolean isExit;

    /** List of all edges incident to this node. */
    private final List<Edge> connectedEdges;

    /** List of edges that agents can travel through from this node. */
    private final List<Edge> outgoingEdges;

    /**
     * Constructs a new Node.
     * * @param id Unique identifier.
     * 
     * @param x        X-coordinate.
     * @param y        Y-coordinate.
     * @param capacity Maximum capacity for agents.
     */
    public Node(int id, double x, double y, double capacity) {
        super(id, capacity);
        this.id = id;
        this.x = x;
        this.y = y;
        this.connectedEdges = new ArrayList<>();
        this.outgoingEdges = new ArrayList<>();
    }

    /**
     * Calculates an attractiveness multiplier for an agent based on local node
     * properties, heavily prioritizing exit nodes.
     * * @param agentState Agent decision properties.
     * 
     * @return Score multiplier.
     */
    @Override
    public double getScoreMultiplierForAgent(AgentDecisionalProperties agentState) {
        double scoreMultiplier = super.getScoreMultiplierForAgent(agentState);
        if (isExit()) {
            scoreMultiplier *= 10; // Significant preference for exits
        }
        return scoreMultiplier;
    }

    @Override
    public String toString() {
        return "Node{" +
                "id=" + getId() +
                ", x=" + x +
                ", y=" + y +
                ", exit=" + isExit +
                ", onFire=" + isOnFire() +
                "}";
    }

    /** @return The unique identifier of the node. */
    @Override
    public int getId() {
        return id;
    }

    /** @return The X coordinate. */
    public double getX() {
        return x;
    }

    /** @param x The X coordinate to set. */
    public void setX(double x) {
        this.x = x;
    }

    /** @return The Y coordinate. */
    public double getY() {
        return y;
    }

    /** @param y The Y coordinate to set. */
    public void setY(double y) {
        this.y = y;
    }

    /** @param capacity The capacity to set. */
    @Override
    public void setCapacity(double capacity) {
        super.setCapacity(capacity);
    }

    /** @return True if this node acts as an evacuation exit. */
    public boolean isExit() {
        return isExit;
    }

    /** @param exit Sets the exit status of the node. */
    public void setExit(boolean exit) {
        isExit = exit;
    }

    /** Toggles the exit status. */
    public void switchExit() {
        isExit = !isExit;
    }

    /**
     * Retrieves neighbors based on connected edges.
     * 
     * @return A list of {@link GraphElement}s connected to this node.
     */
    @Override
    public List<GraphElement> getNeighbors() {
        return new ArrayList<>(connectedEdges);
    }

    /**
     * Finds the edge connecting this node to a specific neighbor.
     * 
     * @param neighbor The neighbor node.
     * @return The connecting {@link Edge}, or null if none exists.
     */
    public Edge getEdgeTo(Node neighbor) {
        for (Edge edge : connectedEdges) {
            if (edge.getOppositeNode(this).equals(neighbor)) {
                return edge;
            }
        }
        return null;
    }

    /**
     * Adds an edge to this node's connected list.
     * 
     * @param edge The edge to add.
     */
    public void addEdge(Edge edge) {
        if (edge != null) {
            connectedEdges.add(edge);
            if (!edge.isDirected() || edge.getStart().equals(this)) {
                outgoingEdges.add(edge);
            }
        }
    }

    /**
     * Removes an edge from this node's lists.
     * 
     * @param edge The edge to remove.
     */
    public void removeEdge(Edge edge) {
        if (edge != null) {
            connectedEdges.remove(edge);
            outgoingEdges.remove(edge);
        }
    }

    /**
     * @return List of edges agents can travel through from this node.
     */
    public List<Edge> getOutgoingEdges() {
        return outgoingEdges;
    }

    /**
     * @return a copy of all edges incident to this node.
     */
    public List<Edge> getEdges() {
        return new ArrayList<>(connectedEdges);
    }
}