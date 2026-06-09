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

/**
 * Provides decision contexts for agents based on their current position in the graph.
 * 
 * <p>This class is responsible for creating and caching context objects that contain
 * information about nodes and edges, which agents use to make informed decisions
 * during the simulation.</p>
 */
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

    /**
     * Gets the decision context for a specific node.
     * 
     * <p>This method provides cached context information about a node, including
     * its outgoing edges and other relevant data that agents need to make decisions.</p>
     * 
     * @param node the node for which to get the context
     * @return the node context containing decision-relevant information
     * @throws NullPointerException if node is null
     */
    public NodeContext getNodeContext(Node node) {
        Objects.requireNonNull(node, "node cannot be null when trying to get NodeContext");
        NodeContext cachedContext = cachedNodeContexts.get(node);
        if (cachedContext == null) {
            cachedContext = constructNodeContext(node);
            cachedNodeContexts.put(node, cachedContext);
        }
        return cachedContext;
    }

    /**
     * Gets the decision context for a specific edge.
     * 
     * <p>This method provides cached context information about an edge, including
     * accessible nodes and other relevant data that agents need to make decisions
     * while traversing edges.</p>
     * 
     * @param edge the edge for which to get the context
     * @return the edge context containing decision-relevant information
     * @throws NullPointerException if edge is null
     */
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

    /**
     * Registers the chosen action of an agent in the context of its current node or edge.
     * This should be called after an agent has made a decision and before the action is executed, 
     * @param agent the agent for which to register the chosen action
     * @param action the action chosen by the agent based on its decision
     * @return true if the action was successfully registered in the context, 
     * false if the action could not be registered (e.g., due to invalid action or context)
     */
    public boolean registerChosenAction(Agent agent, AgentAction action) {
        Objects.requireNonNull(agent);
        if (action == null) {
            return false;
        }

        if (agent.isOnNode()) {
            NodeContext context = Objects.requireNonNull(cachedNodeContexts.get(agent.getCurrentNode()));
            return context.registerOutgoingIntent(action.getClosestTargetEdge(), agent);
        } else if (agent.isOnEdge()) {
            EdgeContext context = Objects.requireNonNull(cachedEdgesContexts.get(agent.getCurrentEdge()));
            return context.registerOutgoingIntent(action.getClosestTargetNode(), agent);
        } else {
            throw new AgentStateException(
                    "Agent is not on a node or an edge, cannot register chosen action in context");
        }

        // Edge currentEdge = agent.getCurrentEdge();
        // Node previousNode = agent.getPreviousOrCurrentNode();
        // if (currentEdge == null || previousNode == null) {
        //     return false;
        // }

        // EdgeContext context = cachedEdgesContexts.get(currentEdge);
        // if (context == null) {
        //     return false;
        // }

        // Edge targetEdge = action.getClosestTargetEdge();
        // if (targetEdge == null || !targetEdge.equals(currentEdge)) {
        //     return true;
        // }
        // Node targetNode = currentEdge.getOppositeNode(previousNode);
        // return context.registerOutgoingIntent(targetNode, agent);
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
