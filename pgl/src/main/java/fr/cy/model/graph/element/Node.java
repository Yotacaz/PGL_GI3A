package fr.cy.model.graph.element;

import java.util.ArrayList;
import java.util.List;

import fr.cy.model.agent.behaviour.properties.AgentDecisionalProperties;

/**
 * Class representing a node in the graph.
 * 
 * A node is an element in the graph located at a specific position (x, y)
 * in a plane. It inherits common properties
 * from GraphElement such as the identifier, fire state, and congestion.
 * 
 * @author GI3A
 * @version 1.0
 */
public class Node extends GraphElement {
    private final int id;

    /** Coordinates X, Y */
    private double x;
    private double y;

    private boolean isExit;

    /** List of edges connected to this node */
    private final List<Edge> connectedEdges;
    /** List of outgoing edges */
     
    private final List<Edge> outgoingEdges;

    /**
     * Constructor for the Node class.
     * 
     * @param id unique identifier for the node
     * @param x  the X coordinate of the node
     * @param y  the Y coordinate of the node
     */
    public Node(int id, double x, double y, double capacity) {
        super(id, capacity);
        this.id = id;
        this.x = x;
        this.y = y;

        connectedEdges = new ArrayList<>();
        outgoingEdges = new ArrayList<>();
    }

    @Override
    public double getScoreMultiplierForAgent(AgentDecisionalProperties agentState) {
        double scoreMultiplier = super.getScoreMultiplierForAgent(agentState);
        if (isExit()) {
            scoreMultiplier *= 10; // prefer exits
        }
        return scoreMultiplier;
    }

    /**
     * Returns a textual representation of the node.
     * 
     * @return a string containing the node's id and coordinates
     */
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

    /**
     * Returns the unique identifier of the node.
     * 
     * @return the node's id
     */
    public int getId() {
        return id;
    }

    /**
     * Returns the X coordinate of the node.
     * 
     * @return the X coordinate
     */
    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    /**
     * Returns the Y coordinate of the node.
     * 
     * @return the Y coordinate
     */
    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public void setCapacity(double capacity) {
        super.setCapacity(capacity);
    }

    /**
     * Checks if the node is an exit.
     * 
     * @return true if the node is an exit, false otherwise
     */
    public boolean isExit() {
        return isExit;
    }

    /**
     * Sets whether the node is an exit.
     * 
     * @param exit true if the node is an exit, false otherwise
     */
    public void setExit(boolean exit) {
        isExit = exit;
    }

    /**
     * Toggles the exit status of the node.
     */
    public void switchExit() {
        isExit = !isExit;
    }

    @Override
    public List<GraphElement> getNeighbors() {
        return new ArrayList<>(connectedEdges);
    }

    public Edge getEdgeTo(Node neighbor) {
        for (Edge edge : connectedEdges) {
            if (edge.getOppositeNode(this).equals(neighbor)) {
                return edge;
            }
        }
        return null;
    }

    /**
     * Adds an edge connected to this node.
     * 
     * @param edge the edge to add
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
     * Removes an edge connected to this node.
     * 
     * @param edge the edge to remove
     */
    public void removeEdge(Edge edge) {
        if (edge != null) {
            connectedEdges.remove(edge);
            outgoingEdges.remove(edge);
        }
    }

    /**
     * Return all outgoing edges from this node. For undirected edges, they are
     * considered outgoing from both nodes.
     * 
     * @return list of outgoing edges
     */
    public List<Edge> getOutgoingEdges() {
        return outgoingEdges;
    }

    /**
     * Returns all edges connected to this node.
     * 
     * @return list of edges
     */
    public List<Edge> getEdges() {
        return new ArrayList<>(connectedEdges);
    }
}