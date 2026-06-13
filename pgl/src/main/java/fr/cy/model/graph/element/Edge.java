package fr.cy.model.graph.element;

import fr.cy.model.graph.GraphConfig;
import fr.cy.model.agent.Agent;
import fr.cy.model.agent.AgentSettings;
import fr.cy.model.agent.exceptions.AgentStateException;
import fr.cy.model.agent.properties.AgentDecisionalProperties;
import fr.cy.model.agent.properties.AgentPhysicalProperties;
import fr.cy.model.fire.Fire;
import java.util.List;
import java.util.Objects;

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
public final class Edge extends GraphElement {

    /** Nodes defining the edge endpoints. */
    private Node start;
    private Node end;

    /** Indicates if the edge is directed. */
    private boolean directed;

    /** Dimensions of the edge (must be non-negative). */
    private double width;
    private double length;

    /**
     * Local discretization of the edge into segments (buckets) used by the local
     * congestion model. Rebuilt every simulation tick from the agents currently
     * on the edge.
     */
    private EdgeSegment[] segments;

    /** Number of agents moving in the forward direction (from start to end). */
    private int agentCountForward = 0;
    /** Number of agents moving in the backward direction (from end to start). */
    private int agentCountBackward = 0;
    /** Total surface occupied by agents moving in the forward direction. */
    private double totalOccupiedSurfaceForward = 0.0;
    /** Total surface occupied by agents moving in the backward direction. */
    private double totalOccupiedSurfaceBackward = 0.0;

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

    /**
     * Returns the starting node of this edge.
     *
     * @return The starting node.
     */
    public Node getStart() {
        return start;
    }

    /**
     * Returns the ending node of this edge.
     *
     * @return The ending node.
     */
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

    /**
     * Returns whether this edge is directed (one-way).
     *
     * @return True if the edge is directed.
     */
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

    /**
     * Swaps start and end without touching node or graph adjacency structures.
     * Callers must update those structures before calling this method.
     */
    public void reverseDirection() {
        Node temp = start;
        start = end;
        end = temp;
    }

    /**
     * Handles cleanup when the edge is removed from the graph.
     * Updates agents on connected nodes to prevent dangling references.
     * Also removes this edge from the connected nodes' edge lists.
     */
    public void onRemove() {
        // move agents that are on the edge

        // reset previous edge reference for agents on connected nodes
        Objects.requireNonNull(end,
                " Edge end node cannot be null when removing edge, please call edge.onRemove() before node.onRemove()");
        for (Agent agent : end.getAgents()) {
            if (agent.getPreviousOrCurrentEdge() != null && agent.getPreviousOrCurrentEdge().equals(this)) {
                agent.onPreviousEdgeDeleted();
            }
        }
        end.removeEdge(this);

        Objects.requireNonNull(start,
                " Edge start node cannot be null when removing edge, please call edge.onRemove() before node.onRemove()");
        for (Agent agent : start.getAgents()) {
            if (agent.getPreviousOrCurrentEdge() != null && agent.getPreviousOrCurrentEdge().equals(this)) {
                agent.onPreviousEdgeDeleted();
            }
        }
        start.removeEdge(this);

    }

    /**
     * Sets whether this edge is directed (one-way).
     *
     * @param directed True to make the edge directed.
     */
    public void setDirected(boolean directed) {
        this.directed = directed;
    }

    /**
     * Returns the length of the edge in meters.
     *
     * @return The length of the edge.
     */
    public double getLength() {
        return length;
    }

    /**
     * Returns the width of the edge in meters.
     *
     * @return The width of the edge.
     */
    public double getWidth() {
        return width;
    }

    /**
     * Sets the length of this edge.
     *
     * @param length New length (clamped to non-negative).
     */
    public void setLength(double length) {
        this.length = Math.max(0, length);
    }

    /**
     * Sets the width of this edge.
     *
     * @param width New width (clamped to non-negative).
     */
    public void setWidth(double width) {
        this.width = Math.max(0, width);
    }

    /** @return Capacity calculated as width * length. */
    @Override
    public double getCapacity() {
        return width * length;
    }

