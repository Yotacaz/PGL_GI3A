package fr.cy.model.graph.element;

import fr.cy.model.graph.GraphConfig;
import fr.cy.model.agent.Agent;
import fr.cy.model.agent.behaviour.properties.AgentDecisionalProperties;
import fr.cy.model.fire.Fire;
import java.util.*;

/**
 * Represents an edge connecting two nodes in the graph.
 * <p>
 * An edge has a start and end node, a width and a length (used to calculate
 * capacity).
 * It can be directed or undirected and inherits common properties from
 * {@link GraphElement} (identifier, fire state, congestion).
 * </p>
 *
 * @author GI3A
 * @version 1.0
 */
public class Edge extends GraphElement {

    /** Nodes defining the edge endpoints. */
    private Node start;
    private Node end;

    /** Indicates if the edge is directed. */
    private boolean directed;

    /** Dimensions of the edge (must be non-negative). */
    private double width;
    private double length;

    /** Fire propagation status. */
    private boolean burningFromStart = false;
    private boolean burningFromEnd = false;
    private boolean initialBurningFromStart = false;
    private boolean initialBurningFromEnd = false;

    /**
     * Constructs an edge using default values from {@link GraphConfig}.
     *
     * @param id    Unique identifier.
     * @param start Starting node.
     * @param end   Ending node.
     */
    public Edge(int id, Node start, Node end) {
        this(id, start, end, GraphConfig.DEFAULT_EDGE_DIRECTED, GraphConfig.DEFAULT_EDGE_WIDTH,
                GraphConfig.DEFAULT_EDGE_LENGTH);
    }

    /**
     * Constructs an edge with explicit dimensions and directionality.
     *
     * @param id       Unique identifier.
     * @param start    Starting node.
     * @param end      Ending node.
     * @param directed Whether the edge is directed.
     * @param width    Width of the edge (non-negative).
     * @param length   Length of the edge (non-negative).
     */
    public Edge(int id, Node start, Node end, boolean directed, double width, double length) {
        super(id, width * length);
        this.start = start;
        this.end = end;
        this.directed = directed;
        setLength(length);
        setWidth(width);
    }

    /** @return The starting node. */
    public Node getStart() {
        return start;
    }

    /** @return The ending node. */
    public Node getEnd() {
        return end;
    }

    /**
     * Retrieves the node opposite to the one provided.
     * 
     * @param node One of the nodes connected to this edge.
     * @return The opposite node, or null if the provided node is not connected.
     */
    public Node getOppositeNode(Node node) {
        if (node.equals(start))
            return end;
        if (node.equals(end))
            return start;
        return null;
    }

    /** @return True if the edge is directed. */
    public boolean isDirected() {
        return directed;
    }

    /** Swaps the start and end nodes. */
    public void switchDirection() {
        start.removeEdge(this);
        end.removeEdge(this);   

        Node temp = start;
        start = end;
        end = temp;

        start.addEdge(this);
        end.addEdge(this);

        
    }

    /** @param directed True to make the edge directed. */
    public void setDirected(boolean directed) {
        this.directed = directed;
    }

    /** @return The length of the edge. */
    public double getLength() {
        return length;
    }

    /** @return The width of the edge. */
    public double getWidth() {
        return width;
    }

    /** @param length New length (clamped to non-negative). */
    public void setLength(double length) {
        this.length = Math.max(0, length);
    }

    /** @param width New width (clamped to non-negative). */
    public void setWidth(double width) {
        this.width = Math.max(0, width);
    }

    /** @return Capacity calculated as width * length. */
    @Override
    public double getCapacity() {
        return width * length;
    }

    /**
     * Counts agents currently on this edge that entered from the start node.
     * 
     * @return Number of agents moving from start to end.
     */
    public int countAgentsGoingFromStartToEnd() {
        int nb = 0;
        for (Agent agent : getAgents()) {
            Node prev = agent.getPreviousOrCurrentNode();
            if (prev != null && prev.equals(start))
                nb++;
        }
        return nb;
    }

