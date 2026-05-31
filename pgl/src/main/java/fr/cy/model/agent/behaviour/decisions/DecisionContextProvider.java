package fr.cy.model.agent.behaviour.decisions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import fr.cy.model.agent.Agent;
import fr.cy.model.agent.behaviour.agentActions.AgentAction;
import fr.cy.model.graph.Graph;
import fr.cy.model.graph.element.Node;
import fr.cy.model.graph.element.Edge;
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
        if (node == null) {
            return null;
        }
        DecisionNodeContext cachedContext = cachedContexts.get(node);
        if (cachedContext == null) {
            cachedContext = constructContext(agent);
            cachedContexts.put(node, cachedContext);
        }
        return cachedContext;
    }

    public void clearCache() {
        cachedContexts.clear();
    }

    public void registerChosenAction(Agent agent, AgentAction action) {
        if (agent == null || action == null) {
            return;
        }
        Node currentNode = agent.getCurrentNode();
        if (currentNode == null) {
            return;
        }
        DecisionNodeContext context = cachedContexts.get(currentNode);
        if (context == null) {
            return;
        }
        context.registerOutgoingIntent(action.getClosestTargetGraphElement(), agent);
    }

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
                if (oppositeNode.equals(nearbyAgent.getPreviousOrCurrentNode())) {
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
        return new DecisionNodeContext(currentNode, null, null, outgoingEdges, nearbyIncomingAgents,
                nearbyOutgoingAgents);

    }

    @Override
    public String toString() {
        return "DecisionContextProvider{" +
                "graph=" + (graph == null ? "null" : graph.toString()) +
                ", pathFinder=" + (pathFinder == null ? "null" : pathFinder.toString()) +
                ", cachedContexts=" + (cachedContexts == null ? 0 : cachedContexts.size()) +
                '}';
    }

}
