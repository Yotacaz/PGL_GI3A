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
    public static final double SEGMENT_TARGET_LENGTH = 0.5;
    /** Minimum number of segments an edge is split into. */
    public static final int MIN_SEGMENTS_PER_EDGE = 1;
    /** Maximum number of segments an edge is split into (caps memory usage). */
    public static final int MAX_SEGMENTS_PER_EDGE = 256;
    /**
     * Distance in front of an agent to look when computing the local density 
     */
    public static final double DENSITY_WINDOW_IN_FRONT = 1.75;

    /**
     * Distance behind an agent to look when computing the local density
     * Negative values are used to indicate that the window ends behind the agent's current position
     * Positive values are used to indicate that the window ends in front of the agent's current position 
     */
    public static final double DENSITY_WINDOW_BEHIND = 0.3;
}