    /**
     * Evaluates the attractiveness of this edge for an agent going to a specific
     * node.
     * 
     * @param agentState              Agent decision properties.
     * @param agentPhysicalProperties Agent physical properties.
     * @param destinationNode         The target node.
     * @return Multiplier score.
     */
    public double getScoreMultiplierForAgentGoingToNode(AgentDecisionalProperties agentState,
            AgentPhysicalProperties agentPhysicalProperties, Node destinationNode) {
        return getScoreMultiplierForAgent(agentState, agentPhysicalProperties)
                * destinationNode.getScoreMultiplierForAgent(agentState, agentPhysicalProperties);
    }

    // =============
    // LOCAL CONGESTION MODEL (segment discretization)
    // =============

    /**
     * Rebuilds all segment occupancy data based on the current agent positions on this edge.
     */
    public void updateSegments() {
        // update segment occupancy for congestion model
        clearSegments();
        for (Agent agent : getAgents()) {
            assignAgentToSegment(agent);
        }
    }

    /**
     * Returns the number of discretization segments this edge is split into.
     * <p>
     * The count is derived from the edge length so that each segment is roughly
     * {@link GraphConfig#SEGMENT_TARGET_LENGTH} meters long, clamped between
     * {@link GraphConfig#MIN_SEGMENTS_PER_EDGE} and
     * {@link GraphConfig#MAX_SEGMENTS_PER_EDGE}.
     * </p>
     *
     * @return the number of segments (always > 1)
     */
    public int getSegmentCount() {
        int derived = (int) Math.ceil(length / GraphConfig.SEGMENT_TARGET_LENGTH);
        derived = Math.max(GraphConfig.MIN_SEGMENTS_PER_EDGE, derived);
        derived = Math.min(GraphConfig.MAX_SEGMENTS_PER_EDGE, derived);
        return derived;
    }

    /**
     * Lazily (re)allocates the segment array so that it matches the current
     * {@link #getSegmentCount()}. Existing segments are reused when the count is
     * unchanged.
     */
    private void ensureSegments() {
        int n = getSegmentCount();
        if (segments == null || segments.length != n) {
            segments = new EdgeSegment[n];
            for (int i = 0; i < n; i++) {
                segments[i] = new EdgeSegment();
            }
        }
    }

    /**
     * Empties every segment while keeping the array allocated. Used at the
     * beginning of each tick
     */
    public void clearSegments() {
        ensureSegments();
        this.agentCountForward = 0;
        this.agentCountBackward = 0;
        this.totalOccupiedSurfaceForward = 0.0;
        this.totalOccupiedSurfaceBackward = 0.0;
        for (EdgeSegment segment : segments) {
            segment.clear();
        }
    }

    /**
     * Determines whether an agent travels in the forward direction (from the edge
     * {@code start} node towards the {@code end} node).
     *
     * @param agent the agent currently on this edge
     * @return {@code true} if the agent moves from start to end, {@code false}
     *         otherwise
     */
    private boolean isAgentOnEdgeMovingForward(Agent agent) {
        if (!this.equals(agent.getPreviousOrCurrentEdge())) {
            throw new AgentStateException("Agent is not on this edge");
        }
        Node target = Objects.requireNonNull(agent.getCurrentNodeOrNextNodeIfOnEdge());
        return target.equals(end);
    }

    /**
     * Converts an agent state into its normalized position measured from the edge
     * {@code start} node, in {@code [0, 1]}.
     * <p>
     * {@link Agent#getCurrentEdgeProgress()} is always measured from the node the
     * agent entered from, so the value is mirrored for backward agents.
     * </p>
     *
     * @param agent   the agent currently on this edge
     * @param forward whether the agent moves forward (start to end)
     * @return the position from the start node, in {@code [0, 1]}
     */
    private double progressFromStart(Agent agent, boolean forward) {
        double progress = agent.getCurrentEdgeProgress();
        if (progress < 0.0) {
            progress = 0.0;
        }
        return forward ? progress : 1.0 - progress;
    }

    /**
     * Maps a position measured from the start node to a segment index.
     *
     * @param progressFromStart the normalized position from the start node
     *                          ({@code [0, 1]})
     * @return the index of the segment containing that position
     */
    public int segmentIndexForProgressFromStart(double progressFromStart) {
        if (progressFromStart < 0.0 || progressFromStart > 1.0) {
            throw new IllegalArgumentException("Progress must be in [0, 1]");
        }

        int n = getSegmentCount();
        return Math.min((int) (progressFromStart * n), n - 1);
    }

