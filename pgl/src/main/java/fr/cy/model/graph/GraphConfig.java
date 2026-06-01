package fr.cy.model.graph;

import java.io.Serializable;


public class GraphConfig implements Serializable {
    private static final long serialVersionUID = 1L;
    public static final double DEFAULT_NODE_CAPACITY = 20.0;
    public static final double DEFAULT_EDGE_LENGTH = 10.0;
    public static final double DEFAULT_EDGE_WIDTH = 5.0;
    public static final boolean DEFAULT_EDGE_DIRECTED = false;
    // public static final double DEFAULT_EDGE_MAX_AGENT_SPEED = AgentSettings.getInstance().getRUNNING_SPEED()*1.5; // Default max speed for agents on edges
}
