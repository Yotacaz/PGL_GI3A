package fr.cy.model.graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.Serializable;
import fr.cy.model.agent.Agent;
import fr.cy.model.graph.element.Edge;
import fr.cy.model.graph.element.Node;
import fr.cy.model.simulation.SimulationSettings;
import fr.cy.util.IdManager;

/**
 * Represents a graph structure composed of nodes and edges, serving as the
 * core environment for the simulation.
 *
 * @author GI3A
 * @version 1.0
 */
public class Graph implements Serializable {
    private static final long serialVersionUID = 1L;

    private final List<Node> nodes;
    private final List<Edge> edges;
    private final Map<Node, List<Edge>> adjacencyList;

    private final IdManager nodeIdManager;
    private final IdManager edgeIdManager;

    private boolean isFirstTick = true;

    /**
     * Initializes a graph with a specified number of nodes and edges.
     * Nodes are placed randomly, and edges connect random nodes.
     *
     * @param nodeCount The number of nodes to create.
     * @param edgeCount The number of edges to create.
     */
    public Graph(int nodeCount, int edgeCount) {
        this();
        for (int i = 0; i < nodeCount; i++)
            addNode();
        for (int i = 0; i < edgeCount; i++)
            addEdge();
    }

    /**
     * Constructs an empty graph with initialized managers and lists.
     */
    public Graph() {
        this.edges = new ArrayList<>();
        this.nodes = new ArrayList<>();
        this.adjacencyList = new HashMap<>();
        this.nodeIdManager = new IdManager();
        this.edgeIdManager = new IdManager();
    }

    /**
     * Resets all nodes and edges in the graph to their initial state.
     */
    public void reset() {
        for (Edge edge : edges)
            edge.reset();
        for (Node node : nodes)
            node.reset();
    }

    /**
     * Performs a simulation tick. Handles initial state snapshots,
     * updates congestion values, and recomputes stress levels across the graph.
     */
    public void tick() {
        if (isFirstTick) {
            for (Node node : nodes)
                node.setInitialState();
            for (Edge edge : edges)
                edge.setInitialState();
            isFirstTick = false;
        }
        double tickDuration = SimulationSettings.getInstance().getTickDuration();
        for (Node node : nodes)
            node.updateForcedCongestion();
        for (Edge edge : edges)
            edge.updateForcedCongestion();

        updateStressInducedByElements(tickDuration);
    }

    /**
     * Creates a new node at the specified coordinates with a given capacity.
     *
     * @param x        The x-coordinate.
     * @param y        The y-coordinate.
     * @param capacity The maximum capacity of the node.
     * @return The newly created node.
     */
    public Node createNode(double x, double y, double capacity) {
        Node node = new Node(nodeIdManager.generateId(), x, y, capacity);
        addNode(node);
        return node;
    }

    /**
     * Creates a new node at the specified coordinates with default capacity.
     *
     * @param x The x-coordinate.
     * @param y The y-coordinate.
     * @return The newly created node.
     */
    public Node createNode(double x, double y) {
        return createNode(x, y, GraphConfig.DEFAULT_NODE_CAPACITY);
    }

    /**
     * Creates an undirected edge between two nodes.
     *
     * @param startNode The starting node.
     * @param endNode   The ending node.
     * @return The newly created edge.
     */
    public Edge createEdge(Node startNode, Node endNode) {
        Edge edge = new Edge(edgeIdManager.generateId(), startNode, endNode);
        addEdge(edge);
        return edge;
    }

    /**
     * Creates an edge with specific properties.
     *
     * @param startNode Starting node.
     * @param endNode   Ending node.
     * @param length    Edge length.
     * @param width     Edge width.
     * @param directed  Whether the edge is directed.
     * @return The newly created edge.
     */
    public Edge createEdge(Node startNode, Node endNode, double length, double width, boolean directed) {
        Edge edge = new Edge(edgeIdManager.generateId(), startNode, endNode, directed, width, length);
        addEdge(edge);
        return edge;
    }

    /**
     * Adds a pre-constructed node to the graph.
     *
     * @param node The node to add.
     */
    public void addNode(Node node) {
        nodes.add(node);
        adjacencyList.put(node, new ArrayList<>());
    }

    /**
     * Adds a node to the graph at a random position.
     */
    public void addNode() {
        double x = Math.random() * 1000;
        double y = Math.random() * 1000;
        createNode(x, y);
    }

    /**
     * Adds multiple nodes to the graph.
     * 
     * @param count The number of nodes to add.
     */
    public void addNodes(int count) {
        for (int i = 0; i < count; i++)
            addNode();
    }

