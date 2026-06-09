package fr.cy.model.graph.element;

import fr.cy.model.graph.GraphConfig;
import fr.cy.model.agent.Agent;
import fr.cy.model.agent.behaviour.properties.AgentDecisionalProperties;
import fr.cy.model.fire.Fire;
import java.util.*;

/**
 * Represent an edge connecting two nodes in the graph. An edge has a start and end node, a width and
 *
 * An edge has a start and end node, a width and
 * a length, used to calculate the capacity. It can be
 * directed or not and inherits the common properties of
 * {@link GraphElement} (identifier, fire state, congestion).
 *
 * @author GI3A
 * @version 1.0
 */
public class Edge extends GraphElement {

    /** Node of departure/arrival of the edge */
    private Node start;
    private Node end;

    private int agentGoingToStartNode;
    private int agentGoingToEndNode;

    /** Indicates if the edge is directed */
    private boolean directed;

    /** Dimensions of the edge (>= 0) */
    private double width;
    private double length;
    /** Fire direction */
    private boolean burningFromStart = false;
    private boolean burningFromEnd = false;

    private boolean initialBurningFromStart = false;
    private boolean initialBurningFromEnd = false;

    /**
     * Simplified constructor using default values from
     * {@link GraphConfig} for the width and length.
     *
     * @param id       unique identifier of the edge
     * @param start    start node
     * @param end      end node
     * @param directed true if the edge is directed
     */
    public Edge(int id, Node start, Node end) {
        this(id, start, end, GraphConfig.DEFAULT_EDGE_DIRECTED, GraphConfig.DEFAULT_EDGE_WIDTH,
                GraphConfig.DEFAULT_EDGE_LENGTH);
    }

    /**
     * Complete constructor for an edge.
     *
     * @param id       unique identifier of the edge
     * @param start    start node
     * @param end      end node
     * @param directed true if the edge is directed
     * @param width    width of the edge (non-negative value)
     * @param length   length of the edge (non-negative value)
     */
    public Edge(int id, Node start, Node end, boolean directed, double width, double length) {

        super(id, width * length);

        this.start = start;
        this.end = end;

        this.directed = directed;

        setLength(length);
        setWidth(width);
    }

    /**
     * @return the start node
     */
    public Node getStart() {
        return start;
    }

    /**
     * @return the end node
     */
    public Node getEnd() {
        return end;
    }

    public Node getOppositeNode(Node node) {
        if (node.equals(start)) {
            return end;
        }
        if (node.equals(end)) {
            return start;
        }

        return null;
    }

    /**
     * @return {@code true} if the edge is directed
     */
    public boolean isDirected() {
        return directed;
    }

    public void switchDirection() {
        Node temp = start;
        start = end;
        end = temp;
    }

    /**
     * Sets whether the edge is directed.
     *
     * @param directed true for a directed edge
     */
    public void setDirected(boolean directed) {
        this.directed = directed;
    }

    /**
     * @return the length of the edge
     */
    public double getLength() {
        return length;
    }

    /**
     * @return the width of the edge
     */
    public double getWidth() {
        return width;
    }

    /**
     * Sets the length of the edge, ensuring it is non-negative.
     *
     * @param length the new length
     */
    public void setLength(double length) {
        this.length = Math.max(0, length);
    }

    /**
     * Sets the width of the edge, ensuring it is non-negative.
     *
     * @param width the new width
     */
    public void setWidth(double width) {
        this.width = Math.max(0, width);
    }

    /**
     * Calculates the total capacity of the edge by multiplying width and length.
     *
     * @return the capacity (width * length)
     */
    @Override
    public double getCapacity() {
        return width * length;
    }

    /**
     * Count agents currently on this edge that entered from the start node.
     *
     * <p>
     * This method inspects each {@link fr.cy.model.agent.Agent} present on the
     * edge and counts those whose last known node equals the edge's
     * {@code start} node. It is intended to provide a lightweight direction
     * estimate (number of agents moving from start → end) without storing
     * agent lists inside the edge.
     * </p>
     *
     * @return number of agents that most recently came from the start node
     */
    public int countAgentsGoingFromStartToEnd() {
        int nb = 0;
        for (Agent agent : getAgents()) {
            Node prev = agent.getPreviousOrCurrentNode();
            if (prev != null && prev.equals(start)) {
                nb++;
            }
        }
        return nb;
    }

    /**
     * Count agents currently on this edge that entered from the end node.
     *
     * <p>
     * Symmetric to {@link #countAgentsGoingFromStartToEnd()}: inspects
     * agents on the edge and returns how many have their previous/current
     * node equal to the edge's {@code end} node (estimate for end → start flow).
     * </p>
     *
     * @return number of agents that most recently came from the end node
     */
    public int countAgentsGoingFromEndToStart() {
        int nb = 0;
        for (Agent agent : getAgents()) {
            Node prev = agent.getPreviousOrCurrentNode();
            if (prev != null && prev.equals(end)) {
                nb++;
            }
        }
        return nb;
    }


