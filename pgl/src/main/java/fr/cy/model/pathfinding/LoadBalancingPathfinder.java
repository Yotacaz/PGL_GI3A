package fr.cy.model.pathfinding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.cy.model.graph.Graph;
import fr.cy.model.graph.element.Edge;
import fr.cy.model.graph.element.Node;

/**
 * Optimiseur de chemins avec équilibrage de charge.
 *
 * Cette classe permet d'optimiser la distribution du flux dans le réseau
 * en équilibrant la charge entre plusieurs arêtes parallèles et en répartissant
 * les agents de manière optimale.
 *
 * @author GI3A
 * @version 1.0
 */
public class LoadBalancingPathfinder {
    private final PathfindingManager pathfindingManager;
    private final FlowStateManager flowStateManager;
    private final Graph graph;

    /**
     * Constructeur de l'optimiseur de chemins.
     *
     * @param graph le graphe
     */
    public LoadBalancingPathfinder(Graph graph) {
        this.graph = graph;
        this.pathfindingManager = new PathfindingManager(graph);
        this.flowStateManager = new FlowStateManager(graph);
    }

    /**
     * Trouve le chemin optimal en considérant la charge actuelle du réseau.
     *
     * @param source      nœud source
     * @param destination nœud destination
     * @return le chemin équilibré
     */
    public List<Node> findLoadBalancedPath(Node source, Node destination) {
        // Obtenir les chemins alternatifs
        List<List<Node>> alternativePaths = findAlternativePaths(source, destination, 3);
        
        if (alternativePaths.isEmpty()) {
            return new ArrayList<>();
        }

        // Sélectionner le chemin avec le moins de charge
        return selectLeastLoadedPath(alternativePaths);
    }

    /**
     * Trouve plusieurs chemins alternatifs entre source et destination.
     *
     * @param source      nœud source
     * @param destination nœud destination
     * @param maxPaths    nombre maximum de chemins à trouver
     * @return liste des chemins alternatifs
     */
    public List<List<Node>> findAlternativePaths(Node source, Node destination, int maxPaths) {
        List<List<Node>> paths = new ArrayList<>();
        List<Node> primaryPath = pathfindingManager.findShortestPath(source, destination);
        
        if (!primaryPath.isEmpty()) {
            paths.add(primaryPath);
        }

        // Ajouter d'autres chemins (via BFS avec exclusion d'arêtes utilisées)
        for (int i = 1; i < maxPaths && i < graph.getNodes().size(); i++) {
            // Implémentation simplifiée - pourrait être améliorée
            List<Node> altPath = findAlternativePathExcluding(source, destination, paths);
            if (!altPath.isEmpty()) {
                paths.add(altPath);
            }
        }

        return paths;
    }

    /**
     * Trouve un chemin alternatif en excluant les chemins déjà trouvés.
     *
     * @param source       nœud source
     * @param destination  nœud destination
     * @param excludePaths chemins à exclure
     * @return un chemin alternatif
     */
    private List<Node> findAlternativePathExcluding(Node source, Node destination, 
                                                     List<List<Node>> excludePaths) {
        // Implémentation simplifiée - retourner un chemin vide
        // Une véritable implémentation utiliserait la recherche en largeur (BFS)
        // avec modification dynamique des coûts ou des arêtes exclues
        return new ArrayList<>();
    }

    /**
     * Sélectionne le chemin avec le moins de charge.
     *
     * @param paths les chemins disponibles
     * @return le chemin avec le moins de charge
     */
    private List<Node> selectLeastLoadedPath(List<List<Node>> paths) {
        return paths.stream()
            .min((p1, p2) -> Double.compare(
                calculatePathLoad(p1),
                calculatePathLoad(p2)
            ))
            .orElse(paths.isEmpty() ? new ArrayList<>() : paths.get(0));
    }

    /**
     * Calcule la charge d'un chemin.
     *
     * @param path le chemin
     * @return la charge totale du chemin
     */
    private double calculatePathLoad(List<Node> path) {
        double load = 0;
        
        for (int i = 0; i < path.size() - 1; i++) {
            Node current = path.get(i);
            Node next = path.get(i + 1);
            
            for (Edge edge : graph.getAdjacentEdges(current)) {
                if (edge.getOppositeNode(current).equals(next)) {
                    double utilizationRate = flowStateManager.getEdgeUtilizationRate(edge);
                    load += utilizationRate;
                    break;
                }
            }
        }
        
        return load / Math.max(path.size() - 1, 1);
    }

    /**
     * Distribue des requêtes de chemin entre plusieurs chemins pour équilibrer la charge.
     *
     * @param requests liste des requêtes (source, destination)
     * @return liste des chemins sélectionnés
     */
    public List<List<Node>> distributeLoadAcrossPaths(
        List<FlowBasedPathfinding.Pair<Node, Node>> requests) {
        
        List<List<Node>> selectedPaths = new ArrayList<>();
        Map<List<Node>, Integer> pathUsageCount = new HashMap<>();

        for (FlowBasedPathfinding.Pair<Node, Node> request : requests) {
            List<Node> path = findLoadBalancedPath(request.getFirst(), request.getSecond());
            selectedPaths.add(path);
            pathUsageCount.put(path, pathUsageCount.getOrDefault(path, 0) + 1);
        }

        return selectedPaths;
    }

    /**
     * Retourne le gestionnaire de pathfinding.
     *
     * @return l'instance de PathfindingManager
     */
    public PathfindingManager getPathfindingManager() {
        return pathfindingManager;
    }

    /**
     * Retourne le gestionnaire d'état de flux.
     *
     * @return l'instance de FlowStateManager
     */
    public FlowStateManager getFlowStateManager() {
        return flowStateManager;
    }
}