    /**
     * Inserts an agent into the segment matching its current position, in the list
     * matching its travel direction. The per-direction occupied-surface sums are
     * updated incrementally.
     *
     * @param agent the agent to assign (must be on this edge)
     */
    public void assignAgentToSegment(Agent agent) {
        ensureSegments();
        boolean forward = isAgentOnEdgeMovingForward(agent);
        double position = progressFromStart(agent, forward);
        int index = segmentIndexForProgressFromStart(position);
        segments[index].addAgent(agent, forward);
    }

    /**
     * Returns the total surface occupied by agents travelling in a given direction
     * in
     * a window of segments.
     *
     * @param low              inclusive index of the first segment in the window
     * @param high             inclusive index of the last segment in the window
     * @param forwardDirection {@code true} for agents moving from start to end,
     *                         {@code false} for agents moving from end to start
     * @return the total occupied surface in that window and direction, or 0.0 if
     *         the indices are out of bounds
     */
    public double getAreaOccupiedByAgentsBetween(int low, int high, boolean forwardDirection) {
        ensureSegments();
        if (low < 0 || high >= segments.length || low > high) {
            return 0.0;
        }
        double occupiedSurface = 0.0;
        for (int i = low; i <= high; i++) {
            occupiedSurface += segments[i].getOccupiedSurface(forwardDirection);
        }
        return occupiedSurface;
    }

    /**
     * Overload of {@link #getAreaOccupiedByAgentsBetween(int, int, boolean)} that
     * accepts position in meters instead of segment indices.
     *
     * @param startDistance    distance from the start node to the beginning of the
     *                         window (in meters)
     * @param endDistance      distance from the start node to the end of the window
     *                         (in meters)
     * @param forwardDirection {@code true} for agents moving from start to end,
     *                         {@code false} for agents moving from end to start
     * @return the total occupied surface in that window and direction, or 0.0 if
     *         the distances are out of bounds
     */
    public double getAreaOccupiedByAgentsBetween(double startDistance, double endDistance, boolean forwardDirection) {
        ensureSegments();
        if (startDistance < 0 || endDistance > length || startDistance > endDistance) {
            return 0.0;
        }
        int low = segmentIndexForProgressFromStart(startDistance / length);
        int high = segmentIndexForProgressFromStart(endDistance / length);
        return getAreaOccupiedByAgentsBetween(low, high, forwardDirection);
    }

    /**
     * Returns the total surface occupied by agents at the entrance of the edge.
     *
     * @param entranceNode the node at which to calculate the occupied surface
     * @return the total occupied surface at the entrance, or 0.0 if the node is not
     *         an endpoint of the edge
     */
    public double getTotalAreaOccupiedByAgentsAtEntrance(Node entranceNode) {
        boolean forwardDirection = entranceNode.equals(start);
        double agentWidth = AgentSettings.getInstance().getMedianSurfaceAreaTakenByAgent();
        double startDistance = forwardDirection ? 0 : length - agentWidth;
        double endDistance = forwardDirection ? agentWidth : length;
        assert startDistance >= 0 && endDistance <= length : "Invalid distance window for edge entrance";
        return getAreaOccupiedByAgentsBetween(startDistance, endDistance, forwardDirection)
                + getAreaOccupiedByAgentsBetween(startDistance, endDistance, !forwardDirection);
    }

    /**
     * Returns the number of agents at the entrance of the edge, regardless of their
     * direction.
     *
     * @param entranceNode the node at which to count the agents
     * @return the number of agents at the entrance, or 0 if the node is not an
     *         endpoint of the edge
     */
    public int getNumberOfAgentsEnteringFromNode(Node entranceNode) {
        boolean forwardDirection = entranceNode.equals(start);
        double agentWidth = AgentSettings.getInstance().getMedianSurfaceAreaTakenByAgent();
        double startDistance = forwardDirection ? 0 : length - agentWidth;
        double endDistance = forwardDirection ? agentWidth : length;
        assert startDistance >= 0 && endDistance <= length : "Invalid distance window for edge entrance";
        return getNumberOfAgentsBetween(startDistance, endDistance, forwardDirection);
        // only count agents moving in the direction of the entrance
    }

