package fr.cy.model.pathfinding;

import fr.cy.model.graph.Graph;
import fr.cy.model.graph.element.Node;
import fr.cy.model.graph.element.Edge;
import java.util.List;
import java.util.ArrayList;
import java.io.Serializable;

/**
 * Main pathfinding system for agents in the simulation.
 * 
 * Provides two core pathfinding functionalities:
 * - nearestExit: Finds the closest exit from a given starting node using Dijkstra's algorithm
 * - shortestPath: Finds the optimal path to an exit using MAPF (Multi-Agent Path Finding)
 *   while considering congestion and fire hazards
 * 
 * @author GI3A
 * @version 1.0
 */
public class PathFinder implements Serializable {
    
    private final Graph graph;
    private final DijkstraAlgorithm dijkstra;
    private final MapfAlgorithm mapf;
    


    /**
     * Constructs a new Pathfinder for the given graph.
     * 
     * @param graph The graph structure to use for pathfinding
     */
    public PathFinder(Graph graph) {
        this.graph = graph;
        this.dijkstra = new DijkstraAlgorithm(graph);
        this.mapf = new MapfAlgorithm(graph);
    }
    
    /**
     * Finds the nearest exit from a given starting node using Dijkstra's algorithm.
     * 
     * This method is useful for quick exit identification when multi-agent coordination
     * is not a concern (e.g., initial path planning).
     * 
     * @param startNode The node from which to search for the nearest exit
     * @return The nearest exit node, or null if no exit is reachable
     */
    public Node nearestExit(Node startNode) {
        if (startNode == null) {
            return null;
        }
        
        // Get all exit nodes from the graph
        List<Node> exitNodes = getExitNodes();
        if (exitNodes.isEmpty()) {
            return null;
        }
        
        // Use Dijkstra to find distances to all nodes
        DijkstraResult result = dijkstra.computeShortestPaths(startNode);
        
        if (result == null) {
            return null;
        }
        
        // Find the closest exit
        Node nearestExit = null;
        double minDistance = Double.MAX_VALUE;
        
        for (Node exitNode : exitNodes) {
            double distance = result.getDistance(exitNode);
            if (distance < minDistance && distance != Double.MAX_VALUE) {
                minDistance = distance;
                nearestExit = exitNode;
            }
        }
        
        return nearestExit;
    }
    
    /**
     * Finds the shortest path to an exit from a starting node using MAPF.
     * 
     * This method takes into account:
     * - Congestion levels on edges (favors less congested paths)
     * - Fire hazards on nodes and edges (avoids or penalizes burning areas)
     * - Multi-agent coordination to reduce path conflicts
     * 
     * The result is an optimal path that balances travel time with safety.
     * 
     * @param startNode The node from which to find a path to an exit
     * @return A GraphPath object containing the sequence of nodes and edges to follow,
     *         or an empty GraphPath if no path is found
     */
    public GraphPath shortestPath(Node startNode) {
        if (startNode == null) {
            return new GraphPath();
        }
        
        // Get all exit nodes from the graph
        List<Node> exitNodes = getExitNodes();
        if (exitNodes.isEmpty()) {
            return new GraphPath();
        }
        
        // Use MAPF to find the optimal path considering congestion and fire
        GraphPath path = mapf.findOptimalPath(startNode, exitNodes);
        
        return path != null ? path : new GraphPath();
    }
    
    /**
     * Gets all exit nodes in the graph.
     * 
     * @return A list of all nodes marked as exits
     */
    private List<Node> getExitNodes() {
        return graph.getExits();
    }
    
    /**
     * Gets the underlying graph structure.
     * 
     * @return The graph used by this pathfinder
     */
    public Graph getGraph() {
        return graph;
    }
    
    /**
     * Checks if a node is an exit.
     * 
     * @param node The node to check
     * @return true if the node is marked as an exit, false otherwise
     */
    public boolean isExit(Node node) {
        return node != null && node.isExit();
    }
    
    /**
     * Checks if a node or edge is on fire.
     * 
     * @param node The node to check
     * @return true if the node is on fire, false otherwise
     */
    public boolean isOnFire(Node node) {
        return node != null && node.isOnFire();
    }
    
    /**
     * Checks if an edge is on fire.
     * 
     * @param edge The edge to check
     * @return true if the edge is on fire, false otherwise
     */
    public boolean isOnFire(Edge edge) {
        return edge != null && edge.isOnFire();
    }
}
