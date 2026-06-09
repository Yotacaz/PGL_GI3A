package fr.cy.model.graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.Serializable;

import java.io.*;

import fr.cy.model.agent.Agent;
import fr.cy.model.graph.element.Edge;
import fr.cy.model.graph.element.Node;
import fr.cy.util.IdManager;


/**
 * Represents a graph composed of nodes and edges.
 *
 * @author GI3A
 * @version 1.0
 */
public class Graph implements Serializable {
    private static final long serialVersionUID = 1L;

    /** List of all nodes and edges in the graph */
    private final List<Node> nodes;
    private final List<Edge> edges;

    /** Adjacency list */
    private final Map<Node, List<Edge>> adjacencyList;

    private final IdManager nodeIdManager;
    private final IdManager edgeIdManager;

    private boolean isFirstTick = true;

    /**
     * Creates a graph with a specified number of nodes and edges, using random positions
     * for nodes and random connections for edges.
     */
    public Graph(int nodeCount, int edgeCount) {
        this();

        for (int i = 0; i < nodeCount; i++) {
            addNode();
        }

        for (int i = 0; i < edgeCount; i++) {
            addEdge();
        }
    }

    /**
     * Creates an empty graph.
     */
    public Graph() {
        this.edges = new ArrayList<>();
        this.nodes = new ArrayList<>();

        this.adjacencyList = new HashMap<>();

        this.nodeIdManager = new IdManager();
        this.edgeIdManager = new IdManager();
    }

    public void reset() {
        for (Edge edge : edges) {
            edge.reset();
        }
        for (Node node : nodes) {
            node.reset();
        }
    }

    /**
     * Give an update to each elements that need it (eg: cached values like stress)
     */
    public void tick() {
        /** Snapshot auto */
        if (isFirstTick) {
            for (Node node : nodes) {
                node.setInitialState();
            }
            for (Edge edge : edges) {
                edge.setInitialState();
            }
            isFirstTick = false;
        }

        for (Node node : nodes) {
            node.updateForcedCongestion();
        }
        for (Edge edge : edges) {
            edge.updateForcedCongestion();
        }

        updateStressInducedByElements();
    }

    /**
     * Creates a node at a specific position in the graph.
     * 
     * @param x        x coordinate
     * @param y        y coordinate
     * @param capacity maximum capacity
     * @return the created node
     */
    public Node createNode(double x, double y, double capacity) {
        Node node = new Node(nodeIdManager.generateId(), x, y, capacity);
        addNode(node);

        return node;
    }

    /**
     * Creates a node at a specific position in the graph.
     * 
     * @param x x coordinate
     * @param y y coordinate
     * @return the created node
     */
    public Node createNode(double x, double y) {
        return createNode(x, y, GraphConfig.DEFAULT_NODE_CAPACITY);
    }

    /**
     * Creates an edge between two specified nodes.
     * 
     * @param startNode starting node
     * @param endNode   ending node
     * @return the created edge
     */
    public Edge createEdge(Node startNode, Node endNode) {
        Edge edge = new Edge(edgeIdManager.generateId(), startNode, endNode);
        addEdge(edge);

        return edge;
    }

    /**
     * Creates an edge specified by two nodes, width, length, and whether it is directed.
     * 
     * @param startNode
     * @param endNode
     * @param length
     * @param width
     * @param directed
     * @return the created edge
     */
    public Edge createEdge(Node startNode, Node endNode, double length, double width, boolean directed) {
        Edge edge = new Edge(edgeIdManager.generateId(), startNode, endNode, directed, width, length);

        addEdge(edge);
        return edge;
    }

    /**
     * Adds a node to the graph.
     *
     * @param node the node to add
     */
    public void addNode(Node node) {
        nodes.add(node);
        adjacencyList.put(node, new ArrayList<>());
    }

    /**
     * Adds a node to the graph at a random position.
     * 
     */
    public void addNode() {
        double x = Math.random() * 1000; // Exemple de position aléatoire
        double y = Math.random() * 1000;
        createNode(x, y);
    }

    public void addNodes(int count) {
        for (int i = 0; i < count; i++) {
            addNode();
        }
    }

    /**
     * Adds an edge to the graph.
     *
     * @param edge the edge to add
     */
    public void addEdge(Edge edge) {
        if (edge == null) {
            throw new GraphException("Impossible d'ajouter une arête nulle.");
        }

        if (!adjacencyList.containsKey(edge.getStart()) || !adjacencyList.containsKey(edge.getEnd())) {
            throw new GraphException("Les deux nœuds de l'arête doivent appartenir au graphe.");
        }

        edges.add(edge);
        adjacencyList.get(edge.getStart()).add(edge);
        edge.getStart().addEdge(edge);

        if (!edge.isDirected()) {
            adjacencyList.get(edge.getEnd()).add(edge);
            edge.getEnd().addEdge(edge);
        }
    }


    /**
     * Adds a node at random.
     *
     */
    public void addEdge() {
        if (nodes.size() < 2) {
            throw new GraphException("Il doit y avoir au moins 2 nœuds pour ajouter une arête.");
        }

        Node startNode = nodes.get((int) (Math.random() * nodes.size()));
        Node endNode = nodes.get((int) (Math.random() * nodes.size()));

        while (endNode == startNode) {
            endNode = nodes.get((int) (Math.random() * nodes.size()));
        }

        createEdge(startNode, endNode);
    }