    /**
     * Compute the maximum allowed agent speed when entering this edge from
     * the given node.
     *
     * <p>
     * The base speed is computed by {@link #getMaxAgentSpeed()} which already
     * accounts for fires and congestion. For directed edges this base speed is
     * returned unchanged. For non-directed edges a simple counter-flow penalty is
     * applied: the method estimates how many agents are moving in the opposite
     * direction (using {@link #countAgentsGoingFromStartToEnd()} and
     * {@link #countAgentsGoingFromEndToStart()}) and reduces the speed by a
     * factor proportional to the ratio of opposite-flow agents.
     * </p>
     *
     * <p>
     * Current penalty formula: speed * (1 - 0.5 * oppositeRatio), where
     * {@code oppositeRatio} = opposite / (same + opposite). The coefficient
     * {@code 0.5} is a tunable penalty constant.
     * </p>
     *
     * @param fromNode node from which the agent enters this edge (must be
     *                 either the edge's {@code start} or {@code end})
     * @return maximum agent speed allowed when entering from {@code fromNode}
     * @throws IllegalArgumentException if {@code fromNode} is not connected to
     *                                  this edge
     */
    public double getMaxAgentSpeedInDirection(Node fromNode) {

        double speed = getMaxAgentSpeed();

        if (!directed) { // only apply counter-flow penalty on non-directed edges
            int sameDirection;
            int oppositeDirection;

            if (fromNode.equals(start)) {
                sameDirection = countAgentsGoingFromStartToEnd();
                oppositeDirection = countAgentsGoingFromEndToStart();
            } else if (fromNode.equals(end)) {
                sameDirection = countAgentsGoingFromEndToStart();
                oppositeDirection = countAgentsGoingFromStartToEnd();
            } else {
                throw new IllegalArgumentException("Le nœud n'appartient pas à l'arête");
            }

            int total = sameDirection + oppositeDirection;
            if (total == 0) {
                return speed;
            }

            double oppositeRatio = oppositeDirection / (double) total;

            double counterFlowFactor = 1.0 - 0.5 * oppositeRatio; // 0.5 = penalty coefficient

            return speed * counterFlowFactor;
        }

        return speed;
    }

    /**
     * Evaluate a score multiplier for an agent on this edge, based on its
     * properties and the agent's properties.
     * 
     * @param agentState      the properties of the agent for which we want to
     *                        evaluate the score multiplier
     * @param destinationNode the node the agent is trying to reach by going through
     *                        this edge (used to evaluate the score multiplier of
     *                        that node as well)
     * @return a score multiplier for an agent on this edge, based on its properties
     *         and the agent's properties,
     */
    public double getScoreMultiplierForAgentGoingToNode(AgentDecisionalProperties agentState, Node destinationNode) {
        double scoreMult = getScoreMultiplierForAgent(agentState);
        scoreMult *= destinationNode.getScoreMultiplierForAgent(agentState);
        return scoreMult;
    }

    @Override
    public List<GraphElement> getNeighbors() {
        List<GraphElement> neighbors = new ArrayList<>();

        neighbors.add(start);
        neighbors.add(end);

        return neighbors;
    }

    /**
     * Textual representation of the edge, including its id, connected nodes, dimensions, max agent speed,
     *
     * @return a string describing the edge
     */
    @Override
    public String toString() {

        return "Edge{" +
                "id=" + getId() +
                ", startNode=" + start.getId() +
                ", endNode=" + end.getId() +
                ", length=" + length +
                ", width=" + width +
                ", maxAgentSpeed=" + String.format("%.2f", getMaxAgentSpeed()) +
                ", directed=" + directed +
                ", onFire=" + isOnFire() +
                ", congestion=" + String.format("%.2f", getCongestion()) +
                '}';
    }

    public boolean isBurningFromStart() {
        return burningFromStart;
    }

    public boolean isBurningFromEnd() {
        return burningFromEnd;
    }

    /**
     * Lights the edge on fire from a given source node, using the provided fire properties.
     */
    public void igniteFrom(Node source, Fire newFire) {
        if (!isOnFire()) {
            setFire(newFire);
        }
        // Save from which side the fire is coming to determine the burning direction and how it spreads on the edge
        if (source.equals(start)) {
            burningFromStart = true;
        } else if (source.equals(end)) {
            burningFromEnd = true;
        }
    }

    public double getBurnedDistance() {
        if (!isOnFire()) {
            return 0.0;
        }

        return getFire().getBurningTime() * getFire().getSpreadRate();
    }

    public boolean isFullyBurned() {
        if (!isOnFire()) {
            return false;
        }

        double distance = getBurnedDistance();

        /** Case where flames come from both nodes */
        if (burningFromEnd && burningFromStart) {
            return (distance * 2) >= length;
        }
        return distance >= length;
    }

    public void setStart(Node node) {
        start = node;
    }
    public void setEnd(Node node) {
        end = node;
    }
        

    @Override
    public void removeFire() {
        super.removeFire();
        this.burningFromStart = false;
        this.burningFromEnd = false;
    }

    /**
     * Calculates the percentage of the edge covered by flames (from 0.0 to 1.0).
     * 
     * @return Percentage
     */
    public double getBurnPercentage() {
        if (!isOnFire()) {
            return 0.0;
        }

        // Distance = Time (ticks) * Spread Rate
        double burnedDistance = getFire().getBurningTime() * getFire().getSpreadRate();

        return Math.min(1.0, burnedDistance / getLength());
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

        // 2. Reset burning direction to initial state
        this.burningFromStart = this.initialBurningFromStart;
        this.burningFromEnd = this.initialBurningFromEnd;
    }
}
