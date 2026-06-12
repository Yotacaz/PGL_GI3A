package fr.cy.model.pathfinding;

import fr.cy.model.graph.Graph;
import fr.cy.model.graph.element.Node;
import fr.cy.model.graph.element.Edge;

import java.util.*;
import java.io.Serializable;

/**
 * Multi-Agent Path Finding (MAPF) Algorithm implementation.
 * 
 * Computes optimal paths considering:
 * - Congestion levels on edges and nodes (favors less congested routes)
 * - Fire hazards (avoids or heavily penalizes burning areas)
 * - Multi-agent coordination to minimize conflicts
 * 
 * Uses a weighted cost function that combines distance, congestion, and fire risk.
 * 
 * @author GI3A
 * @version 1.0
 */
public class MapfAlgorithm implements Serializable{
    
    private final Graph graph;
    
    // Configuration constants for path weighting
    private static final double FIRE_PENALTY = 1000.0;       // Heavy penalty for fire
    private static final double CONGESTION_FACTOR = 0.5;     // Weight factor for congestion
    private static final double BASE_DISTANCE_FACTOR = 1.0;  // Weight factor for distance
    
    /**
     * Constructs a new MapfAlgorithm for the given graph.
     * 
     * @param graph The graph on which to run the algorithm
     */
    public MapfAlgorithm(Graph graph) {
        this.graph = graph;
    }
    
    /**
     * Finds the optimal path from a start node to any of the exit nodes.
     * 
     * Considers congestion and fire hazards when computing the cost of each path.
     * 
     * @param startNode The starting node
     * @param exitNodes The list of potential exit nodes
     * @return A GraphPath object containing the optimal path (including start and exit),
     *         or null if no path is found
     */
    public GraphPath findOptimalPath(Node startNode, List<Node> exitNodes) {
        if (startNode == null || exitNodes == null || exitNodes.isEmpty()) {
            return null;
        }
        
        // Use modified Dijkstra with congestion and fire penalties
        Map<Node, Double> costs = new HashMap<>();
        Map<Node, Node> predecessors = new HashMap<>();
        PriorityQueue<Node> priorityQueue = new PriorityQueue<>(
            Comparator.comparingDouble(costs::get)
        );
        
        // Initialize all costs to infinity except source
        for (Node node : graph.getNodes()) {
            costs.put(node, Double.MAX_VALUE);
            predecessors.put(node, null);
        }
        costs.put(startNode, 0.0);
        priorityQueue.add(startNode);
        
        Set<Node> visited = new HashSet<>();
        Node bestExit = null;
        
        // Main search loop
        while (!priorityQueue.isEmpty()) {
            Node current = priorityQueue.poll();
            
            if (visited.contains(current)) {
                continue;
            }
            visited.add(current);
            
            // Check if we reached an exit
            if (exitNodes.contains(current)) {
                bestExit = current;
                break;
            }
            
            double currentCost = costs.get(current);
            
            // Process all neighbors
            List<Edge> adjacentEdges = graph.getAdjacentEdges(current);
            if (adjacentEdges != null) {
                for (Edge edge : adjacentEdges) {
                    Node neighbor = edge.getOppositeNode(current);
                    
                    if (neighbor != null && !visited.contains(neighbor)) {
                        // Calculate weighted cost considering congestion and fire
                        double edgeCost = calculateWeightedCost(edge, neighbor);
                        double newCost = currentCost + edgeCost;
                        double oldCost = costs.get(neighbor);
                        
                        // Update if lower cost path found
                        if (newCost < oldCost) {
                            costs.put(neighbor, newCost);
                            predecessors.put(neighbor, current);
                            priorityQueue.add(neighbor);
                        }
                    }
                }
            }
        }
        
        // Reconstruct path if exit was found
        if (bestExit != null) {
            return reconstructGraphPath(startNode, bestExit, predecessors, costs);
        }
        
        return null;
    }
    
