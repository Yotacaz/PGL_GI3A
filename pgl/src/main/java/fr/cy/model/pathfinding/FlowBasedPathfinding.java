package fr.cy.model.pathfinding;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.cy.model.graph.Graph;
import fr.cy.model.graph.element.Node;

/**
 * Gestionnaire de pathfinding basé sur le flux pour gérer plusieurs demandes
 * de chemin simultanément et optimiser l'allocation des ressources.
 *
 * Ce gestionnaire permet de :
 * - Calculer les chemins pour plusieurs agents
 * - Gérer les demandes de flux concurrent
 * - Optimiser la distribution du flux dans le réseau
 *
 * @author GI3A
 * @version 1.0
 */
public class FlowBasedPathfinding implements Serializable {
    private static final long serialVersionUID = 1L;
    private final Graph graph;
    private final MinCostMaxFlow flowAlgorithm;
    private final Map<Integer, List<Node>> pathCache;

    /**
     * Constructeur du gestionnaire de pathfinding basé sur le flux.
     *
     * @param graph le graphe d'agents
     */
    public FlowBasedPathfinding(Graph graph) {
        this.graph = graph;
        this.flowAlgorithm = new MinCostMaxFlow(graph);
        this.pathCache = new HashMap<>();
    }

    /**
     * Calcule le chemin optimal d'une source à une destination.
     *
     * @param source      nœud source
     * @param destination nœud destination
     * @return liste des nœuds du chemin
     */
    public List<Node> findPath(Node source, Node destination) {
        // Construire le graphe de flux
        flowAlgorithm.buildFlowGraph(source, destination, 1.0);

        // Calculer le chemin avec coût minimum
        return flowAlgorithm.computeMinCostMaxFlow(source, destination, 1.0);
    }

    /**
     * Calcule les chemins pour plusieurs paires source-destination.
     *
     * @param requests liste de paires (source, destination)
     * @return liste des chemins correspondants
     */
    public List<List<Node>> findPathsForMultipleRequests(List<Pair<Node, Node>> requests) {
        List<List<Node>> paths = new ArrayList<>();

        for (Pair<Node, Node> request : requests) {
            List<Node> path = findPath(request.getFirst(), request.getSecond());
            paths.add(path);
        }

        return paths;
    }

    /**
     * Calcule le flux global pour l'ensemble des demandes de chemin.
     *
     * @param requests   liste de paires (source, destination)
     * @param flowAmount montant de flux par demande
     * @return le flux total distribué dans le réseau
     */
    public double computeGlobalFlow(List<Pair<Node, Node>> requests, double flowAmount) {
        double totalFlow = 0;

        flowAlgorithm.buildFlowGraph(requests.get(0).getFirst(), requests.get(0).getSecond(), flowAmount);

        for (Pair<Node, Node> request : requests) {
            List<Node> path = flowAlgorithm.computeMinCostMaxFlow(
                    request.getFirst(),
                    request.getSecond(),
                    flowAmount);

            if (!path.isEmpty()) {
                totalFlow += flowAmount;
            }
        }

        return totalFlow;
    }

    /**
     * Classe interne pour représenter une paire de nœuds.
     *
     * @param <T> type du premier élément
     * @param <U> type du second élément
     */
    public static class Pair<T, U> {
        private final T first;
        private final U second;

        public Pair(T first, U second) {
            this.first = first;
            this.second = second;
        }

        public T getFirst() {
            return first;
        }

        public U getSecond() {
            return second;
        }
    }

    /**
     * Retourne le gestionnaire Min-Cost Max-Flow interne.
     *
     * @return l'instance de MinCostMaxFlow
     */
    public MinCostMaxFlow getFlowAlgorithm() {
        return flowAlgorithm;
    }

    /**
     * Efface le cache des chemins.
     */
    public void clearPathCache() {
        pathCache.clear();
    }
}
