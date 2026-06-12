package fr.cy.model.graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.io.Serializable;
import fr.cy.model.agent.Agent;
import fr.cy.model.agent.exceptions.AgentStateException;
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
    /** A random number generator for stochastic operations. */
    private static final Random random = new Random();
    private final List<Node> nodes;
    private final List<Edge> edges;
    private final Map<Node, List<Edge>> adjacencyList;

    private final IdManager nodeIdManager;
    private final IdManager edgeIdManager;

    private boolean isFirstTick = true;

    /**
     * Constructs an empty graph with initialized data structures and ID managers.
     */
    public Graph() {
        this.edges = new ArrayList<>();
        this.nodes = new ArrayList<>();
        this.adjacencyList = new HashMap<>();
        this.nodeIdManager = new IdManager();
        this.edgeIdManager = new IdManager();
    }

    /**
     * Initializes a graph with a specified number of nodes and edges.
     * Nodes are placed randomly, and edges connect random nodes.
     *
     * @param nodeCount The number of nodes to create.
     * @param edgeCount The number of edges to create.
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

    // =========================================================================
    // 3. LIFECYCLE & TICK LOGIC
    // =========================================================================

    /**
     * Resets all nodes and edges in the graph back to their initial state.
     */
    public void reset() {
        for (Edge edge : edges) {
            edge.reset();
        }
        for (Node node : nodes) {
            node.reset();
        }
    }

    /**
     * Performs a simulation tick. Handles initial state snapshots,
     * updates congestion delay metrics, and recomputes environmental stress levels
     * across the graph.
     */
    public void tick() {
        if (isFirstTick) {
            for (Node node : nodes)
                node.setInitialState();
            for (Edge edge : edges)
                edge.setInitialState();
            isFirstTick = false;
        }

        // Update heavy congestion penalty trackers for all active elements
        for (Node node : nodes)
            node.updateCongestionDelays();
        for (Edge edge : edges)
            edge.updateCongestionDelays();

        double tickDuration = SimulationSettings.getInstance().getTickDuration();
        updateStressInducedByElements(tickDuration);
    }

    /**
     * Core routine to cascade stress updates across all infrastructure components.
     * * @param tickDuration Standard tick time delta.
     */
    private void updateStressInducedByElements(double tickDuration) {
        for (Node element : nodes) {
            element.updateStressGeneratedByThisElement(tickDuration);
        }
        for (Edge element : edges) {
            element.updateStressGeneratedByThisElement(tickDuration);
        }
        for (Node element : nodes) {
            element.updateCachedTotalStressInducedIncludingNeighbors();
        }
        for (Edge element : edges) {
            element.updateCachedTotalStressInducedIncludingNeighbors();
        }
    }

    // =========================================================================
    // 4. NODE MANAGEMENT (CREATION / DELETION)
    // =========================================================================

    /**
     * Creates a new node at the specified coordinates with a given capacity.
     *
     * @param x        The x-coordinate.
     * @param y        The y-coordinate.
     * @param capacity The maximum safe physical capacity of the node.
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
     * Adds a pre-constructed node to the active graph structure.
     *
     * @param node The node to add.
     */
    public void addNode(Node node) {
        nodes.add(node);
        adjacencyList.put(node, new ArrayList<>());
    }

    /** Adds a node to the graph at a random coordinate position. */
    public void addNode() {
        double x = Math.random() * 1000;
        double y = Math.random() * 1000;
        createNode(x, y);
    }

    /**
     * Adds multiple nodes to the graph.
     * * @param count The amount of random nodes to inject.
     */
    public void addNodes(int count) {
        for (int i = 0; i < count; i++)
            addNode();
    }

    /**
     * Safely removes a node and all of its associated edges from the graph.
     * <p>
     * Note: Agent emergency relocation is handled externally by AgentManager
     * prior to calling this method.
     * </p>
     *
     * @param nodeToRemove The target node to eliminate.
     */
    public void removeNode(Node nodeToRemove) {
        if (!nodes.contains(nodeToRemove))
            return;

        List<Edge> connectedEdges = new ArrayList<>(nodeToRemove.getEdges());
        // Strip and eliminate all attached edges first
        for (Edge edge : connectedEdges) {
            removeEdge(edge);
        }

        // Clean up the main node tracking lists and release the identity mapping
        adjacencyList.remove(nodeToRemove);
        nodes.remove(nodeToRemove);
        nodeIdManager.releaseId(nodeToRemove.getId());
    }

    /**
     * Get a list of all nodes directly connected to a given node by an edge.
     * 
     * @param node The node to search adjacent nodes from
     * @return A list of all nodes directly connected to the given node by an edge.
     */
    public List<Node> getAdjacentNodes(Node node) {
        List<Node> neighbors = new ArrayList<>();
        List<Edge> connectedEdges = node.getEdges();
        for (Edge edge : connectedEdges) {
            neighbors.add(edge.getOppositeNode(node));
        }
        return neighbors;
    }

    /**
     * @return A random node from the graph, or null if the graph is empty.
     */
    public Node getRandomNode() {
        if (nodes.isEmpty()) {
            return null;
        }
        return nodes.get(random.nextInt(nodes.size()));
    }

    // =========================================================================
    // 5. EDGE MANAGEMENT (CREATION / DELETION)
    // =========================================================================

    /**
     * Creates an undirected edge connecting two structural nodes.
     *
     * @param startNode The origin node.
     * @param endNode   The destination node.
     * @return The newly created connecting edge.
     */
    public Edge createEdge(Node startNode, Node endNode) {
        Edge edge = new Edge(edgeIdManager.generateId(), startNode, endNode);
        addEdge(edge);
        return edge;
    }

    /**
     * Creates an edge segment configured with specific dimensions and routing
     * behaviors.
     *
     * @param startNode Starting point.
     * @param endNode   Ending point.
     * @param length    Physical traversal length.
     * @param width     Physical capacity width.
     * @param directed  Boolean flag establishing one-way traversal restriction.
     * @return The freshly assembled edge instance.
     */
    public Edge createEdge(Node startNode, Node endNode, double length, double width, boolean directed) {
        Edge edge = new Edge(edgeIdManager.generateId(), startNode, endNode, directed, width, length);
        addEdge(edge);
        return edge;
    }

    /**
     * Registers a pre-constructed edge into the graph and updates standard
     * adjacency matrices.
     *
     * @param edge The constructed edge.
     * @throws GraphException If the provided nodes are orphaned or absent from the
     *                        main topology.
     */
    public void addEdge(Edge edge) {
        if (edge == null)
            throw new GraphException("Cannot add a null edge.");
        if (!adjacencyList.containsKey(edge.getStart()) || !adjacencyList.containsKey(edge.getEnd())) {
            throw new GraphException("Both nodes of the edge must belong to the graph.");
        }

        edges.add(edge);

        // Link the edge onto the forward target
        adjacencyList.get(edge.getStart()).add(edge);
        edge.getStart().addEdge(edge);

        // If undirected, map the reverse target link symmetrically
        if (!edge.isDirected()) {
            adjacencyList.get(edge.getEnd()).add(edge);
            edge.getEnd().addEdge(edge);
        }
    }

    /**
     * Randomly spawns an edge link between two existing distinct nodes.
     * * @throws GraphException If fewer than 2 distinct nodes exist.
     */
    public void addEdge() {
        if (nodes.size() < 2)
            throw new GraphException("At least 2 nodes required to add an edge.");

        Node startNode = nodes.get((int) (Math.random() * nodes.size()));
        Node endNode = nodes.get((int) (Math.random() * nodes.size()));

        while (endNode == startNode) {
            endNode = nodes.get((int) (Math.random() * nodes.size()));
        }
        createEdge(startNode, endNode);
    }

    /**
     * Generates a batch of randomized edges.
     * * @param count Total number of edges to randomly deploy.
     */
    public void addEdges(int count) {
        for (int i = 0; i < count; i++)
            addEdge();
    }

    /**
     * Removes an edge and unlinks all topological relationships.
     * <p>
     * Note: Agent evacuation processing occurs in AgentManager prior to deletion.
     * </p>
     *
     * @param edge The specific edge component to dismantle.
     */
    public void removeEdge(Edge edge) {
        if (!edges.contains(edge))
            return;

        edges.remove(edge);

        // Clean up adjacency bindings
        if (adjacencyList.containsKey(edge.getStart())) {
            adjacencyList.get(edge.getStart()).remove(edge);
        }
        if (adjacencyList.containsKey(edge.getEnd())) {
            adjacencyList.get(edge.getEnd()).remove(edge);
        }

        // Clean up node-level tracking to avoid concurrent modification issues
        edge.onRemove();

        // Release the unique identifier back to the pool
        edgeIdManager.releaseId(edge.getId());
    }

    /**
     * Flips the traversal direction parameter of a one-way edge and remaps
     * adjacency matrices.
     * * @param edge The directed edge targeted for reversal.
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

    // =========================================================================
    // 6. QUERIES & TOPOLOGY GETTERS
    // =========================================================================

    /** @return An unmodifiable collection view of all nodes. */
    public List<Node> getNodes() {
        return nodes;
    }

    /** @return An unmodifiable collection view of all edges. */
    public List<Edge> getEdges() {
        return edges;
    }

    /**
     * Searches the graph sequentially to retrieve a node matching a given ID.
     * * @param id The unique integer ID tag.
     * 
     * @return The node instance, or null if no match is found.
     */
    public Node getNodeById(int id) {
        for (Node node : nodes) {
            if (node.getId() == id)
                return node;
        }
        return null;
    }

    /**
     * Fetches all direct neighbors attached to a given node.
     * * @param node The specific node to query.
     * 
     * @return A list containing adjacent nodes.
     */
    public List<Node> getNeighbors(Node node) {
        List<Node> neighbors = new ArrayList<>();
        List<Edge> connectedEdges = adjacencyList.getOrDefault(node, new ArrayList<>());
        for (Edge edge : connectedEdges) {
            neighbors.add(edge.getOppositeNode(node));
        }
        return neighbors;
    }

    /**
     * @return The complete graph topology represented as an adjacency dictionary
     *         map.
     */
    public Map<Node, List<Edge>> getAdjacencyList() {
        return adjacencyList;
    }

    /**
     * Retrieves all edges that are actively connected to a specific node.
     * * @param node The node to target.
     * 
     * @return A list containing all attached edges.
     */
    public List<Edge> getAdjacentEdges(Node node) {
        return adjacencyList.getOrDefault(node, new ArrayList<>());
    }

    /**
     * Retrieves a specialized subset of nodes classified as successful evacuation
     * points.
     * * @return A list of all marked exit nodes on the graph.
     */
    public List<Node> getExits() {
        List<Node> exits = new ArrayList<>();
        for (Node node : nodes) {
            if (node.isExit())
                exits.add(node);
        }
        return exits;
    }
}