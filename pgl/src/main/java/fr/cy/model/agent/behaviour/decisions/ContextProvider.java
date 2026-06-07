package fr.cy.model.agent.behaviour.decisions;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import fr.cy.model.agent.Agent;
import fr.cy.model.agent.behaviour.agentActions.AgentAction;
import fr.cy.model.agent.exceptions.AgentStateException;
import fr.cy.model.graph.Graph;
import fr.cy.model.graph.element.Node;
import fr.cy.model.graph.element.Edge;
import fr.cy.model.pathfinding.PathFinder;

public class ContextProvider implements Serializable {
    private static final long serialVersionUID = 1L;
    private final Graph graph;
    private final PathFinder pathFinder;
    private Map<Node, NodeContext> cachedNodeContexts = new HashMap<>();
    private Map<Edge, EdgeContext> cachedEdgesContexts = new HashMap<>();

    // private final
    public ContextProvider(Graph graph, PathFinder pathFinder) {
        this.graph = graph;
        this.pathFinder = pathFinder;
    }

    public NodeContext getNodeContext(Node node) {
        Objects.requireNonNull(node, "node cannot be null when trying to get NodeContext");
        NodeContext cachedContext = cachedNodeContexts.get(node);
        if (cachedContext == null) {
            cachedContext = constructNodeContext(node);
            cachedNodeContexts.put(node, cachedContext);
        }
        return cachedContext;
    }

    public EdgeContext getEdgeContext(Edge edge) {
        Objects.requireNonNull(edge, "edge cannot be null when trying to get NodeContext");
        EdgeContext cachedContext = cachedEdgesContexts.get(edge);
        if (cachedContext == null) {
            cachedContext = constructEdgeContext(edge);
            cachedEdgesContexts.put(edge, cachedContext);
        }
        return cachedContext;
    }

    public void clearCache() {
        cachedNodeContexts.clear();
        cachedEdgesContexts.clear();
    }

    public boolean registerChosenAction(Agent agent, AgentAction action) {
        Objects.requireNonNull(agent);
        if (action == null) {
            return false;
        }
        Node currentNode = agent.getCurrentNode();
        if (currentNode != null) {
            NodeContext context = Objects.requireNonNull(cachedNodeContexts.get(currentNode));
            return context.registerOutgoingIntent(action.getClosestTargetEdge(), agent);
        }

        Edge currentEdge = agent.getCurrentEdge();
        Node previousNode = agent.getPreviousOrCurrentNode();
        if (currentEdge == null || previousNode == null) {
            return false;
        }

        EdgeContext context = cachedEdgesContexts.get(currentEdge);
        if (context == null) {
            return false;
        }

        Edge targetEdge = action.getClosestTargetEdge();
        if (targetEdge == null || !targetEdge.equals(currentEdge)) {
            return true;
        }
        Node targetNode = currentEdge.getOppositeNode(previousNode);
        return context.registerOutgoingIntent(targetNode, agent);
    }

    private NodeContext constructNodeContext(Node node) {
        // List<Node> recommendedPath = pathFinder.findPath(agent.getCurrentNode(),
        // agent.getDestinationNode());
        Node currentNode = Objects.requireNonNull(node,
                "Node must be valid to construct decision context");
        List<Edge> allEdges = currentNode.getEdges();
        Map<Edge, List<Agent>> nearbyIncomingAgents = new HashMap<>();
        Map<Edge, List<Agent>> nearbyOutgoingAgents = new HashMap<>();
        Map<Edge, Double> spaceOccupiedAtEdgesEntrance = new HashMap<>();
        for (Edge edge : allEdges) {
            double spaceOccupiedAtEntrance = 0.0;
            Node oppositeNode = edge.getOppositeNode(currentNode);
            for (Agent nearbyAgent : edge.getAgents()) {
                // Determine if the nearby agent is incoming or outgoing relative to the current
                // node
                double progress = nearbyAgent.getCurrentEdgeProgress();
                double distToNode = -1.0;
                if (progress < 0) {
                    throw new AgentStateException(
                            "Agent travel progress on edge cannot be <0 if is on an edge (got+" + progress + ")");
                }
                if (oppositeNode.equals(nearbyAgent.getPreviousOrCurrentNode())) { //coming from opposite side
                    nearbyIncomingAgents.computeIfAbsent(edge, k -> new ArrayList<>()).add(nearbyAgent);
                    distToNode = (1 - progress) * edge.getLength();
                } else { //coming from this side
                    nearbyOutgoingAgents.computeIfAbsent(edge, k -> new ArrayList<>()).add(nearbyAgent);
                    distToNode = progress * edge.getLength();
                }
                //add to space occuppied at entrance if close enougth
                double agentWidth = nearbyAgent.getSurfaceAreaTakenByAgent();
                if (distToNode <= agentWidth) {
                    spaceOccupiedAtEntrance += agentWidth;
                }

            }
            spaceOccupiedAtEdgesEntrance.put(edge, spaceOccupiedAtEntrance);
        }
        // account for agents that are currently on the node and planning to take an
        // outgoing edge
        for (Agent nearbyAgent : currentNode.getAgents()) {
            Edge targetOutgoinEdge = nearbyAgent.getCurrentEdgeOrNextEdgeIfOnNode();
            if (targetOutgoinEdge != null) {
                nearbyOutgoingAgents.computeIfAbsent(targetOutgoinEdge, k -> new ArrayList<>()).add(nearbyAgent);
            }
        }

        List<Edge> outgoingEdges = currentNode.getOutgoingEdges();
        return new NodeContext(currentNode, null, null, outgoingEdges, nearbyIncomingAgents,
                nearbyOutgoingAgents, spaceOccupiedAtEdgesEntrance);
    }

    private EdgeContext constructEdgeContext(Edge edge) {
        // List<Node> recommendedPath = pathFinder.findPath(agent.getCurrentNode(),
        // agent.getDestinationNode());
        Edge currentEdge = Objects.requireNonNull(edge,
                "Node must be valid to construct decision context");
        List<Node> accessiblesNodes = new ArrayList<>();
        accessiblesNodes.add(currentEdge.getStart());
        if (!currentEdge.isDirected()) {
            accessiblesNodes.add(currentEdge.getEnd());
        }

        return new EdgeContext(currentEdge, accessiblesNodes);
    }

    @Override
    public String toString() {
        return "DecisionContextProvider{" +
                "graph=" + (graph == null ? "null" : graph.toString()) +
                ", pathFinder=" + (pathFinder == null ? "null" : pathFinder.toString()) +
                ", cachedNodesContexts=" + (cachedNodeContexts == null ? 0 : cachedNodeContexts.size()) +
                ", cachedEdgesContexts=" + (cachedEdgesContexts == null ? 0 : cachedEdgesContexts.size()) +
                '}';
    }

}