    /**
     * Calculates the weighted cost of traversing an edge.
     * 
     * The cost combines:
     * - Base Euclidean distance
     * - Congestion penalty on the edge
     * - Congestion penalty on the destination node
     * - Fire hazard penalty if applicable
     * 
     * @param edge The edge to calculate cost for
     * @param destinationNode The destination node of the edge
     * @return The calculated weighted cost
     */
    private double calculateWeightedCost(Edge edge, Node destinationNode) {
        double baseCost = 0;
        
        // 1. Base distance cost
        baseCost += calculateEuclideanDistance(edge) * BASE_DISTANCE_FACTOR;
        
        // 2. Congestion penalty on edge
        double edgeCongestion = edge.getCongestion();
        baseCost += edgeCongestion * CONGESTION_FACTOR;
        
        // 3. Congestion penalty on destination node
        double nodeCongestion = destinationNode.getCongestion();
        baseCost += nodeCongestion * CONGESTION_FACTOR;
        
        // 4. Fire hazard penalties
        if (edge.isOnFire()) {
            baseCost += FIRE_PENALTY;
        }
        if (destinationNode.isOnFire()) {
            baseCost += FIRE_PENALTY;
        }
        
        return baseCost;
    }
    
    /**
     * Calculates the Euclidean distance between the start and end of an edge.
     * 
     * @param edge The edge to calculate distance for
     * @return The Euclidean distance
     */
    private double calculateEuclideanDistance(Edge edge) {
        Node start = edge.getStart();
        Node end = edge.getEnd();
        
        double dx = end.getX() - start.getX();
        double dy = end.getY() - start.getY();
        return Math.sqrt(dx * dx + dy * dy);
    }
    
    /**
     * Reconstructs the path from start to destination using the predecessors map.
     * Converts the node sequence into a GraphPath with associated edges.
     * 
     * @param startNode The start node
     * @param destinationNode The destination node
     * @param predecessors Map of nodes to their predecessors
     * @param costs Map of nodes to their path costs
     * @return A GraphPath object representing the path, or null if no valid path
     */
    private GraphPath reconstructGraphPath(Node startNode, Node destinationNode, 
            Map<Node, Node> predecessors, Map<Node, Double> costs) {
        List<Node> pathNodes = new ArrayList<>();
        Node current = destinationNode;
        
        // Walk backwards from destination to start
        while (current != null) {
            pathNodes.add(0, current);
            current = predecessors.get(current);
        }
        
        // Verify that the path actually starts from the start node
        if (pathNodes.isEmpty() || !pathNodes.get(0).equals(startNode)) {
            return null; // No valid path found
        }
        
        // Convert node list to edge list
        List<Edge> pathEdges = new ArrayList<>();
        for (int i = 0; i < pathNodes.size() - 1; i++) {
            Node fromNode = pathNodes.get(i);
            Node toNode = pathNodes.get(i + 1);
            Edge connectingEdge = findEdgeBetween(fromNode, toNode);
            if (connectingEdge != null) {
                pathEdges.add(connectingEdge);
            }
        }
        
        // Get the path cost (distance to destination)
        double pathCost = costs.get(destinationNode);
        
        return new GraphPath(pathNodes, pathEdges, pathCost);
    }
    
    /**
     * Finds the edge connecting two nodes.
     * 
     * @param fromNode The first node
     * @param toNode The second node
     * @return The edge connecting the two nodes, or null if not found
     */
    private Edge findEdgeBetween(Node fromNode, Node toNode) {
        List<Edge> adjacentEdges = graph.getAdjacentEdges(fromNode);
        if (adjacentEdges != null) {
            for (Edge edge : adjacentEdges) {
                if ((edge.getStart().equals(fromNode) && edge.getEnd().equals(toNode)) ||
                    (edge.getEnd().equals(fromNode) && edge.getStart().equals(toNode))) {
                    return edge;
                }
            }
        }
        return null;
    }
}