    public void addEdges(int count) {
        for (int i = 0; i < count; i++) {
            addEdge();
        }
    }
    /**
     * Removes a node and all edges connected to it.
     *
     * @param node the node to remove
     */
    public void removeNode(Node nodeToRemove) {
        // 1. Handle agents on connected edges before removing the edge
        List<Edge> connectedEdges = new ArrayList<>(nodeToRemove.getEdges());
        for (Edge edge : connectedEdges) {
            removeEdge(edge);
        }

        // 2. Handle agents on the removed node
        List<Node> neighbors = getNeighbors(nodeToRemove); // List of adjacent nodes
        if (!neighbors.isEmpty()) {
            Node target = neighbors.get(0);
            for (Agent agent : new ArrayList<>(nodeToRemove.getAgents())) {
                // Move to the neighbor
                agent.putOnNode(target);

                // If you have properly added the congestion flag in your Node class:
                target.setForcedCongestion(true);
            }
        }

        // 3. Final removal
        nodes.remove(nodeToRemove);
    }
    /**
     * Removes an edge from the graph.
     *
     * @param edge the edge to remove
     */
    public void removeEdge(Edge edge) {
        edges.remove(edge);

        if (adjacencyList.containsKey(edge.getStart())) {
            adjacencyList.get(edge.getStart()).remove(edge);
        }
        if (adjacencyList.containsKey(edge.getEnd())) {
            adjacencyList.get(edge.getEnd()).remove(edge);
        }

        edge.getStart().removeEdge(edge);
        edge.getEnd().removeEdge(edge);

        for (Agent agent : new ArrayList<>(edge.getAgents())) {
            Node startNode = edge.getStart();
            agent.putOnNode(startNode);
        }

        edgeIdManager.releaseId(edge.getId());
    }

    /**
     * Switches the direction of a directed edge. If the edge is undirected, this method has no effect.
     * @param edge
     */
    public void switchEdgeDirection(Edge edge) {
        if (edge.isDirected()) {
            
            edge.getStart().removeEdge(edge);
            adjacencyList.get(edge.getStart()).remove(edge);

            edge.switchDirection();

            edge.getEnd().addEdge(edge);
            adjacencyList.get(edge.getEnd()).add(edge);
            


        }
    }

    /**
     * @return the list of graph nodes
     */
    public List<Node> getNodes() {
        return nodes;
    }

    /**
     * @return the list of graph edges
     */
    public List<Edge> getEdges() {
        return edges;
    }

    /**
     * Searches for a node by its identifier.
     *
     * @param id requested identifier
     * @return the node if found, otherwise {@code null}
     */
    public Node getNodeById(int id) {
        for (Node node : nodes) {
            if (node.getId() == id) {
                return node;
            }
        }

        return null;
    }

    /**
     * Returns the list of neighbors of a node.
     *
     * @param node node whose neighbors are requested
     * @return list of neighboring nodes
     */
    public List<Node> getNeighbors(Node node) {
        List<Node> neighbors = new ArrayList<>();

        List<Edge> connectedEdges = adjacencyList.get(node);

        for (Edge edge : connectedEdges) {
            neighbors.add(edge.getOppositeNode(node));
        }

        return neighbors;
    }

    /**
     * Retourne la liste d'adjacence (peut être vide si non initialisée).
     *
     * @return la map d'adjacence
     */
    public Map<Node, List<Edge>> getAdjacencyList() {
        return adjacencyList;
    }

    public List<Edge> getAdjacentEdges(Node node) {
        return adjacencyList.getOrDefault(node, new ArrayList<>());
    }

    /**
     * Update the stress induced by each element and cache it. This method should be
     * called at each tick
     */
    private void updateStressInducedByElements() {
        // update stress generated by each element and cache it, then update total
        // stress induced including neighbors
        for (Node element : nodes) {
            element.updateStressGeneratedByThisElement();
        }
        for (Edge element : edges) {
            element.updateStressGeneratedByThisElement();
        }
        for (Node element : nodes) {
            element.updateCachedTotalStressInducedIncludingNeighbors();
        }
        for (Edge element : edges) {
            element.updateCachedTotalStressInducedIncludingNeighbors();
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Graph{\n");

        sb.append("  Nodes:\n");
        for (Node node : nodes) {
            sb.append("    ").append(node).append("\n");
        }

        sb.append("  Edges:\n");
        for (Edge edge : edges) {
            sb.append("    ").append(edge).append("\n");
        }

        sb.append("}");
        return sb.toString();
    }

    public List<Node> getExits() {
        List<Node> exits = new ArrayList<>();

        for (Node node : nodes) {
            if (node.isExit()) {
                exits.add(node);
            }
        }

        return exits;
    }

    public static void main(String[] args) {
        Graph graph = new Graph();
        Node n1 = graph.createNode(5, 5);
        Node n2 = graph.createNode(15, 5);
        Node n3 = graph.createNode(15, 10);
        Node n4 = graph.createNode(20, 5);
        n3.setExit(true);
        graph.createEdge(n1, n2);
        graph.createEdge(n2, n3);
        graph.createEdge(n2, n4);

    }
}
