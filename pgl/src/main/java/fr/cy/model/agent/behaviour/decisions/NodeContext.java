package fr.cy.model.agent.behaviour.decisions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import fr.cy.model.agent.Agent;
import fr.cy.model.graph.CongestionStats;
import fr.cy.model.graph.element.Edge;
import fr.cy.model.graph.element.Node;
import fr.cy.model.pathfinding.GraphPath;

/** Context for agents on a node */
public class NodeContext extends AbstractGraphElementContext<Edge> {
    private static final long serialVersionUID = 1L;
    /** The node from which the decision is made */
    private Node sourceNode;
    private GraphPath recommendedPath;
    private GraphPath shortestPathToExit;
    /**
     * Map of edges to the agents currently taking or planning to take that edge.
     */
    private Map<Edge, List<Agent>> incomingNearbyAgents;
    private Map<Edge, List<Agent>> outgoingNearbyAgents;
    private Map<Edge, Double> spaceOccupiedAgentEnteringEdge;

    NodeContext(Node sourceNode, GraphPath recommendedPath, GraphPath shortestPathToExit,
            List<Edge> outgoingEdges,
            Map<Edge, List<Agent>> incomingNearbyAgents,
            Map<Edge, List<Agent>> outgoingNearbyAgents, Map<Edge, Double> spaceOccupiedAgentEnteringEdge) {
        super(outgoingEdges);
        this.sourceNode = sourceNode;
        this.recommendedPath = recommendedPath;
        this.shortestPathToExit = shortestPathToExit;
        this.incomingNearbyAgents = incomingNearbyAgents;
        this.outgoingNearbyAgents = outgoingNearbyAgents;
        this.spaceOccupiedAgentEnteringEdge = spaceOccupiedAgentEnteringEdge;
        
    }

    // public void clear() {
    // incomingNearbyAgents.clear();
    // outgoingNearbyAgents.clear();
    // outgoingEdges.clear();
    // }

    public List<Edge> getOutgoingEdges() {
        return getAccessibleElements();
    }

    public CongestionStats<Edge> getCongestionStatsForOutgoingEdges() {
        return getCongestionStatsForAccessibleElements();
    }

    public List<Edge> getSortedOutgoingEdgesByCongestion() {
        return getSortedAccessibleElementsByCongestion();
    }

    /**
     * Returns the modifiable map of nearby agents on incoming edges and nodes. The
     * keys are the graph elements (edges or nodes) that are adjacent to the source
     * node, and the values are lists of agents that are currently on those elements
     * or planning to move onto them.
     * This method should stays package-private to prevent external modification of
     * the map structure, but allows modification of the lists of agents for each
     * graph element.
     * 
     * @return the map of nearby agents on incoming edges and nodes
     */
    Map<Edge, List<Agent>> getIncomingNearbyAgents() {
        return incomingNearbyAgents;
    }

    /**
     * Returns the modifiable map of nearby agents on outgoing edges and nodes. The
     * keys are the graph elements (edges or nodes) that are adjacent to the source
     * node, and the values are lists of agents that are currently on those elements
     * or planning to move onto them.
     * This method should stays package-private to prevent external modification of
     * the map structure, but allows modification of the lists of agents for each
     * graph element.
     * 
     * @return the map of nearby agents on outgoing edges and nodes
     */
    Map<Edge, List<Agent>> getOutgoingNearbyAgents() {
        return outgoingNearbyAgents;
    }

    boolean registerOutgoingIntent(Edge edge, Agent agent) {
        if (edge == null || agent == null) {
            return true;
        }

        double pendingSpaceOnEdge = spaceOccupiedAgentEnteringEdge.getOrDefault(edge, 0.0);

        double futureOccupiedSpace = pendingSpaceOnEdge + agent.getSurfaceAreaTakenByAgent();
        if (futureOccupiedSpace > edge.getWidth()) {
            return false;
        }

        List<Agent> agents = outgoingNearbyAgents.computeIfAbsent(edge, k -> new ArrayList<>());
        if (!agents.contains(agent)) {
            agents.add(agent);
            spaceOccupiedAgentEnteringEdge.put(edge, futureOccupiedSpace);
        }
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
                ", outgoingEdges=" + getOutgoingEdges().size() +
                '}';
    }

}
