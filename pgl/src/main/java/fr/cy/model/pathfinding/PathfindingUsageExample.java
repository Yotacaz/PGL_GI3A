package fr.cy.model.pathfinding;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import fr.cy.model.graph.Graph;
import fr.cy.model.graph.element.Node;

/**
 * Exemple d'utilisation complet du système de pathfinding Min-Cost Max-Flow.
 *
 * Cette classe illustre comment utiliser les différentes composantes
 * du système de pathfinding dans une simulation d'agents.
 *
 * @author GI3A
 * @version 1.0
 */
public class PathfindingUsageExample implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * Exemple 1 : Utilisation basique du pathfinding.
     */
    public static void example1_BasicPathfinding() {
        // Créer un graphe
        Graph graph = new Graph();
        Node start = graph.createNode(0, 0, 10);
        Node middle = graph.createNode(10, 0, 10);
        Node goal = graph.createNode(20, 0, 10);

        graph.createEdge(start, middle);
        graph.createEdge(middle, goal);

        // Créer le gestionnaire de pathfinding
        PathfindingManager manager = new PathfindingManager(graph);

        // Trouver un chemin
        List<Node> path = manager.findShortestPath(start, goal);
        System.out.println("Path found: " + path.size() + " nodes");

        // Valider le chemin
        if (PathfindingUtils.isValidPath(path, graph)) {
            double length = PathfindingUtils.calculatePathLength(path, graph);
            double cost = PathfindingUtils.calculatePathCost(path, graph);
            System.out.println("Path length: " + length + ", Cost: " + cost);
        }
    }

    /**
     * Exemple 2 : Pathfinding avec suivi de flux.
     */
    public static void example2_FluxTracking() {
        Graph graph = new Graph();
        Node n1 = graph.createNode(0, 0, 10);
        Node n2 = graph.createNode(10, 0, 10);
        Node n3 = graph.createNode(20, 0, 10);

        graph.createEdge(n1, n2);
        graph.createEdge(n2, n3);

        // Gestionnaire de pathfinding
        PathfindingManager manager = new PathfindingManager(graph);
        FlowStateManager flowState = manager.getFlowStateManager();

        // Simuler le passage de plusieurs agents
        for (int i = 0; i < 5; i++) {
            List<Node> path = manager.findShortestPath(n1, n3);

            // Enregistrer le flux
            for (Node node : path) {
                manager.recordNodeFlow(node, 1.0);
            }
        }

        // Vérifier la congestion
        double utilization = flowState.getNodeUtilizationRate(n2);
        System.out.println("Node n2 utilization: " + (utilization * 100) + "%");
    }

    /**
     * Exemple 3 : Équilibrage de charge.
     */
    public static void example3_LoadBalancing() {
        Graph graph = new Graph();
        Node start = graph.createNode(0, 0, 10);
        Node path1_mid = graph.createNode(5, 5, 10);
        Node path2_mid = graph.createNode(5, -5, 10);
        Node goal = graph.createNode(10, 0, 10);

        graph.createEdge(start, path1_mid);
        graph.createEdge(path1_mid, goal);
        graph.createEdge(start, path2_mid);
        graph.createEdge(path2_mid, goal);

        LoadBalancingPathfinder loadBalancer = new LoadBalancingPathfinder(graph);

        // Trouver chemins équilibrés pour plusieurs requêtes
        List<FlowBasedPathfinding.Pair<Node, Node>> requests = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            requests.add(new FlowBasedPathfinding.Pair<>(start, goal));
        }

        List<List<Node>> paths = loadBalancer.distributeLoadAcrossPaths(requests);
        System.out.println("Distributed " + paths.size() + " paths");
    }

    /**
     * Exemple 4 : Cache de chemins.
     */
    public static void example4_PathCaching() {
        Graph graph = new Graph();
        Node n1 = graph.createNode(0, 0, 10);
        Node n2 = graph.createNode(10, 0, 10);
        Node n3 = graph.createNode(20, 0, 10);

        graph.createEdge(n1, n2);
        graph.createEdge(n2, n3);

        // Créer un cache
        PathCache cache = new PathCache(graph, 100);
        PathfindingManager manager = new PathfindingManager(graph);

        // Première requête - miss de cache
        long start = System.currentTimeMillis();
        List<Node> path1 = manager.findShortestPath(n1, n3);
        long time1 = System.currentTimeMillis() - start;
        cache.cachePath(n1, n3, path1);

        // Deuxième requête - hit de cache
        start = System.currentTimeMillis();
        List<Node> cachedPath = cache.getCachedPath(n1, n3);
        long time2 = System.currentTimeMillis() - start;

        System.out.println("First compute: " + time1 + "ms");
        System.out.println("Cached retrieval: " + time2 + "ms");
        System.out.println("Speedup: " + (time1 / (double) time2) + "x");

        // Afficher les stats du cache
        double[] stats = cache.getStatistics();
        System.out.println("Cache hits: " + (int) stats[0]);
        System.out.println("Cache misses: " + (int) stats[1]);
        System.out.println("Hit rate: " + (stats[2] * 100) + "%");
    }

    /**
     * Exemple 5 : Stratégies de sélection de chemin.
     */
    public static void example5_SelectionStrategies() {
        Graph graph = new Graph();
        Node start = graph.createNode(0, 0, 10);
        Node goal = graph.createNode(20, 0, 10);
        graph.createEdge(start, goal);

        PathSelectionStrategy greedy = new PathSelectionStrategies.GreedyStrategy();
        PathSelectionStrategy shortest = new PathSelectionStrategies.ShortestPathStrategy();
        PathSelectionStrategy random = new PathSelectionStrategies.RandomPathStrategy();

        List<List<Node>> availablePaths = new ArrayList<>();
        availablePaths.add(new ArrayList<>(List.of(start, goal)));

        List<Node> selected1 = greedy.selectPath(start, goal, availablePaths);
        List<Node> selected2 = shortest.selectPath(start, goal, availablePaths);
        List<Node> selected3 = random.selectPath(start, goal, availablePaths);

        System.out.println("Greedy selected: " + selected1.size() + " nodes");
        System.out.println("Shortest selected: " + selected2.size() + " nodes");
        System.out.println("Random selected: " + selected3.size() + " nodes");
    }

    /**
     * Exemple 6 : Rapport de performance.
     */
    public static void example6_PerformanceReporting() {
        PathfindingPerformanceReport report = new PathfindingPerformanceReport();

        report.start();

        for (int i = 0; i < 100; i++) {
            long computeStart = System.nanoTime();
            // Simulation de calcul de chemin
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
            }
            long computeTime = (System.nanoTime() - computeStart) / 1_000_000;

            report.recordPathComputation(5 + (i % 5), 50 + i, computeTime);
            report.recordFlowProcessed(1.0);
        }

        report.stop();

        System.out.println(report.generateReport());
    }

    /**
     * Exemple 7 : Gestion de suppression de nœud.
     */
    public static void example7_NodeDeletion() {
        Graph graph = new Graph();
        Node n1 = graph.createNode(0, 0, 10);
        Node n2 = graph.createNode(10, 0, 10);
        Node n3 = graph.createNode(20, 0, 10);

        graph.createEdge(n1, n2);
        graph.createEdge(n2, n3);

        PathfindingManager manager = new PathfindingManager(graph);
        List<Node> path = manager.findShortestPath(n1, n3);

        System.out.println("Original path: " + path.size() + " nodes");

        // Supprimer le nœud intermédiaire
        // (À adapter en fonction de votre API Graph)
        // graph.removeNode(n2);

        // Les chemins affectés devraient être recalculés
        System.out.println("Path updated after node deletion");
    }

    /**
     * Exemple 8 : Optimisation et validation de chemin.
     */
    public static void example8_PathOptimization() {
        Graph graph = new Graph();
        List<Node> nodes = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            nodes.add(graph.createNode(i * 10, 0, 10));
        }

        for (int i = 0; i < nodes.size() - 1; i++) {
            graph.createEdge(nodes.get(i), nodes.get(i + 1));
        }

        List<Node> path = nodes;

        // Valider
        boolean valid = PathfindingUtils.isValidPath(path, graph);
        System.out.println("Path valid: " + valid);

        // Vérifier boucles
        boolean hasLoop = PathfindingUtils.hasLoop(path);
        System.out.println("Path has loop: " + hasLoop);

        // Calculer les statistiques
        double length = PathfindingUtils.calculatePathLength(path, graph);
        double cost = PathfindingUtils.calculatePathCost(path, graph);
        int congestedEdges = PathfindingUtils.countCongestedEdges(path, graph);

        System.out.println("Length: " + length);
        System.out.println("Cost: " + cost);
        System.out.println("Congested edges: " + congestedEdges);

        // Optimiser
        List<Node> optimized = PathfindingUtils.optimizePath(path, graph);
        System.out.println("Optimized path length: " + optimized.size());
    }

    /**
     * Méthode principale pour exécuter tous les exemples.
     */
    public static void main(String[] args) {
        System.out.println("=== Pathfinding Usage Examples ===\n");

        System.out.println("Example 1: Basic Pathfinding");
        example1_BasicPathfinding();
        System.out.println();

        System.out.println("Example 2: Flux Tracking");
        example2_FluxTracking();
        System.out.println();

        System.out.println("Example 3: Load Balancing");
        example3_LoadBalancing();
        System.out.println();

        System.out.println("Example 4: Path Caching");
        example4_PathCaching();
        System.out.println();

        System.out.println("Example 5: Selection Strategies");
        example5_SelectionStrategies();
        System.out.println();

        System.out.println("Example 6: Performance Reporting");
        example6_PerformanceReporting();
        System.out.println();

        System.out.println("Example 7: Node Deletion");
        example7_NodeDeletion();
        System.out.println();

        System.out.println("Example 8: Path Optimization");
        example8_PathOptimization();
        System.out.println();
    }
}
