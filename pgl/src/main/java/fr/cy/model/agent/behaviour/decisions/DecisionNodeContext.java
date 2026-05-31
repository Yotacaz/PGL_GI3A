package fr.cy.model.agent.behaviour.decisions;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import fr.cy.model.agent.Agent;
import fr.cy.model.graph.CongestionStats;
import fr.cy.model.graph.element.Edge;
import fr.cy.model.graph.element.Node;
import fr.cy.model.pathfinding.GraphPath;

public class DecisionNodeContext implements Serializable {
    private static final long serialVersionUID = 1L;
    /** The node from which the decision is made */
    private Node sourceNode;
    private GraphPath recommendedPath;
    private GraphPath shortestPathToExit;
    private List<Edge> outgoingEdges;
    private Map<Edge, List<Agent>> incomingNearbyAgents;
    private Map<Edge, List<Agent>> outgoingNearbyAgents;
    private CongestionStats<Edge> congestionStatsForOutgoingEdges;
    /*
     * Map of edges to the number of agents currently taking or planning to next
     * take that edge
     */

    DecisionNodeContext(Node sourceNode, GraphPath recommendedPath, GraphPath shortestPathToExit,
            List<Edge> outgoingEdges,
            Map<Edge, List<Agent>> incomingNearbyAgents,
            Map<Edge, List<Agent>> outgoingNearbyAgents) {
        this.sourceNode = sourceNode;
        this.recommendedPath = recommendedPath;
        this.shortestPathToExit = shortestPathToExit;
        this.outgoingEdges = outgoingEdges;
        this.incomingNearbyAgents = incomingNearbyAgents;
        this.outgoingNearbyAgents = outgoingNearbyAgents;
        this.congestionStatsForOutgoingEdges = CongestionStats.computeCongestionStats(outgoingEdges);
    }

    // public void clear() {
    // incomingNearbyAgents.clear();
    // outgoingNearbyAgents.clear();
    // outgoingEdges.clear();
    // }

    public List<Edge> getOutgoingEdges() {
        return Collections.unmodifiableList(outgoingEdges);
    }

    public CongestionStats<Edge> getCongestionStatsForOutgoingEdges() {
        return congestionStatsForOutgoingEdges;
    }

    public List<Edge> getSortedOutgoingEdgesByCongestion() {
        List<Edge> sortedEdges = congestionStatsForOutgoingEdges.getSortedByCongestion();
        return Collections.unmodifiableList(sortedEdges);
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

    void registerOutgoingIntent(Edge edge, Agent agent) {
        if (edge == null || agent == null) {
            return;
        }
        // removeOutgoingIntent(agent);
        List<Agent> agents = outgoingNearbyAgents.computeIfAbsent(edge, k -> new ArrayList<>());
        if (!agents.contains(agent)) {
            agents.add(agent);
        }
    }

    // void removeOutgoingIntent(Agent agent) {
    // if (agent == null) {
    // return;
    // }
    // for (List<Agent> agents : outgoingNearbyAgents.values()) {
    // agents.remove(agent);
    // }
    // }

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
        DecisionNodeContext other = (DecisionNodeContext) obj;
        return sourceNode.equals(other.sourceNode);
    }

    @Override
    public String toString() {
        return "DecisionNodeContext{" +
                "sourceNode=" + (sourceNode == null ? "null" : sourceNode.toString()) +
                ", outgoingEdges=" + (outgoingEdges == null ? 0 : outgoingEdges.size()) +
                '}';
    }

}
