package fr.cy.model.agent.context;

import java.util.List;
import java.util.Map;

import fr.cy.model.agent.Agent;
import fr.cy.model.graph.CongestionStats;
import fr.cy.model.graph.element.Edge;
import fr.cy.model.graph.element.Node;
import fr.cy.model.pathfinding.GraphPath;

/**
 * Context for agents on a node, providing all necessary information for decision-making.
 * 
 * <p>This class encapsulates the contextual information that an agent needs when
 * making decisions at a node, including available edges, nearby agents, recommended
 * paths, and congestion information.</p>
 */
public class NodeContext extends AbstractGraphElementContext<Edge> {
    private static final long serialVersionUID = 1L;
    
    /** The node from which the decision is made */
    private Node sourceNode;
    
    /** The recommended path from this node to an exit */
    private GraphPath recommendedPath;
    
    /** The shortest path from this node to the nearest exit */
    private GraphPath shortestPathToExit;
    
    /**
     * Map of edges to the number of agents currently on or planning to use that edge.
     * This represents agents leaving the node via connected edges.
     */
    private Map<Edge, Integer> outgoingNearbyAgentsEnteringEdge;
    
    /**
     * Map of edges to the amount of space occupied by agents entering that edge.
     * Used to calculate congestion and available capacity.
     */
    private Map<Edge, Double> spaceOccupiedAgentEnteringEdge;

    /**
     * Creates a new NodeContext with the specified parameters.
     * 
     * @param sourceNode the node from which decisions are being made
     * @param recommendedPath the recommended path from this node to an exit
     * @param shortestPathToExit the shortest path from this node to the nearest exit
     * @param outgoingEdges the list of edges leaving this node
     * @param incomingNearbyAgents agents approaching this node via incoming edges
     * @param outgoingNearbyAgents agents leaving this node via outgoing edges
     * @param spaceOccupiedAgentEnteringEdge space occupation data for each outgoing edge
     */
    NodeContext(Node sourceNode, GraphPath recommendedPath, GraphPath shortestPathToExit,
            Map<Edge, Integer> outgoingNearbyAgents, Map<Edge, Double> spaceOccupiedAgentEnteringEdge) {
        super(sourceNode.getNeighbors());
        this.sourceNode = sourceNode;
        this.recommendedPath = recommendedPath;
        this.shortestPathToExit = shortestPathToExit;
        this.outgoingNearbyAgentsEnteringEdge = outgoingNearbyAgents;
        this.spaceOccupiedAgentEnteringEdge = spaceOccupiedAgentEnteringEdge;
        
    }

    /**
     * Gets congestion statistics for the outgoing edges from this node.
     * 
     * @return congestion statistics containing occupancy and capacity information
     */
    public CongestionStats<Edge> getCongestionStatsForOutgoingEdges() {
        return getCongestionStatsForAccessibleElements();
    }

    /**
     * Gets the outgoing edges sorted by congestion level.
     * 
     * @return the list of outgoing edges sorted from least to most congested
     */
    public List<Edge> getSortedOutgoingEdgesByCongestion() {
        return getSortedAccessibleElementsByCongestion();
    }

    /**
     * Gets the number of agents entering the specified edge.
     *
     * @param edge the edge for which to count entering agents
     * @return the number of agents entering the edge
     */
    public int getNumAgentsEnteringEdge(Edge edge) {
        return outgoingNearbyAgentsEnteringEdge.getOrDefault(edge, 0);
    }

    public double getSpaceOccupiedAtEdgeEntrance(Edge edge) {
        return spaceOccupiedAgentEnteringEdge.getOrDefault(edge, 0.0);
    }
    
    @Override
    boolean registerOutgoingIntent(Edge edge, Agent agent) {
        if (edge == null || agent == null) {
            return true;
        }

        double pendingSpaceOnEdge = spaceOccupiedAgentEnteringEdge.getOrDefault(edge, 0.0);

        double futureOccupiedSpace = pendingSpaceOnEdge + agent.getSurfaceAreaTakenByAgent();
        if (futureOccupiedSpace > edge.getWidth()) {
            return false;
        }

        int numAgents = outgoingNearbyAgentsEnteringEdge.getOrDefault(edge, 0);
        if (numAgents > 0) {
            outgoingNearbyAgentsEnteringEdge.put(edge, numAgents + 1);
        } else {
            outgoingNearbyAgentsEnteringEdge.put(edge, 1);
        }
        spaceOccupiedAgentEnteringEdge.put(edge, futureOccupiedSpace);
        return true;
    }

    public GraphPath getRecommendedPath() {
        return recommendedPath;
    }

    public GraphPath getShortestPathToExit() {
        return shortestPathToExit;
    }

    public Node getSourceNode() {
        return sourceNode;
    }

    @Override
    public int hashCode() {
        return sourceNode.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        NodeContext other = (NodeContext) obj;
        return sourceNode.equals(other.sourceNode);
    }

    @Override
    public String toString() {
        return "DecisionNodeContext{" +
                "sourceNode=" + (sourceNode == null ? "null" : sourceNode.toString()) +
                ",\n recommendedPath=" + (recommendedPath == null ? "null" : recommendedPath.toString()) +
                ",\n shortestPathToExit=" + (shortestPathToExit == null ? "null" : shortestPathToExit.toString()) +
                ",\n outgoingNearbyAgentsEnteringEdge=" + (outgoingNearbyAgentsEnteringEdge == null ? "null" : outgoingNearbyAgentsEnteringEdge.toString()) +
                ",\n spaceOccupiedAgentEnteringEdge=" + (spaceOccupiedAgentEnteringEdge == null ? "null" : spaceOccupiedAgentEnteringEdge.toString()) +
                '}';
    }

}
