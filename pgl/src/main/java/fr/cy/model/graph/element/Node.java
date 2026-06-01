package fr.cy.model.graph.element;

import java.util.ArrayList;
import java.util.List;

import fr.cy.model.agent.behaviour.properties.AgentDecisionalProperties;

/**
 * Classe représentant un nœud dans le graphe.
 * 
 * Un nœud est un élément du graphe situé à une position donnée (x, y)
 * dans un plan. Il hérite des propriétés communes
 * de GraphElement comme l'identifiant, l'état d'incendie et la congestion.
 * 
 * @author GI3A
 * @version 1.0
 */
public class Node extends GraphElement {
    private final int id;

    /** Coordonnées X, Y */
    private double x;
    private double y;

    private boolean isExit;

    /** Liste des arêtes connectées à ce nœud */
    private final List<Edge> connectedEdges;
    /** Liste des arêtes sortantes */
    private final List<Edge> outgoingEdges;

    /**
     * Constructeur créant un nouveau nœud avec une position spécifiée.
     * 
     * @param id l'identifiant unique du nœud
     * @param x  la coordonnée X du nœud
     * @param y  la coordonnée Y du nœud
     */
    public Node(int id, double x, double y, double capacity) {
        super(id, capacity);
        this.id = id;
        this.x = x;
        this.y = y;

        connectedEdges = new ArrayList<>();
        outgoingEdges = new ArrayList<>();
    }

    @Override
    public double getScoreMultiplierForAgent(AgentDecisionalProperties agentState) {
        double scoreMultiplier = super.getScoreMultiplierForAgent(agentState);
        if (isExit()) {
            scoreMultiplier *= 10; //prefer exits
        }
        return scoreMultiplier;
    }

    /**
     * Retourne une représentation textuelle du nœud.
     * 
     * @return une chaîne de caractères contenant l'identifiant et les coordonnées
     *         du nœud
     */
    @Override
    public String toString() {

        return "Node{" +
                "id=" + getId() +
                ", x=" + x +
                ", y=" + y +
                ", exit=" + isExit +
                ", onFire=" + isOnFire() +
                "}";
    }

    /**
     * Récupère l'identifiant unique du nœud.
     * 
     * @return l'identifiant du nœud
     */
    public int getId() {
        return id;
    }

    /**
     * Récupère la coordonnée X du nœud.
     * 
     * @return la coordonnée X
     */
    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    /**
     * Récupère la coordonnée Y du nœud.
     * 
     * @return la coordonnée Y
     */
    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    /**
     * Vérifie si le nœud est une sortie.
     * 
     * @return true si le nœud est une sortie, false sinon
     */
    public boolean isExit() {
        return isExit;
    }

    /**
     * Définit si le nœud est une sortie.
     * 
     * @param exit true si le nœud est une sortie, false sinon
     */
    public void setExit(boolean exit) {
        isExit = exit;
    }

    @Override
    public List<GraphElement> getNeighbors() {
        return new ArrayList<>(connectedEdges);
    }

    public Edge getEdgeTo(Node neighbor) {
        for (Edge edge : connectedEdges) {
            if (edge.getOppositeNode(this).equals(neighbor)) {
                return edge;
            }
        }
        return null;
    }

    /**
     * Ajoute une arête connectée à ce nœud.
     * 
     * @param edge l'arête à ajouter
     */
    public void addEdge(Edge edge) {
        if (edge != null) {
            connectedEdges.add(edge);

            if (!edge.isDirected() || edge.getStart().equals(this)) {
                outgoingEdges.add(edge);
            }
        }
    }

    /**
     * Supprime une arête connectée à ce nœud
     * * @param edge l'arête à supprimer
     */
    public void removeEdge(Edge edge) {
        if (edge != null) {
            connectedEdges.remove(edge);
            outgoingEdges.remove(edge);
        }
    }

    /**
     * Return all outgoing edges from this node. For undirected edges, they are
     * considered outgoing from both nodes.
     * 
     * @return list of outgoing edges
     */
    public List<Edge> getOutgoingEdges() {
        return outgoingEdges;
    }

    /**
     * Retourne toutes les arêtes connectées à ce nœud.
     * 
     * @return liste des arêtes
     */
    public List<Edge> getEdges() {
        return new ArrayList<>(connectedEdges);
    }
}