package fr.cy.model.pathfinding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.Serializable;

import fr.cy.model.graph.Graph;
import fr.cy.model.graph.element.Node;

/**
 * Cache pour les chemins et optimisations de performance.
 *
 * Cette classe optimise le pathfinding en mettant en cache les chemins
 * calculés et en permettant une réutilisation rapide pour des requêtes
 * similaires.
 *
 * @author GI3A
 * @version 1.0
 */
public class PathCache {
    private static final long serialVersionUID = 1L;
    private final Map<String, List<Node>> cache;
    private final Graph graph;
    private int cacheHits;
    private int cacheMisses;
    private final int maxCacheSize;

    /**
     * Constructeur du cache de chemins.
     *
     * @param graph        le graphe
     * @param maxCacheSize taille maximale du cache
     */
    public PathCache(Graph graph, int maxCacheSize) {
        this.graph = graph;
        this.cache = new HashMap<>();
        this.maxCacheSize = maxCacheSize;
        this.cacheHits = 0;
        this.cacheMisses = 0;
    }

    /**
     * Génère une clé de cache pour une paire source-destination.
     *
     * @param source      nœud source
     * @param destination nœud destination
     * @return clé unique
     */
    private String generateCacheKey(Node source, Node destination) {
        return source.getId() + "->" + destination.getId();
    }

    /**
     * Récupère un chemin du cache.
     *
     * @param source      nœud source
     * @param destination nœud destination
     * @return le chemin si en cache, null sinon
     */
    public List<Node> getCachedPath(Node source, Node destination) {
        String key = generateCacheKey(source, destination);
        List<Node> path = cache.get(key);

        if (path != null) {
            cacheHits++;
            return new ArrayList<>(path); // Retourner une copie
        }

        cacheMisses++;
        return null;
    }

    /**
     * Ajoute un chemin au cache.
     *
     * @param source      nœud source
     * @param destination nœud destination
     * @param path        le chemin à mettre en cache
     */
    public void cachePath(Node source, Node destination, List<Node> path) {
        if (cache.size() >= maxCacheSize) {
            // Nettoyer le cache en supprimant une entrée aléatoire
            cache.remove(cache.keySet().iterator().next());
        }

        String key = generateCacheKey(source, destination);
        cache.put(key, new ArrayList<>(path));
    }

    /**
     * Invalide un chemin du cache.
     *
     * @param source      nœud source
     * @param destination nœud destination
     */
    public void invalidatePath(Node source, Node destination) {
        String key = generateCacheKey(source, destination);
        cache.remove(key);
    }

    /**
     * Invalide tous les chemins passant par un nœud.
     *
     * @param node le nœud
     */
    public void invalidatePathsContainingNode(Node node) {
        cache.entrySet().removeIf(entry -> {
            List<Node> path = entry.getValue();
            return path.contains(node);
        });
    }

    /**
     * Invalide tous les chemins contenant une arête.
     *
     * @param sourceNode      source de l'arête
     * @param destinationNode destination de l'arête
     */
    public void invalidatePathsContainingEdge(Node sourceNode, Node destinationNode) {
        cache.entrySet().removeIf(entry -> {
            List<Node> path = entry.getValue();
            for (int i = 0; i < path.size() - 1; i++) {
                if ((path.get(i).equals(sourceNode) && path.get(i + 1).equals(destinationNode)) ||
                        (path.get(i).equals(destinationNode) && path.get(i + 1).equals(sourceNode))) {
                    return true;
                }
            }
            return false;
        });
    }

    /**
     * Vide complètement le cache.
     */
    public void clear() {
        cache.clear();
    }

    /**
     * Retourne les statistiques du cache.
     *
     * @return tableau [cacheHits, cacheMisses, hitRate]
     */
    public double[] getStatistics() {
        double total = cacheHits + cacheMisses;
        double hitRate = total > 0 ? (double) cacheHits / total : 0;
        return new double[] { cacheHits, cacheMisses, hitRate };
    }

    /**
     * Retourne le nombre d'entrées en cache.
     *
     * @return nombre d'entrées
     */
    public int getCacheSize() {
        return cache.size();
    }

    /**
     * Retourne le nombre de hits de cache.
     *
     * @return nombre de hits
     */
    public int getCacheHits() {
        return cacheHits;
    }

    /**
     * Retourne le nombre de misses de cache.
     *
     * @return nombre de misses
     */
    public int getCacheMisses() {
        return cacheMisses;
    }

    /**
     * Réinitialise les statistiques du cache.
     */
    public void resetStatistics() {
        cacheHits = 0;
        cacheMisses = 0;
    }
}