    /**
     * Adds a pre-constructed edge to the graph and updates adjacency lists.
     *
     * @param edge The edge to add.
     * @throws GraphException If the nodes are not present in the graph.
     */
    public void addEdge(Edge edge) {
        if (edge == null)
            throw new GraphException("Cannot add a null edge.");
        if (!adjacencyList.containsKey(edge.getStart()) || !adjacencyList.containsKey(edge.getEnd())) {
            throw new GraphException("Both nodes of the edge must belong to the graph.");
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
     * Adds a random edge between existing nodes.
     * 
     * @throws GraphException If fewer than 2 nodes exist.
     */
    public void addEdge() {
        if (nodes.size() < 2)
            throw new GraphException("At least 2 nodes required to add an edge.");
        Node startNode = nodes.get((int) (Math.random() * nodes.size()));
        Node endNode = nodes.get((int) (Math.random() * nodes.size()));
        while (endNode == startNode)
            endNode = nodes.get((int) (Math.random() * nodes.size()));
        createEdge(startNode, endNode);
    }

    /**
     * Adds multiple random edges.
     * 
     * @param count The number of edges to add.
     */
    public void addEdges(int count) {
        for (int i = 0; i < count; i++)
            addEdge();
    }

    /**
     * Removes a node and all associated edges, migrating agents to neighbor nodes.
     *
     * @param nodeToRemove The node to remove.
     */
    public void removeNode(Node nodeToRemove) {
        List<Edge> connectedEdges = new ArrayList<>(nodeToRemove.getEdges());
        for (Edge edge : connectedEdges)
            removeEdge(edge);

        List<Node> neighbors = getNeighbors(nodeToRemove);
        if (!neighbors.isEmpty()) {
            Node target = neighbors.get(0);
            for (Agent agent : new ArrayList<>(nodeToRemove.getAgents())) {
                agent.putOnNode(target);
                target.setForcedCongestion(true);
            }
        }
        nodes.remove(nodeToRemove);
    }

    /**
     * Removes an edge and migrates agents to the starting node.
     *
     * @param edge The edge to remove.
     */
    public void removeEdge(Edge edge) {
        edges.remove(edge);
        if (adjacencyList.containsKey(edge.getStart()))
            adjacencyList.get(edge.getStart()).remove(edge);
        if (adjacencyList.containsKey(edge.getEnd()))
            adjacencyList.get(edge.getEnd()).remove(edge);

        edge.getStart().removeEdge(edge);
        edge.getEnd().removeEdge(edge);


        // Handle agents on the removed edge
        for (Agent agent : new ArrayList<>(edge.getAgents())) {
            agent.putOnNode(edge.getStart());
        }
        edgeIdManager.releaseId(edge.getId());
    }

    /**
     * Switches the direction of a directed edge.
     * 
     * @param edge The directed edge to reverse.
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

    /** @return The list of all nodes. */
    public List<Node> getNodes() {
        return nodes;
    }

    /** @return The list of all edges. */
    public List<Edge> getEdges() {
        return edges;
    }

    /**
     * Finds a node by ID.
     * 
     * @param id The ID to search for.
     * @return The node, or null if not found.
     */
    public Node getNodeById(int id) {
        for (Node node : nodes)
            if (node.getId() == id)
                return node;
        return null;
    }

    /**
     * Retrieves neighbors of a specific node.
     * 
     * @param node The node to query.
     * @return A list of neighbor nodes.
     */
    public List<Node> getNeighbors(Node node) {
        List<Node> neighbors = new ArrayList<>();
        List<Edge> connectedEdges = adjacencyList.get(node);
        for (Edge edge : connectedEdges)
            neighbors.add(edge.getOppositeNode(node));
        return neighbors;
    }

    /** @return The adjacency map. */
    public Map<Node, List<Edge>> getAdjacencyList() {
        return adjacencyList;
    }

    /**
     * * @param node The node to query.
     * 
     * @return Edges connected to the node.
     */
    public List<Edge> getAdjacentEdges(Node node) {
        return adjacencyList.getOrDefault(node, new ArrayList<>());
    }

    private void updateStressInducedByElements(double tickDuration) {
        for (Node element : nodes)
            element.updateStressGeneratedByThisElement(tickDuration);
        for (Edge element : edges)
            element.updateStressGeneratedByThisElement(tickDuration);
        for (Node element : nodes)
            element.updateCachedTotalStressInducedIncludingNeighbors();
        for (Edge element : edges)
            element.updateCachedTotalStressInducedIncludingNeighbors();
    }

    /** @return A list of all exit nodes. */
    public List<Node> getExits() {
        List<Node> exits = new ArrayList<>();
        for (Node node : nodes)
            if (node.isExit())
                exits.add(node);
        return exits;
    }
}