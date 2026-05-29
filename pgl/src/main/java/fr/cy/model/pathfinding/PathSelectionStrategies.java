package fr.cy.model.pathfinding;

import java.util.List;

import fr.cy.model.graph.element.Node;

/**
 * Implémentations concrètes de stratégies de sélection de chemin.
 *
 * @author GI3A
 * @version 1.0
 */
public class PathSelectionStrategies {

    /**
     * Stratégie "greedy" : sélectionne le premier chemin disponible.
     */
    public static class GreedyStrategy implements PathSelectionStrategy {
        @Override
        public List<Node> selectPath(Node startNode, Node goalNode, List<List<Node>> availablePaths) {
            return availablePaths.isEmpty() ? List.of() : availablePaths.get(0);
        }
    }

    /**
     * Stratégie "shortest" : sélectionne le chemin le plus court.
     */
    public static class ShortestPathStrategy implements PathSelectionStrategy {
        @Override
        public List<Node> selectPath(Node startNode, Node goalNode, List<List<Node>> availablePaths) {
            if (availablePaths.isEmpty()) {
                return List.of();
            }
            
            return availablePaths.stream()
                .min((p1, p2) -> Integer.compare(p1.size(), p2.size()))
                .orElse(List.of());
        }
    }

    /**
     * Stratégie "random" : sélectionne aléatoirement un chemin.
     */
    public static class RandomPathStrategy implements PathSelectionStrategy {
        @Override
        public List<Node> selectPath(Node startNode, Node goalNode, List<List<Node>> availablePaths) {
            if (availablePaths.isEmpty()) {
                return List.of();
            }
            
            int index = (int) (Math.random() * availablePaths.size());
            return availablePaths.get(index);
        }
    }

    /**
     * Stratégie "stress-avoidant" : évite les chemins avec fort facteur de stress.
     */
    public static class StressAvoidantStrategy implements PathSelectionStrategy {
        @Override
        public List<Node> selectPath(Node startNode, Node goalNode, List<List<Node>> availablePaths) {
            if (availablePaths.isEmpty()) {
                return List.of();
            }
            
            // Sélectionner le chemin avec le stress minimum
            // (À améliorer avec accès aux données de stress des arêtes)
            return availablePaths.get(0);
        }
    }

    /**
     * Stratégie "risk-taker" : prend les chemins les plus rapides malgré les risques.
     */
    public static class RiskTakerStrategy implements PathSelectionStrategy {
        @Override
        public List<Node> selectPath(Node startNode, Node goalNode, List<List<Node>> availablePaths) {
            if (availablePaths.isEmpty()) {
                return List.of();
            }
            
            // Sélectionner le chemin le plus court (prenant des risques)
            return availablePaths.stream()
                .min((p1, p2) -> Integer.compare(p1.size(), p2.size()))
                .orElse(List.of());
        }
    }
}
