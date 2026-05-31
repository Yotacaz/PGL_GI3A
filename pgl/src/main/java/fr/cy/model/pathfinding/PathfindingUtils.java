package fr.cy.model.pathfinding;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import fr.cy.model.graph.Graph;
import fr.cy.model.graph.element.Edge;
import fr.cy.model.graph.element.Node;

/**
 * Utilitaires et validateurs pour le système de pathfinding.
 *
 * @author GI3A
 * @version 1.0
 */
public class PathfindingUtils implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * Valide qu'un chemin est valide (tous les nœuds existent et sont connectés).
     *
     * @param path  le chemin à valider
     * @param graph le graphe de référence
     * @return true si le chemin est valide
     */
    public static boolean isValidPath(List<Node> path, Graph graph) {
        if (path == null || path.isEmpty()) {
            return false;
        }

        // Vérifier que tous les nœuds existent dans le graphe
        Set<Node> graphNodes = new HashSet<>(graph.getNodes());
        for (Node node : path) {
            if (!graphNodes.contains(node)) {
                return false;
            }
        }

        // Vérifier que les nœuds consécutifs sont connectés
        for (int i = 0; i < path.size() - 1; i++) {
            Node current = path.get(i);
            Node next = path.get(i + 1);

            boolean connected = false;
            for (Edge edge : graph.getAdjacentEdges(current)) {
                if (edge.getOppositeNode(current).equals(next)) {
                    connected = true;
                    break;
                }
            }

            if (!connected) {
                return false;
            }
        }

        return true;
    }

    /**
     * Calcule la longueur totale d'un chemin.
     *
     * @param path  le chemin
     * @param graph le graphe de référence
     * @return la longueur totale
     */
    public static double calculatePathLength(List<Node> path, Graph graph) {
        if (path.size() < 2) {
            return 0;
        }

        double length = 0;
        for (int i = 0; i < path.size() - 1; i++) {
            Node current = path.get(i);
            Node next = path.get(i + 1);

            for (Edge edge : graph.getAdjacentEdges(current)) {
                if (edge.getOppositeNode(current).equals(next)) {
                    length += edge.getLength();
                    break;
                }
            }
        }

        return length;
    }

    /**
     * Calcule le coût total d'un chemin (longueur + stress).
     *
     * @param path  le chemin
     * @param graph le graphe de référence
     * @return le coût total
     */
    public static double calculatePathCost(List<Node> path, Graph graph) {
        if (path.size() < 2) {
            return 0;
        }

        double cost = 0;
        for (int i = 0; i < path.size() - 1; i++) {
            Node current = path.get(i);
            Node next = path.get(i + 1);

            for (Edge edge : graph.getAdjacentEdges(current)) {
                if (edge.getOppositeNode(current).equals(next)) {
                    double edgeCost = PathfindingConfig.computeEdgeCost(
                            edge.getLength(),
                            edge.getStressInducingImpact());
                    cost += edgeCost;
                    break;
                }
            }
        }

        return cost;
    }

    /**
     * Compte le nombre d'arêtes congestionnées dans un chemin.
     *
     * @param path  le chemin
     * @param graph le graphe de référence
     * @return le nombre d'arêtes congestionnées
     */
    public static int countCongestedEdges(List<Node> path, Graph graph) {
        if (path.size() < 2) {
            return 0;
        }

        int count = 0;
        for (int i = 0; i < path.size() - 1; i++) {
            Node current = path.get(i);
            Node next = path.get(i + 1);

            for (Edge edge : graph.getAdjacentEdges(current)) {
                if (edge.getOppositeNode(current).equals(next)) {
                    if (edge.isCongested()) {
                        count++;
                    }
                    break;
                }
            }
        }

        return count;
    }

    /**
     * Reconstruit un chemin à partir d'une liste de nœuds invalides en trouvant
     * un chemin alternatif qui les contourne.
     *
     * @param originalPath le chemin original
     * @param invalidNodes les nœuds à contourner
     * @param graph        le graphe
     * @return un chemin alternatif sans les nœuds invalides
     */
    public static List<Node> rebuildPathAvoidingNodes(List<Node> originalPath,
            Set<Node> invalidNodes,
            Graph graph) {
        List<Node> newPath = new ArrayList<>();

        for (Node node : originalPath) {
            if (!invalidNodes.contains(node)) {
                newPath.add(node);
            }
        }

        // Si le nouveau chemin est fragmenté, il faudrait utiliser un algorithme
        // de pathfinding pour reconnecter les fragments
        return newPath;
    }

    /**
     * Vérifie si un chemin contient des boucles.
     *
     * @param path le chemin à vérifier
     * @return true si le chemin contient une boucle
     */
    public static boolean hasLoop(List<Node> path) {
        Set<Node> visited = new HashSet<>();

        for (Node node : path) {
            if (visited.contains(node)) {
                return true;
            }
            visited.add(node);
        }

        return false;
    }

    /**
     * Optimise un chemin en supprimant les détours inutiles.
     *
     * @param path  le chemin à optimiser
     * @param graph le graphe de référence
     * @return le chemin optimisé
     */
    public static List<Node> optimizePath(List<Node> path, Graph graph) {
        if (path.size() <= 2) {
            return new ArrayList<>(path);
        }

        List<Node> optimized = new ArrayList<>();
        optimized.add(path.get(0));

        for (int i = 1; i < path.size() - 1; i++) {
            Node prev = optimized.get(optimized.size() - 1);
            Node current = path.get(i);
            Node next = path.get(i + 1);

            // Vérifier si nous pouvons sauter le nœud courant
            boolean canSkip = false;
            for (Edge edge : graph.getAdjacentEdges(prev)) {
                if (edge.getOppositeNode(prev).equals(next)) {
                    canSkip = true;
                    break;
                }
            }

            if (!canSkip) {
                optimized.add(current);
            }
        }

        optimized.add(path.get(path.size() - 1));
        return optimized;
    }
}
