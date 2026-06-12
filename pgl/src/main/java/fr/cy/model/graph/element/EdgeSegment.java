package fr.cy.model.graph.element;
 
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
 
import fr.cy.model.agent.Agent;
 
/**
 * A single discretization bucket of an {@link Edge} used by the local
 * congestion model.
 * <p>
 * Each segment keeps track of the agents that are currently located within its
 * span, split by travel direction along the edge:
 * </p>
 * <ul>
 * <li><b>forward</b>: agents moving from the edge {@code start} node towards the
 * {@code end} node;</li>
 * <li><b>backward</b>: agents moving from the {@code end} node towards the
 * {@code start} node.</li>
 * </ul>
 * <p>
 * For each direction the segment also maintains the running sum of the surface
 * areas occupied by its agents
 * ({@link Agent#getSurfaceAreaTakenByAgent()}). These pre-computed sums let the
 * edge evaluate the local density of a window of segments in constant time per
 * segment, without iterating over the agents again.
 * </p>
 */
public class EdgeSegment implements Serializable {
 
    private static final long serialVersionUID = 1L;
 
    /** Agents moving from the edge start node towards the end node. */
    private final List<Agent> forwardAgents = new ArrayList<>();
    /** Agents moving from the edge end node towards the start node. */
    private final List<Agent> backwardAgents = new ArrayList<>();
 
    /** Pre-computed sum of occupied surfaces for forward agents. */
    private double forwardOccupiedSurface = 0.0;
    /** Pre-computed sum of occupied surfaces for backward agents. */
    private double backwardOccupiedSurface = 0.0;
 
    /**
     * Registers an agent in this segment for the given travel direction and
     * updates the corresponding occupied-surface sum.
     *
     * @param agent   the agent to add (must not be {@code null})
     * @param forward {@code true} if the agent travels from start to end,
     *                {@code false} if it travels from end to start
     */
    public void addAgent(Agent agent, boolean forward) {
        double surface = agent.getSurfaceAreaTakenByAgent();
        if (forward) {
            forwardAgents.add(agent);
            forwardOccupiedSurface += surface;
        } else {
            backwardAgents.add(agent);
            backwardOccupiedSurface += surface;
        }
    }
 
    /**
     * Clears both agent lists and resets the occupied-surface sums. Used when the
     * segments are rebuilt at the beginning of a simulation tick.
     */
    public void clear() {
        forwardAgents.clear();
        backwardAgents.clear();
        forwardOccupiedSurface = 0.0;
        backwardOccupiedSurface = 0.0;
    }
 
    /**
     * Returns the occupied surface for the requested direction.
     *
     * @param forward {@code true} for the forward direction, {@code false} for the
     *                backward direction
     * @return the pre-computed sum of occupied surfaces in that direction
     */
    public double getOccupiedSurface(boolean forward) {
        return forward ? forwardOccupiedSurface : backwardOccupiedSurface;
    }
 
    /**
     * Returns the agents travelling in the requested direction.
     *
     * @param forward {@code true} for the forward direction, {@code false} for the
     *                backward direction
     * @return the (live) list of agents in that direction
     */
    public List<Agent> getAgents(boolean forward) {
        return forward ? forwardAgents : backwardAgents;
    }
 
    /** @return the occupied surface of forward agents. */
    public double getForwardOccupiedSurface() {
        return forwardOccupiedSurface;
    }
 
    /** @return the occupied surface of backward agents. */
    public double getBackwardOccupiedSurface() {
        return backwardOccupiedSurface;
    }
 
    /** @return the agents moving from start to end. */
    public List<Agent> getForwardAgents() {
        return forwardAgents;
    }
 
    /** @return the agents moving from end to start. */
    public List<Agent> getBackwardAgents() {
        return backwardAgents;
    }
}
