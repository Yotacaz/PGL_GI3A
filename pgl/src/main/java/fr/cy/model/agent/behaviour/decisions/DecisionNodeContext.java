package fr.cy.model.agent.behaviour.decisions;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.cy.model.agent.Agent;
import fr.cy.model.agent.behaviour.agentActions.AgentAction;
import fr.cy.model.graph.element.Edge;
import fr.cy.model.graph.element.GraphElement;
import fr.cy.model.graph.element.Node;
import fr.cy.model.pathfinding.GraphPath;

public class DecisionNodeContext {
    /** The node from which the decision is made */
    private Node sourceNode;
    private GraphPath recommendedPath;
    private List<Edge> outgoingEdges;
    private Map<Edge, List<Agent>> incomingNearbyAgents;
    private Map<Edge, List<Agent>> outgoingNearbyAgents;
    /**Read only map of agent actions for this tick*/
    private double localStressLevel;
    private double localCrowdingLevel;
    private int totalNumberOfNearbyAgents;
    /* Map of edges to the number of agents currently taking or planning to next take that edge */

    DecisionNodeContext(Node sourceNode, GraphPath recommendedPath, List<Edge> outgoingEdges,
            Map<Edge, List<Agent>> incomingNearbyAgents,
            Map<Edge, List<Agent>> outgoingNearbyAgents, double localStressLevel, double localCrowdingLevel) {
        this.sourceNode = sourceNode;
        this.recommendedPath = recommendedPath;
        this.outgoingEdges = outgoingEdges;
        this.incomingNearbyAgents = incomingNearbyAgents;
        this.outgoingNearbyAgents = outgoingNearbyAgents;
        this.localStressLevel = localStressLevel;
        this.localCrowdingLevel = localCrowdingLevel;
        this.totalNumberOfNearbyAgents = incomingNearbyAgents.values().stream().mapToInt(List::size).sum() +
                outgoingNearbyAgents.values().stream().mapToInt(List::size).sum();
    }

    // public void clear() {
    //     incomingNearbyAgents.clear();
    //     outgoingNearbyAgents.clear();
    //     outgoingEdges.clear();
    // }

    public int getTotalNumberOfNearbyAgents() {
        return totalNumberOfNearbyAgents;
    }

    public double getLocalCrowdingLevel() {
        return localCrowdingLevel;
    }

    public double getLocalStressLevel() {
        return localStressLevel;
    }

    public List<Edge> getOutgoingEdges() {
        return Collections.unmodifiableList(outgoingEdges);
    }

    /**
     * Returns the modifiable map of nearby agents on incoming edges and nodes. The keys are the graph elements (edges or nodes) that are adjacent to the source node, and the values are lists of agents that are currently on those elements or planning to move onto them.
     * This method should stays package-private to prevent external modification of the map structure, but allows modification of the lists of agents for each graph element.
     * @return the map of nearby agents on incoming edges and nodes
     */
    Map<Edge, List<Agent>> getIncomingNearbyAgents() {
        return incomingNearbyAgents;
    }

    /**
     * Returns the modifiable map of nearby agents on outgoing edges and nodes. The keys are the graph elements (edges or nodes) that are adjacent to the source node, and the values are lists of agents that are currently on those elements or planning to move onto them.
     * This method should stays package-private to prevent external modification of the map structure, but allows
     * @return the map of nearby agents on outgoing edges and nodes
     */
    Map<Edge, List<Agent>> getOutgoingNearbyAgents() {
        return outgoingNearbyAgents;
    }

    // public Agent getRandomAgentInOutgoingEdge() {
    //     // outgoingNearbyAgents.
    // }

    public GraphPath getRecommendedPath() {
        return recommendedPath;
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

}
