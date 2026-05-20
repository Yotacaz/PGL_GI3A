package fr.cy.model.graph;

import java.util.Objects;

/**
 * Classe abstraite représentant un élément du graphe.
 * 
 * Cette classe est la base pour tous les éléments du graphe tels que
 * les nœuds et les arêtes. Elle gère les propriétés communes incluant
 * l'identifiant unique, l'état d'incendie et le niveau de congestion.
 * 
 * @author GI3A
 * @version 1.0
 */
public abstract class GraphElement {
    /** Identifiant unique de l'élément du graphe */
    private final int id;

    /** Indicateur du state d'incendie de l'élément */
    private boolean onFire;

    /** Niveau de congestion de l'élément (valeur entre 0 et 1) */
    private double congestion;

    /**
     * Constructeur initialisant un élément du graphe.
     * 
     * @param id l'identifiant unique de l'élément
     */
    protected GraphElement(int id) {
        this.id = id;
        this.onFire = false;
        this.congestion = 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }

        if (!(o instanceof Node)) {
            return false;
        }

        GraphElement element = (GraphElement) o;

        return getId() == element.getId();
    }

    /**
     * Récupère l'identifiant unique de cet élément du graphe.
     * 
     * @return l'identifiant de l'élément
     */
    public int getId() {
        return id;
    }

    /**
     * Vérifie si cet élément est actuellement en feu.
     * 
     * @return {@code true} si l'élément est en feu, {@code false} sinon
     */
    public boolean isOnFire() {
        return onFire;
    }

    /**
     * Récupère le niveau de congestion de cet élément.
     * 
     * @return le niveau de congestion
     */
    public double getCongestion() {
        return congestion;
    }

    /**
     * Définit le niveau de congestion de cet élément.
     * La valeur est limitée entre 0 et 1.
     * 
     * @param congestion le nouveau niveau de congestion
     */
    public void setCongestion(double congestion) {
        this.congestion = Math.max(0, Math.min(1, congestion));
    }

}
