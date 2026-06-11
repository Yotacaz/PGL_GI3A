package fr.cy.model.pathfinding;

import fr.cy.model.graph.Graph;
import fr.cy.model.graph.element.Node;
import fr.cy.model.graph.element.Edge;

import java.util.*;

/**
 * Implementation of Dijkstra's shortest path algorithm.
 * 
 * Used to find the shortest distances from a source node to all other reachable nodes
 * in the graph. This is the basis for finding the nearest exit.
 * 
 * The algorithm uses Euclidean distance as the weight metric between nodes.
 * 
 * @author GI3A
 * @version 1.0
 */
public class DijkstraAlgorithm {
    
    private final Graph graph;
    
    /**
     * Constructs a new DijkstraAlgorithm for the given graph.
     * 
     * @param graph The graph on which to run the algorithm
     */
    public DijkstraAlgorithm(Graph graph) {
        this.graph = graph;
    }
    
    /**
     * Computes the shortest paths from a source node to all reachable nodes.
     * 
     * @param sourceNode The node from which to compute shortest paths
     * @return A DijkstraResult containing distances and predecessors, or null if sourceNode is invalid
     */
    public DijkstraResult computeShortestPaths(Node sourceNode) {
        if (sourceNode == null || !graph.getNodes().contains(sourceNode)) {
            return null;
        }
        
        // Initialize distances and predecessors
        Map<Node, Double> distances = new HashMap<>();
        Map<Node, Node> predecessors = new HashMap<>();
        PriorityQueue<Node> priorityQueue = new PriorityQueue<>(
            Comparator.comparingDouble(distances::get)
        );
        
        // Initialize all distances to infinity except source
        for (Node node : graph.getNodes()) {
            distances.put(node, Double.MAX_VALUE);
            predecessors.put(node, null);
        }
        distances.put(sourceNode, 0.0);
        
        // Add source to priority queue
        priorityQueue.add(sourceNode);
        
        Set<Node> visited = new HashSet<>();
        
        // Main Dijkstra loop
        while (!priorityQueue.isEmpty()) {
            Node current = priorityQueue.poll();
            
            // If already visited, skip
            if (visited.contains(current)) {
                continue;
            }
            visited.add(current);
            
            double currentDistance = distances.get(current);
            
            // Process all neighbors
            List<Edge> adjacentEdges = graph.getAdjacentEdges(current);
            if (adjacentEdges != null) {
                for (Edge edge : adjacentEdges) {
                    Node neighbor = edge.getOppositeNode(current);
                    
                    if (neighbor != null && !visited.contains(neighbor)) {
                        // Calculate weight based on Euclidean distance
                        double weight = calculateEdgeWeight(edge);
                        double newDistance = currentDistance + weight;
                        double oldDistance = distances.get(neighbor);
                        
                        // Update if shorter path found
                        if (newDistance < oldDistance) {
                            distances.put(neighbor, newDistance);
                            predecessors.put(neighbor, current);
                            priorityQueue.add(neighbor);
                        }
                    }
                }
            }
        }
        
        return new DijkstraResult(distances, predecessors, sourceNode);
    }
    
    /**
     * Calculates the weight (cost) of traversing an edge based on its Euclidean distance.
     * 
     * @param edge The edge to calculate weight for
     * @return The weight of the edge
     */
    private double calculateEdgeWeight(Edge edge) {
        Node start = edge.getStart();
        Node end = edge.getEnd();
        
        // Euclidean distance between nodes
        double dx = end.getX() - start.getX();
        double dy = end.getY() - start.getY();
        return Math.sqrt(dx * dx + dy * dy);
    }
}
