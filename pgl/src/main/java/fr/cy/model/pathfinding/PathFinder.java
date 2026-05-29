package fr.cy.model.pathfinding;

import java.util.ArrayList;
import java.util.List;

import fr.cy.model.graph.Graph;
import fr.cy.model.graph.element.Edge;
import fr.cy.model.graph.element.Node;

/**
 * Classe de pathfinding utilisant l'algorithme Min-Cost Max-Flow.
 *
 * Le coût d'un chemin dépend :
 * - de la longueur des arêtes
 * - du danger des éléments traversés
 * - de la congestion des arêtes (capacité et flux)
 *
 * Cet algorithme est optimal pour gérer les goulots d'étranglement
 * et la distribution des agents dans le réseau.
 *
 * @author GI3A
 * @version 1.0
 */
public class PathFinder {

    private final Graph graph;
    private final MinCostMaxFlow flowAlgorithm;

    public PathFinder(Graph graph) {
        this.graph = graph;
        this.flowAlgorithm = new MinCostMaxFlow(graph);
    }

    /**
     * Trouve le chemin le plus court entre deux nœuds en utilisant
     * l'algorithme Min-Cost Max-Flow avec un flux de 1 unité.
     *
     * @param start le nœud de départ
     * @param goal  le nœud d'arrivée
     * @return liste des nœuds du chemin optimal
     */
    public List<Node> shortestPath(Node start, Node goal) {
        // Construire le graphe de flux basé sur l'état actuel du graphe
        flowAlgorithm.buildFlowGraph(start, goal, 1.0);

        // Calculer le chemin optimal avec coût minimum et flux maximum
        List<Node> path = flowAlgorithm.computeMinCostMaxFlow(start, goal, 1.0);

        // Si aucun chemin trouvé, retourner une liste vide
        if (path.isEmpty()) {
            return new ArrayList<>();
        }

        return path;
    }


    public double getDistanceToNearestExit(Node start) {
        double minDistance = Double.POSITIVE_INFINITY;

        for (Node exit : graph.getExits()) {
            List<Node> path = shortestPath(start, exit);
            if (!path.isEmpty()) {
                double distance = path.stream()
                        .flatMap(node -> graph.getAdjacentEdges(node).stream())
                        .mapToDouble(Edge::getLength)
                        .sum();

                minDistance = Math.min(minDistance, distance);
            }
        }

        return minDistance;
    }

}
