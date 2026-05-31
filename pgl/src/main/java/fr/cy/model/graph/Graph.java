package fr.cy.model.graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.io.*;

import fr.cy.model.graph.element.Edge;
import fr.cy.model.graph.element.Node;
import fr.cy.model.pathfinding.*;

/**
 * Représente un graphe composé de nœuds et d'arêtes.
 *
 * @author GI3A
 * @version 1.0
 */
public class Graph implements Serializable{
    private static final long serialVersionUID = 1L;

    /** Liste de tous les nœuds et edges du graphe */
    private final List<Node> nodes;
    private final List<Edge> edges;

    /** Liste d'adjacence */
    private final Map<Node, List<Edge>> adjacencyList;

    private final IdManager nodeIdManager;
    private final IdManager edgeIdManager;

    /**
     * Crée un graphe vide.
     */
    public Graph() {
        this.edges = new ArrayList<>();
        this.nodes = new ArrayList<>();

        this.adjacencyList = new HashMap<>();

        this.nodeIdManager = new IdManager();
        this.edgeIdManager = new IdManager();
    }

    /**
     * Give an update to each elements that need it (eg: cached values like stress)
     */
    public void tick() {
        updateStressInducedByElements();
    }

    /**
     * Crée un noeud à une position spécifique dans le graphe
     * 
     * @param x        coordonnée x
     * @param y        coordonnée y
     * @param capacité maximale
     * @return le noeud créé
     */
    public Node createNode(double x, double y, double capacity) {
        Node node = new Node(nodeIdManager.generateId(), x, y, capacity);
        addNode(node);

        return node;
    }

    /**
     * Crée un noeud à une position spécifique dans le graphe
     * 
     * @param x coordonnée x
     * @param y coordonnée y
     * @return le noeud créé
     */
    public Node createNode(double x, double y) {
        return createNode(x, y, GraphConfig.DEFAULT_NODE_CAPACITY);
    }

    /**
     * Crée une arrête entre deux noeuds spécifiés
     * 
     * @param startNode noeud de départ
     * @param endNode   noeud d'arrivée
     * @return l'arrête créée
     */
    public Edge createEdge(Node startNode, Node endNode) {
        Edge edge = new Edge(edgeIdManager.generateId(), startNode, endNode);
        addEdge(edge);

        return edge;
    }

    /**
     * Crée une arrête spécifiée par deux noeuds, largeur, longueur et sa
     * possibilité à être orienté.
     * 
     * @param startNode
     * @param endNode
     * @param length
     * @param width
     * @param directed
     * @return l'arrête créée
     */
    public Edge createEdge(Node startNode, Node endNode, double length, double width, boolean directed) {
        Edge edge = new Edge(edgeIdManager.generateId(), startNode, endNode, directed, width, length);

        addEdge(edge);
        return edge;
    }

    /**
     * Ajoute un nœud au graphe.
     *
     * @param node le nœud à ajouter
     */
    public void addNode(Node node) {
        nodes.add(node);
        adjacencyList.put(node, new ArrayList<>());
    }

    /**
     * Ajoute une arête au graphe.
     *
     * @param edge l'arête à ajouter
     */
    public void addEdge(Edge edge) {
        if (edge == null) {
            throw new GraphException("Impossible d'ajouter une arête nulle.");
        }

        if (!adjacencyList.containsKey(edge.getStart()) || !adjacencyList.containsKey(edge.getEnd())) {
            throw new GraphException("Les deux nœuds de l'arête doivent appartenir au graphe.");
        }

        edges.add(edge);
        adjacencyList.get(edge.getStart()).add(edge);
        edge.getStart().addEdge(edge);

        if (!edge.isDirected()) {
            adjacencyList.get(edge.getEnd()).add(edge);
            edge.getEnd().addEdge(edge);
        } else {
            edge.getEnd().addEdge(edge);
        }
    }

    /**
     * Supprime un nœud et toutes les arêtes qui y sont connectées.
     *
     * @param node le nœud à supprimer
     */
    public void removeNode(Node node) {
        List<Edge> edgesToRemove = new ArrayList<>(adjacencyList.get(node));

        for (Edge edge : edgesToRemove) {
            removeEdge(edge);
        }

        adjacencyList.remove(node);
        nodes.remove(node);
        nodeIdManager.releaseId(node.getId());
    }

    /**
     * Supprime une arête du graphe.
     *
     * @param edge l'arête à supprimer
     */
    public void removeEdge(Edge edge) {
        edges.remove(edge);

        if (adjacencyList.containsKey(edge.getStart())) {
            adjacencyList.get(edge.getStart()).remove(edge);
        }
        if (adjacencyList.containsKey(edge.getEnd())) {
            adjacencyList.get(edge.getEnd()).remove(edge);
        }

        edge.getStart().removeEdge(edge);
        edge.getEnd().removeEdge(edge);

        edgeIdManager.releaseId(edge.getId());
    }

    /**
     * @return la liste de nœuds du graphe
     */
    public List<Node> getNodes() {
        return nodes;
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
     * Retourne la liste des voisins d'un nœud
     *
     * @param node nœud dont on veut les voisins
     * @return liste des nœuds voisins
     */
    public List<Node> getNeighbors(Node node) {
        List<Node> neighbors = new ArrayList<>();

        List<Edge> connectedEdges = adjacencyList.get(node);

        for (Edge edge : connectedEdges) {
            neighbors.add(edge.getOppositeNode(node));
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

    public List<Edge> getAdjacentEdges(Node node) {
        return adjacencyList.getOrDefault(node, new ArrayList<>());
    }

    /**
     * Update the stress induced by each element and cache it. This method should be
     * called at each tick
     */
    private void updateStressInducedByElements() {
        // update stress generated by each element and cache it, then update total
        // stress induced including neighbors
        for (Node element : nodes) {
            element.updateStressGeneratedByThisElement();
        }
        for (Edge element : edges) {
            element.updateStressGeneratedByThisElement();
        }
        for (Node element : nodes) {
            element.updateCachedTotalStressInducedIncludingNeighbors();
        }
        for (Edge element : edges) {
            element.updateCachedTotalStressInducedIncludingNeighbors();
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Graph{\n");

        sb.append("  Nodes:\n");
        for (Node node : nodes) {
            sb.append("    ").append(node).append("\n");
        }

        sb.append("  Edges:\n");
        for (Edge edge : edges) {
            sb.append("    ").append(edge).append("\n");
        }

        sb.append("}");
        return sb.toString();
    }

    public List<Node> getExits() {
        List<Node> exits = new ArrayList<>();

        for (Node node : nodes) {
            if (node.isExit()) {
                exits.add(node);
            }
        }

        return exits;
    }

    public static void main(String[] args) {
        Graph graph = new Graph();
        Node n1 = graph.createNode(5, 5);
        Node n2 = graph.createNode(15, 5);
        Node n3 = graph.createNode(15, 10);
        Node n4 = graph.createNode(20, 5);
        n3.setExit(true);
        graph.createEdge(n1, n2);
        graph.createEdge(n2, n3);
        graph.createEdge(n2, n4);

    }
}
