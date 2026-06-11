package fr.cy.model.graph;

import java.io.Serializable;

public class GraphConfig implements Serializable {
    private static final long serialVersionUID = 1L;
    public static final double DEFAULT_NODE_CAPACITY = 20.0;
    public static final double DEFAULT_EDGE_LENGTH = 10.0;
    public static final double DEFAULT_EDGE_WIDTH = 2.0;
    public static final boolean DEFAULT_EDGE_DIRECTED = false;

    /**
     * Target length (in meters) of a single discretization segment used by the
     * local congestion model. The number of segments of an edge is derived from
     * its length so that each segment is roughly {@code SEGMENT_TARGET_LENGTH}
     * meters long.
     */
    public static final double SEGMENT_TARGET_LENGTH = 2.0;
    /** Minimum number of segments an edge is split into. */
    public static final int MIN_SEGMENTS_PER_EDGE = 1;
    /** Maximum number of segments an edge is split into (caps memory usage). */
    public static final int MAX_SEGMENTS_PER_EDGE = 64;
    /**
     * Radius, expressed in number of segments, of the symmetric window used when
     * computing the local density around an agent (the window spans
     * {@code 2 * DENSITY_WINDOW_RADIUS + 1} segments).
     */
    public static final int DENSITY_WINDOW_RADIUS = 1;
    // public static final double DEFAULT_EDGE_MAX_AGENT_SPEED =
    // AgentSettings.getInstance().getRUNNING_SPEED()*1.5; // Default max speed for
    // agents on edges
}