    /**
     * Checks if there is space left at the entrance of the edge for a new agent.
     *
     * @param entranceNode the node at which to check for space
     * @return {@code true} if there is space left at the entrance for the largest
     *         possible agent, {@code false}
     *         otherwise
     */
    public boolean hasSpaceLeftAtEntranceForMaxSizedAgent(Node entranceNode) {
        double occupiedSurface = getTotalAreaOccupiedByAgentsAtEntrance(entranceNode);
        double maxAgentSurface = AgentSettings.getInstance().getMAX_SURFACE_AREA_TAKEN_BY_AGENT();
        double medianAgentSurface = AgentSettings.getInstance().getMedianSurfaceAreaTakenByAgent();
        return occupiedSurface + maxAgentSurface <= width * medianAgentSurface;
    }

    /**
     * Returns the number of agents occupying the edge between two distances along it.
     *
     * @param startDistance    the start of the range (in meters from the edge start)
     * @param endDistance      the end of the range (in meters from the edge start)
     * @param forwardDirection {@code true} to count agents moving forward, {@code false} for backward
     * @return the number of agents in the specified range and direction
     */
    public int getNumberOfAgentsBetween(double startDistance, double endDistance, boolean forwardDirection) {
        ensureSegments();
        if (startDistance < 0 || endDistance > length || startDistance > endDistance) {
            return 0;
        }
        int low = segmentIndexForProgressFromStart(startDistance / length);
        int high = segmentIndexForProgressFromStart(endDistance / length);
        if (low < 0 || high >= segments.length || low > high) {
            throw new IllegalArgumentException("Calculated segment indices out of bounds");
        }
        int count = 0;
        for (int i = low; i <= high; i++) {
            List<Agent> agents = segments[i].getAgents(forwardDirection);
            count += agents.size();
        }
        return count;
    }

    /**
     * Returns the total available surface for agents travelling in
     * a window of segments. The available surface is the physical surface of the
     * window
     * (window length * edge width), regardless of the current congestion level,
     * because
     * the local density model accounts for congestion through the speed reduction
     * factor.
     *
     * @param low  inclusive index of the first segment in the window
     * @param high inclusive index of the last segment in the window
     * @return the total available surface in that window and direction, or 0.0 if
     *         the indices are out of bounds
     */
    public double getAvailableSurfaceBetween(int low, int high) {
        ensureSegments();
        double segmentLength = length / segments.length;
        double windowLength = (high - low + 1) * segmentLength;
        return windowLength * width;
    }

    /**
     * Computes the local density (occupied surface per available surface) around a
     * position, for agents travelling in a given physical direction.
     * <p>
     * A window in front of the agent is considered from
     * {@link GraphConfig#DENSITY_WINDOW_IN_FRONT} to
     * {@link GraphConfig#DENSITY_WINDOW_BEHIND} meters in front of the agent. The
     * numerator uses the pre-computed surface sums
     * ({@link Agent#getSurfaceAreaTakenByAgent()}, not a mere agent count); the
     * denominator is the physically available surface {@code windowLength * width}.
     * </p>
     *
     * @param progressFromStart the normalized position from the start node
     *                          ({@code [0, 1]})
     * @param forwardDirection  {@code true} to sum forward agents, {@code false}
     *                          to sum backward agents
     * @return the local density ratio (may exceed 1.0 when over-crowded)
     */
    public double getLocalDensity(double progressFromStart, boolean forwardDirection) {
        ensureSegments();
        int n = segments.length;
        int center = segmentIndexForProgressFromStart(progressFromStart);
        double radiusInFront = GraphConfig.DENSITY_WINDOW_IN_FRONT; // meters in front
        double radiusPercent = radiusInFront / length;
        int radiusInSegmentInFront = segmentIndexForProgressFromStart(radiusPercent);
        double radiusBehind = GraphConfig.DENSITY_WINDOW_BEHIND; // meters behind
        double radiusBehindPercent = radiusBehind / length;
        int radiusBehindInSegments = segmentIndexForProgressFromStart(radiusBehindPercent);

        int low = Math.max(Math.min(n - 1, center + radiusBehindInSegments), 0);
        int high = Math.min(n - 1, center + radiusInSegmentInFront);

        double occupiedSurface = getAreaOccupiedByAgentsBetween(low, high, forwardDirection);

        double availableSurface = getAvailableSurfaceBetween(low, high);
        if (availableSurface <= 0.0) {
            return 0.0;
        }
        return occupiedSurface / availableSurface;
    }

