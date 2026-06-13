package fr.cy.model.pathfinding;

import fr.cy.model.graph.element.Node;
import fr.cy.model.graph.element.Edge;
import java.util.List;
import java.util.ArrayList;
import java.io.Serializable;

/**
 * Represents a path through the graph from a start node to a destination.
 * 
 * Encapsulates both the sequence of nodes to traverse and the edges connecting them.
 * This allows agents to follow the path step by step, knowing both their destination
 * and the route to take.
 * 
 * @author GI3A
 * @version 1.0
 */
public class GraphPath implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /** List of nodes forming the path (from start to destination) */
    private final List<Node> nodes;
    
    /** List of edges connecting consecutive nodes in the path */
    private final List<Edge> edges;
    
    /** Total cost/length of the path */
    private double pathCost;
    
    /**
     * Constructs a new GraphPath with empty node and edge lists.
     */
    public GraphPath() {
        this.nodes = new ArrayList<>();
        this.edges = new ArrayList<>();
        this.pathCost = 0.0;
    }
    
    /**
     * Constructs a new GraphPath with given nodes and edges.
     * 
     * @param nodes The sequence of nodes forming the path
     * @param edges The sequence of edges connecting consecutive nodes
     */
    public GraphPath(List<Node> nodes, List<Edge> edges) {
        this.nodes = new ArrayList<>(nodes != null ? nodes : new ArrayList<>());
        this.edges = new ArrayList<>(edges != null ? edges : new ArrayList<>());
        this.pathCost = 0.0;
    }
    
    /**
     * Constructs a new GraphPath with given nodes, edges and cost.
     * 
     * @param nodes The sequence of nodes forming the path
     * @param edges The sequence of edges connecting consecutive nodes
     * @param pathCost The total cost/length of the path
     */
    public GraphPath(List<Node> nodes, List<Edge> edges, double pathCost) {
        this.nodes = new ArrayList<>(nodes != null ? nodes : new ArrayList<>());
        this.edges = new ArrayList<>(edges != null ? edges : new ArrayList<>());
        this.pathCost = pathCost;
    }
    
    /**
     * Gets all nodes in the path.
     * 
     * @return An immutable view of the node list
     */
    public List<Node> getNodes() {
        return new ArrayList<>(nodes);
    }
    
    /**
     * Gets all edges in the path.
     * 
     * @return An immutable view of the edge list
     */
    public List<Edge> getEdges() {
        return new ArrayList<>(edges);
    }
    
    /**
     * Gets the total cost or length of the path.
     * 
     * @return The path cost
     */
    public double getPathCost() {
        return pathCost;
    }
    
    /**
     * Sets the total cost of the path.
     * 
     * @param cost The cost to set
     */
    public void setPathCost(double cost) {
        this.pathCost = cost;
    }
    
    /**
     * Gets the starting node of the path.
     * 
     * @return The first node in the path, or null if path is empty
     */
    public Node getStartNode() {
        return nodes.isEmpty() ? null : nodes.get(0);
    }
    
    public Edge getStartEdge() {
        return edges.isEmpty() ? null : edges.get(0);
    }

    /**
     * Gets the destination (end) node of the path.
     * 
     * @return The last node in the path, or null if path is empty
     */
    public Node getDestinationNode() {
        return nodes.isEmpty() ? null : nodes.get(nodes.size() - 1);
    }
    
    /**
     * Gets the number of nodes in the path.
     * 
     * @return The count of nodes
     */
    public int getNodeCount() {
        return nodes.size();
    }
    
    /**
     * Gets the number of edges in the path.
     * 
     * @return The count of edges
     */
    public int getEdgeCount() {
        return edges.size();
    }
    
    /**
     * Checks if the path is empty.
     * 
     * @return true if the path contains no nodes, false otherwise
     */
    public boolean isEmpty() {
        return nodes.isEmpty();
    }
    
    /**
     * Gets the next node after the given node in the path.
     * 
     * @param currentNode The current node
     * @return The next node in the path, or null if currentNode is the destination or not in the path
     */
    public Node getNextNode(Node currentNode) {
        for (int i = 0; i < nodes.size() - 1; i++) {
            if (nodes.get(i).equals(currentNode)) {
                return nodes.get(i + 1);
            }
        }
        return null;
    }
    
    /**
     * Gets the edge connecting two consecutive nodes at the given index.
     * 
     * @param index The index of the edge (0 = edge between first and second node)
     * @return The edge at the given index, or null if index is out of bounds
     */
    public Edge getEdgeAt(int index) {
        if (index < 0 || index >= edges.size()) {
            return null;
        }
        return edges.get(index);
    }
    
    /**
     * Adds a node to the end of the path.
     * 
     * @param node The node to add
     */
    public void addNode(Node node) {
        if (node != null) {
            nodes.add(node);
        }
    }
    
    /**
     * Adds an edge to the end of the edge list.
     * 
     * @param edge The edge to add
     */
    public void addEdge(Edge edge) {
        if (edge != null) {
            edges.add(edge);
        }
    }
    
    /**
     * Calculates and returns the total length of the path based on edge distances.
     * 
     * @return The sum of all edge Euclidean distances
     */
    public double calculatePathLength() {
        double length = 0.0;
        for (Edge edge : edges) {
            double dx = edge.getEnd().getX() - edge.getStart().getX();
            double dy = edge.getEnd().getY() - edge.getStart().getY();
            length += Math.sqrt(dx * dx + dy * dy);
        }
        return length;
    }

    public Node getNodeAt (int i){
        return nodes.get(i);
    }
         
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < nodes.size(); i++) {
            sb.append(nodes.get(i).getId());
            if (i < nodes.size() - 1) {
                sb.append(" -> ");
            }
        }
        return sb.toString();
    }
}
