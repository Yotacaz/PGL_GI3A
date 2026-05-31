package fr.cy.model.pathfinding;

import java.util.HashMap;
import java.util.Map;
import java.io.Serializable;

/**
 * Rapport de performance du système de pathfinding.
 *
 * Collecte et analyse les métriques de performance du système
 * pour l'optimisation et le monitoring.
 *
 * @author GI3A
 * @version 1.0
 */
public class PathfindingPerformanceReport {
    private static final long serialVersionUID = 1L;
    private final Map<String, Double> metrics;
    private long startTime;
    private long endTime;
    private int pathsComputed;
    private int totalNodesVisited;
    private double totalFlowProcessed;

    /**
     * Constructeur du rapport de performance.
     */
    public PathfindingPerformanceReport() {
        this.metrics = new HashMap<>();
        this.pathsComputed = 0;
        this.totalNodesVisited = 0;
        this.totalFlowProcessed = 0;
    }

    /**
     * Démarre le chronomètre.
     */
    public void start() {
        this.startTime = System.nanoTime();
    }

    /**
     * Arrête le chronomètre.
     */
    public void stop() {
        this.endTime = System.nanoTime();
        long duration = (endTime - startTime) / 1_000_000; // Convertir en ms
        metrics.put("total_time_ms", (double) duration);
    }

    /**
     * Enregistre un chemin calculé.
     *
     * @param pathLength  longueur du chemin
     * @param cost        coût du chemin
     * @param computeTime temps de calcul en ms
     */
    public void recordPathComputation(int pathLength, double cost, long computeTime) {
        pathsComputed++;
        totalNodesVisited += pathLength;

        metrics.put("paths_computed", (double) pathsComputed);
        metrics.put("avg_path_length", (double) totalNodesVisited / pathsComputed);
        metrics.put("last_compute_time_ms", (double) computeTime);
        metrics.put("total_cost", metrics.getOrDefault("total_cost", 0.0) + cost);
    }

    /**
     * Enregistre le flux traité.
     *
     * @param flow montant de flux
     */
    public void recordFlowProcessed(double flow) {
        totalFlowProcessed += flow;
        metrics.put("total_flow", totalFlowProcessed);
    }

    /**
     * Ajoute une métrique personnalisée.
     *
     * @param name  nom de la métrique
     * @param value valeur
     */
    public void recordMetric(String name, double value) {
        metrics.put(name, value);
    }

    /**
     * Retourne une métrique.
     *
     * @param name nom de la métrique
     * @return valeur de la métrique
     */
    public double getMetric(String name) {
        return metrics.getOrDefault(name, 0.0);
    }

    /**
     * Retourne toutes les métriques.
     *
     * @return map des métriques
     */
    public Map<String, Double> getAllMetrics() {
        return new HashMap<>(metrics);
    }

    /**
     * Retourne un rapport formaté.
     *
     * @return rapport texte
     */
    public String generateReport() {
        StringBuilder report = new StringBuilder();
        report.append("=== Pathfinding Performance Report ===\n");
        report.append(String.format("Paths computed: %d\n", pathsComputed));
        report.append(String.format("Average path length: %.2f nodes\n",
                getMetric("avg_path_length")));
        report.append(String.format("Total cost: %.2f\n", getMetric("total_cost")));
        report.append(String.format("Total flow: %.2f\n", totalFlowProcessed));
        report.append(String.format("Total computation time: %.2f ms\n",
                getMetric("total_time_ms")));

        if (pathsComputed > 0) {
            double avgTime = getMetric("total_time_ms") / pathsComputed;
            report.append(String.format("Average time per path: %.4f ms\n", avgTime));
        }

        report.append("=====================================\n");
        return report.toString();
    }

    /**
     * Réinitialise toutes les métriques.
     */
    public void reset() {
        metrics.clear();
        pathsComputed = 0;
        totalNodesVisited = 0;
        totalFlowProcessed = 0;
    }
}
