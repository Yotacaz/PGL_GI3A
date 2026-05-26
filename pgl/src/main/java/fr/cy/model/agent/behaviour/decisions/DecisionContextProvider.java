package fr.cy.model.agent.behaviour.decisions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;

import fr.cy.model.agent.Agent;
import fr.cy.model.graph.Graph;
import fr.cy.model.graph.element.GraphElement;
import fr.cy.model.graph.element.Node;
import fr.cy.model.graph.element.Edge;
import fr.cy.model.pathfinding.GraphPath;
import fr.cy.model.pathfinding.PathFinder;

public class DecisionContextProvider {
    private final Graph graph;
    private final PathFinder pathFinder;
    private Map<Node, DecisionNodeContext> cachedContexts = new HashMap<>();

    // private final 
    public DecisionContextProvider(Graph graph, PathFinder pathFinder) {
        this.graph = graph;
        this.pathFinder = pathFinder;
    }

    public DecisionNodeContext getContext(Agent agent) {
        Node node = agent.getCurrentNode();
        DecisionNodeContext cachedContext = cachedContexts.getOrDefault(node, null);
        if (cachedContext == null) {
            cachedContext = constructContext(agent);
            cachedContexts.put(node, cachedContext);
        }else{
            

        }
        return cachedContext;
    }

    public void clearCache() {
        cachedContexts.clear();
    }

    // private DecisionNodeContext updateContext(DecisionNodeContext context){
    //     // Only the agent on node and their planned outgoing edge can change before tick finishes
    //     Node node = context.getSourceNode();
    //     Map<GraphElement, List<Agent>> incomingNearbyAgents = context.getIncomingNearbyAgents();
    //     //TODO    
    // }

    private DecisionNodeContext constructContext(Agent agent) {
        // List<Node> recommendedPath = pathFinder.findPath(agent.getCurrentNode(), agent.getDestinationNode());
        Node currentNode = Objects.requireNonNull(agent.getCurrentNode(),
                "Node must be valid to construct decision context");
        List<Edge> allEdges = currentNode.getEdges();
        Map<Edge, List<Agent>> nearbyIncomingAgents = new HashMap<>();
        Map<Edge, List<Agent>> nearbyOutgoingAgents = new HashMap<>();
        for (Edge edge : allEdges) {
            for (Agent nearbyAgent : edge.getAgents()) {
                // Determine if the nearby agent is incoming or outgoing relative to the current node
                Node oppositeNode = edge.getOppositeNode(currentNode);
                if (oppositeNode.equals(nearbyAgent.getPreviousNode())) {
                    nearbyIncomingAgents.computeIfAbsent(edge, k -> new ArrayList<>()).add(nearbyAgent);
                } else {
                    nearbyOutgoingAgents.computeIfAbsent(edge, k -> new ArrayList<>()).add(nearbyAgent);
                }
            }
        }
        // account for agents that are currently on the node and planning to take an outgoing edge
        for (Agent nearbyAgent : currentNode.getAgents()) {
            if (!nearbyAgent.equals(agent)) {
                Edge targetOutgoinEdge = nearbyAgent.getCurrentEdgeOrNextEdgeIfOnNode();
                if (targetOutgoinEdge != null) {
                    nearbyOutgoingAgents.computeIfAbsent(targetOutgoinEdge, k -> new ArrayList<>()).add(nearbyAgent);
                }
            }
        }

        // double localStressLevel = 0.5; // Placeholder value
        // double localCrowdingLevel = 0.5; // Placeholder value

        // GraphPath recommendedPath = pathFinder.findPath(agent.getCurrentNode(), null);
        List<Edge> outgoingEdges = currentNode.getOutgoingEdges();
        return new DecisionNodeContext(currentNode, null, outgoingEdges, nearbyIncomingAgents, nearbyOutgoingAgents,
                agent.getStressLevel(), currentNode.getCongestion());

    }

}
