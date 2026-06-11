package fr.cy.model.pathfinding;

import fr.cy.model.graph.element.Node;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

/**
 * Container for the results of Dijkstra's algorithm execution.
 * 
 * Stores the computed distances and predecessors for path reconstruction.
 * 
 * @author GI3A
 * @version 1.0
 */
public class DijkstraResult {
    
    private final Map<Node, Double> distances;
    private final Map<Node, Node> predecessors;
    private final Node sourceNode;
    
    /**
     * Constructs a new DijkstraResult.
     * 
     * @param distances Map of nodes to their shortest distances from the source
     * @param predecessors Map of nodes to their predecessors in the shortest path tree
     * @param sourceNode The source node used in the computation
     */
    public DijkstraResult(Map<Node, Double> distances, Map<Node, Node> predecessors, Node sourceNode) {
        this.distances = distances;
        this.predecessors = predecessors;
        this.sourceNode = sourceNode;
    }
    
    /**
     * Gets the shortest distance from the source to a given node.
     * 
     * @param node The target node
     * @return The shortest distance, or Double.MAX_VALUE if unreachable
     */
    public double getDistance(Node node) {
        return distances.getOrDefault(node, Double.MAX_VALUE);
    }
    
    /**
     * Gets the predecessor of a node in the shortest path from the source.
     * 
     * @param node The node to get the predecessor for
     * @return The predecessor node, or null if not found
     */
    public Node getPredecessor(Node node) {
        return predecessors.get(node);
    }
    
    /**
     * Reconstructs the shortest path from the source to a given destination node.
     * 
     * @param destinationNode The destination node
     * @return A list of nodes representing the path from source to destination,
     *         or an empty list if no path exists
     */
    public List<Node> reconstructPath(Node destinationNode) {
        List<Node> path = new ArrayList<>();
        Node current = destinationNode;
        
        // Walk backwards from destination to source using predecessors
        while (current != null) {
            path.add(0, current);
            current = predecessors.get(current);
        }
        
        // Verify that the path actually starts from the source
        if (!path.isEmpty() && path.get(0).equals(sourceNode)) {
            return path;
        }
        
        return new ArrayList<>(); // No valid path found
    }
    
    /**
     * Gets the source node used in the computation.
     * 
     * @return The source node
     */
    public Node getSourceNode() {
        return sourceNode;
    }
    
    /**
     * Gets all computed distances.
     * 
     * @return Map of nodes to their distances
     */
    public Map<Node, Double> getAllDistances() {
        return distances;
    }
}
