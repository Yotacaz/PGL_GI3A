package fr.cy.model.graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Représente un graphe composé de nœuds et d'arêtes.
 *
 * @author GI3A
 * @version 1.0
 */
public class Graph {

    /** Liste de tous les nœuds et edges du graphe */
    private final List<Node> nodes;
    private final List<Edge> edges;

    /** Liste d'adjacence */
    private final Map<Node, List<Edge>> adjacencyList;

    /** Prochain identifiant à attribuer aux nœuds */
    private int nextNodeId;

    /** Prochain identifiant à attribuer aux arêtes */
    private int nextEdgeId;

    // TODO : Attribution des ids sous forme de queue, recyclage

    /**
     * Crée un graphe vide.
     */
    public Graph() {
        this(new ArrayList<>(), new ArrayList<>());
    }

    /**
     * Crée un graphe à partir de listes existantes de nœuds et d'arêtes.
     *
     * @param edges liste d'arêtes initiale
     * @param nodes liste de nœuds initiale
     */
    public Graph(List<Edge> edges, List<Node> nodes) {
        this.edges = edges;
        this.nodes = nodes;

        this.adjacencyList = new HashMap<>();
        this.nextEdgeId = 0;
        this.nextNodeId = 0;
    }

    /**
     * Ajoute un nœud au graphe.
     *
     * @param node le nœud à ajouter
     */
    public void addNode(Node node) {
        nodes.add(node);
    }

    /**
     * Supprime un nœud et toutes les arêtes qui y sont connectées.
     *
     * @param node le nœud à supprimer
     */
    public void removeNode(Node node) {
        edges.removeIf(edge -> edge.getStart().equals(node)
                || edge.getEnd().equals(node));

        nodes.remove(node);
    }

    /**
     * @return la liste de nœuds du graphe
     */
    public List<Node> getNodes() {
        return nodes;
    }

    /**
     * Ajoute une arête au graphe.
     *
     * @param edge l'arête à ajouter
     */
    public void addEdge(Edge edge) {
        edges.add(edge);
    }

    /**
     * Supprime une arête du graphe.
     *
     * @param edge l'arête à supprimer
     */
    public void removeEdge(Edge edge) {
        edges.remove(edge);
    }

    /**
     * @return la liste d'arêtes du graphe
     */
    public List<Edge> getEdges() {
        return edges;
    }

    /**
     * Recherche un nœud par son identifiant.
     *
     * @param id identifiant recherché
     * @return le nœud si trouvé, sinon {@code null}
     */
    public Node getNodeById(int id) {
        for (Node node : nodes) {
            if (node.getId() == id) {
                return node;
            }
        }

        return null;
    }

    /**
     * Calcule la liste des voisins directs d'un nœud en parcourant
     * l'ensemble des arêtes. Cette méthode ne s'appuie pas sur
     * {@link #adjacencyList}.
     *
     * @param node nœud dont on veut les voisins
     * @return liste des nœuds voisins
     */
    public List<Node> getNeighbors(Node node) {
        List<Node> neighbors = new ArrayList<>();

        for (Edge edge : edges) {
            if (edge.getStart().equals(node)) {
                neighbors.add(edge.getEnd());
            }
            if (!edge.isDirected()
                    &&
                    edge.getEnd().equals(node)) {
                neighbors.add(edge.getStart());
            }
        }
        return neighbors;
    }

    /**
     * Retourne la liste d'adjacence (peut être vide si non initialisée).
     *
     * @return la map d'adjacence
     */
    public Map<Node, List<Edge>> getAdjacencyList() {
        return adjacencyList;
    }

    // TODO : Ajouter une classe travail Pathfinder

}