    /**
     * Free-flow maximum speed of the edge, i.e. the speed an agent would reach
     * with no local congestion. Unlike {@link #getMaxAgentSpeed()}, it does not
     * apply the global congestion factor, because the local model now accounts for
     * crowding through {@link #getLocalDensity(double, boolean)}.
     *
     * @return the free-flow maximum speed (m/s)
     */
    public double getFreeFlowMaxAgentSpeed() {
        double base = AgentSettings.getInstance().getMAX_RUNNING_SPEED();
        return isOnFire() ? base * 1.5 : base;
    }

    /**
     * Local, position-dependent maximum speed for an agent on this edge.
     * <p>
     * The speed follows {@code v = v_max * exp(-alpha * rho_same - beta *
     * rho_opposite)}, where {@code rho_same} and {@code rho_opposite} are the local
     * densities of agents moving in the same and opposite directions around the
     * agent, and {@code alpha}/{@code beta} are taken from {@link AgentSettings}
     * (with {@code beta > alpha} so counter-flow slows agents down more). When
     * enabled, the result is further capped so the agent does not overlap the
     * closest agent ahead.
     * </p>
     *
     * @param agent the agent currently on this edge
     * @return the local maximum speed (m/s), never below {@code 0.1}
     */
    public double getLocalMaxAgentSpeedInDirection(Agent agent) {
        ensureSegments();
        boolean forward = isAgentOnEdgeMovingForward(agent);
        double position = progressFromStart(agent, forward);

        double sameDensity = getLocalDensity(position, forward);
        double oppositeDensity = getLocalDensity(position, !forward);

        AgentSettings settings = AgentSettings.getInstance();
        double alpha = settings.getCONGESTION_ALPHA();
        double beta = settings.getCONGESTION_BETA();
        double factor = Math.exp(-alpha * sameDensity - beta * oppositeDensity);

        double speed = getFreeFlowMaxAgentSpeed() * factor;
        return Math.max(speed, 0.1);
    }

    /**
     * Returns the total surface occupied by agents moving in the backward
     * direction.
     *
     * @return the total occupied surface for backward-moving agents
     */
    public double getTotalOccupiedSurfaceBackward() {
        return totalOccupiedSurfaceBackward;
    }

    /**
     * Returns the total surface occupied by agents moving in the forward direction.
     *
     * @return the total occupied surface for forward-moving agents
     */
    public double getTotalOccupiedSurfaceForward() {
        return totalOccupiedSurfaceForward;
    }

    public double getDirectionalContributionToCongestion(boolean forwardDirection) {
        double total = getCapacity();
        return total > 0 ? (forwardDirection ? totalOccupiedSurfaceForward : totalOccupiedSurfaceBackward) / total : 0.0;
    }

    /**
     * Returns the ratio of the total surface occupied by agents moving in the
     * forward direction to the total surface occupied by all agents on the edge.
     *
     * @return the forward occupied surface ratio, or 0.0 if there are no agents on
     *         the edge
     */
    public double getOccupiedSurfaceRatioInDirection(boolean forwardDirection) {
        double total = totalOccupiedSurfaceForward + totalOccupiedSurfaceBackward;
        return total > 0 ? (forwardDirection ? totalOccupiedSurfaceForward : totalOccupiedSurfaceBackward) / total : 0.0;
    }

    /**
     * Returns the count of agents moving in the backward direction.
     *
     * @return the count of backward-moving agents
     */
    public int getAgentCountBackward() {
        return agentCountBackward;
    }

    /**
     * Returns the count of agents moving in the forward direction.
     *
     * @return the count of forward-moving agents
     */
    public int getAgentCountForward() {
        return agentCountForward;
    }

    @Override
    public double getOccupiedSpace() {
        return totalOccupiedSurfaceForward + totalOccupiedSurfaceBackward;
    }