    /**
     * Counts agents currently on this edge that entered from the end node.
     * 
     * @return Number of agents moving from end to start.
     */
    public int countAgentsGoingFromEndToStart() {
        int nb = 0;
        for (Agent agent : getAgents()) {
            Node prev = agent.getPreviousOrCurrentNode();
            if (prev != null && prev.equals(end))
                nb++;
        }
        return nb;
    }

    /**
     * Calculates maximum allowed speed for an agent entering from a specific node,
     * applying counter-flow penalties if the edge is undirected.
     * * @param fromNode The node the agent is entering from.
     * 
     * @return Calculated speed.
     * @throws IllegalArgumentException if the node is not connected to this edge.
     */
    public double getMaxAgentSpeedInDirection(Node fromNode) {
        double speed = getMaxAgentSpeed();
        if (!directed) {
            int same, opposite;
            if (fromNode.equals(start)) {
                same = countAgentsGoingFromStartToEnd();
                opposite = countAgentsGoingFromEndToStart();
            } else if (fromNode.equals(end)) {
                same = countAgentsGoingFromEndToStart();
                opposite = countAgentsGoingFromStartToEnd();
            } else {
                throw new IllegalArgumentException("Node not connected to edge");
            }
            int total = same + opposite;
            if (total == 0)
                return speed;
            double oppositeRatio = opposite / (double) total;
            return speed * (1.0 - 0.5 * oppositeRatio);
        }
        return speed;
    }

    /**
     * Evaluates the attractiveness of this edge for an agent going to a specific
     * node.
     * 
     * @param agentState      Agent decision properties.
     * @param destinationNode The target node.
     * @return Multiplier score.
     */
    public double getScoreMultiplierForAgentGoingToNode(AgentDecisionalProperties agentState, Node destinationNode) {
        return getScoreMultiplierForAgent(agentState) * destinationNode.getScoreMultiplierForAgent(agentState);
    }

    @Override
    public List<GraphElement> getNeighbors() {
        return List.of(start, end);
    }

    @Override
    public String toString() {
        return String.format(
                "Edge{id=%d, startNode=%d, endNode=%d, length=%.2f, width=%.2f, speed=%.2f, directed=%b, fire=%b, cong=%.2f}",
                getId(), start.getId(), end.getId(), length, width, getMaxAgentSpeed(), directed, isOnFire(),
                getCongestion());
    }

    public boolean isBurningFromStart() {
        return burningFromStart;
    }

    public boolean isBurningFromEnd() {
        return burningFromEnd;
    }

    /**
     * Ignites the edge from a source node.
     * 
     * @param source  Node where fire originates.
     * @param newFire Fire properties.
     */
    public void igniteFrom(Node source, Fire newFire) {
        if (!isOnFire())
            setFire(newFire);
        if (source.equals(start))
            burningFromStart = true;
        else if (source.equals(end))
            burningFromEnd = true;
    }

    /** @return The distance burned along the edge length. */
    public double getBurnedDistance() {
        return !isOnFire() ? 0.0 : getFire().getBurningTime() * getFire().getSpreadRate();
    }

    /** @return True if the entire edge is consumed by fire. */
    public boolean isFullyBurned() {
        if (!isOnFire())
            return false;
        double distance = getBurnedDistance();
        return (burningFromEnd && burningFromStart) ? (distance * 2) >= length : distance >= length;
    }

    public void setStart(Node node) {
        start = node;
    }

    public void setEnd(Node node) {
        end = node;
    }

    @Override
    public double getDamageForAgent(Agent agent, double tickDuration) {
        // Implementation logic for damage calculation
        return super.getDamageForAgent(agent, tickDuration);
    }

    @Override
    public void removeFire() {
        super.removeFire();
        this.burningFromStart = false;
        this.burningFromEnd = false;
    }

    /** @return Percentage of edge burned (0.0 to 1.0). */
    public double getBurnPercentage() {
        return !isOnFire() ? 0.0 : Math.min(1.0, getBurnedDistance() / getLength());
    }

    @Override
    public void setInitialState() {
        super.setInitialState();
        this.initialBurningFromStart = this.burningFromStart;
        this.initialBurningFromEnd = this.burningFromEnd;
    }

    @Override
    public void reset() {
        super.reset();
        this.burningFromStart = this.initialBurningFromStart;
        this.burningFromEnd = this.initialBurningFromEnd;
    }
}