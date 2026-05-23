package fr.cy.model.pathfinding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import fr.cy.model.graph.Graph;
import fr.cy.model.graph.element.Edge;
import fr.cy.model.graph.element.Node;

/**
 * Classe de pathfinding utilisant l'algorithme de Dijkstra.
 *
 * Le coût d'un chemin dépend :
 * - de la longueur des arêtes
 * - du danger des éléments traversés
 *
 * @author GI3A
 * @version 1.0
 */
public class PathFinder {

    private final Graph graph;

    public PathFinder(Graph graph) {
        this.graph = graph;
    }

    private double computeCost(Edge edge) {
        double cost = edge.getLength();

        cost += edge.getStressInducingFactor() * 100;

        return cost;
    }

    public List<Node> reconstructPath(Map<Node, Node> previous, Node goal) {
        List<Node> path = new ArrayList<>();
        Node current = goal;

        while (current != null) {
            path.add(current);
            current = previous.get(current);
        }

        Collections.reverse(path);
        return path;
    }

    public List<Node> shortestPath(Node start, Node goal) {
        Map<Node, Double> distances = new HashMap<>();
        Map<Node, Node> previous = new HashMap<>();
        PriorityQueue<NodeDistance> queue = new PriorityQueue<>();

        /** Initialisaiton */
        for (Node node : graph.getNodes()) {
            distances.put(node, Double.POSITIVE_INFINITY);
        }
        distances.put(start, 0.0);
        queue.add(new NodeDistance(start, 0));

        /** Djisktra */
        while (!queue.isEmpty()) {
            Node current = queue.poll().node();

            if (current.equals(goal)) {
                break;
            }

            for (Edge edge : graph.getAdjacentEdges(current)) {
                Node neighbor = edge.getOppositeNode(current);

                double cost = computeCost(edge);
                double newDistance = distances.get(current) + cost;

                if (newDistance < distances.get(neighbor)) {
                    distances.put(neighbor, newDistance);

                    previous.put(neighbor, current);

                    queue.add(new NodeDistance(neighbor, newDistance));
                }
            }
        }

        return reconstructPath(previous, goal);
    }

}