    /**
     * Checks if the largest possible agent can fit at the entrance of the edge when
     * it is empty.
     *
     * @param entranceNode the node at which to check for space
     * @return {@code true} if the largest possible agent can fit at the entrance
     *         when the edge is empty, {@code false}
     */
    public boolean canMaxSizedAgentFitAtEntranceWhenEdgeIsEmpty(Node entranceNode) {
        double maxAgentSurface = AgentSettings.getInstance().getMAX_SURFACE_AREA_TAKEN_BY_AGENT();
        return maxAgentSurface <= width;
    }

    public boolean isMovingForward(Agent agent) {
        if (agent.isOnEdge()){
            return isAgentOnEdgeMovingForward(agent);
        }
        return start.equals(Objects.requireNonNull(agent.getCurrentNode()));
    }

    /**
     * Registers the addition of an agent to the edge and updates the corresponding
     * counts and occupied surfaces.
     *
     * @param agent the agent to add
     */
    @Override
    protected void registerAgentAddition(Agent agent) {
        super.registerAgentAddition(agent);
        boolean forward = isMovingForward(agent);
        if (forward) {
            this.agentCountForward++;
            this.totalOccupiedSurfaceForward += agent.getSurfaceAreaTakenByAgent();
        } else {
            this.agentCountBackward++;
            this.totalOccupiedSurfaceBackward += agent.getSurfaceAreaTakenByAgent();
        }
    }

    /**
     * Removes an agent from the edge and updates the corresponding counts and
     * occupied surfaces.
     *
     * @param agent the agent to remove
     * @return {@code true} if the agent was successfully removed, {@code false}
     *         otherwise
     */
    @Override
    public boolean removeAgent(Agent agent) {
        boolean removed = super.removeAgent(agent);
        if (removed) {
            boolean forward = isMovingForward(agent);
            if (forward) {
                this.agentCountForward--;
                this.totalOccupiedSurfaceForward -= agent.getSurfaceAreaTakenByAgent();
            } else {
                this.agentCountBackward--;
                this.totalOccupiedSurfaceBackward -= agent.getSurfaceAreaTakenByAgent();
            }
        }
        this.agentCountBackward = Math.max(0, this.agentCountBackward); //dirty fix
        this.agentCountForward = Math.max(0, this.agentCountForward);
        this.totalOccupiedSurfaceBackward = Math.max(0.0, this.totalOccupiedSurfaceBackward);
        this.totalOccupiedSurfaceForward = Math.max(0.0, this.totalOccupiedSurfaceForward);
        return removed;
    }

    @Override
    public List<Node> getNeighbors() {
        return List.of(start, end);
    }

    @Override
    public String toString() {
        return String.format(
                "Edge{id=%d, startNode=%d, endNode=%d, length=%.2f, width=%.2f, speed=%.2f, directed=%b, fire=%b, cong=%.2f}",
                getId(), start.getId(), end.getId(), length, width, getMaxAgentSpeed(), directed, isOnFire(),
                getCongestion());
    }

    /**
     * Returns whether fire is spreading from the start node of this edge.
     *
     * @return {@code true} if the edge is burning from the start node
     */
    public boolean isBurningFromStart() {
        return burningFromStart;
    }

    /**
     * Returns whether fire is spreading from the end node of this edge.
     *
     * @return {@code true} if the edge is burning from the end node
     */
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

    /**
     * Returns the distance that has been burned along this edge.
     *
     * @return The distance burned along the edge length.
     */
    public double getBurnedDistance() {
        return !isOnFire() ? 0.0 : getFire().getBurningTime() * getFire().getSpreadRate();
    }

    /**
     * Returns whether the entire edge has been consumed by fire.
     *
     * @return True if the entire edge is consumed by fire.
     */
    public boolean isFullyBurned() {
        if (!isOnFire())
            return false;
        double distance = getBurnedDistance();
        return (burningFromEnd && burningFromStart) ? (distance * 2) >= length : distance >= length;
    }

    /**
     * Sets the start node of this edge.
     *
     * @param node the new start node
     */
    public void setStart(Node node) {
        start = node;
    }

    /**
     * Sets the end node of this edge.
     *
     * @param node the new end node
     */
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

    /**
     * Returns the proportion of this edge that has been burned.
     *
     * @return Percentage of edge burned (0.0 to 1.0).
     */
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