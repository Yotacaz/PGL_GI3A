package fr.cy.model.graph;

import java.util.Objects;

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

    /**
     * Constructeur créant un nouveau nœud avec une position spécifiée.
     * 
     * @param id l'identifiant unique du nœud
     * @param x  la coordonnée X du nœud
     * @param y  la coordonnée Y du nœud
     */
    public Node(int id, double x, double y) {
        super(id);
        this.id = id;
        this.x = x;
        this.y = y;
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
                ", congestion=" + getCongestion() +
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

    /**
     * Récupère la coordonnée Y du nœud.
     * 
     * @return la coordonnée Y
     */
    public double getY() {
        return y;
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
}