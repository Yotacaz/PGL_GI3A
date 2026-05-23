package fr.cy.model.graph.element;

import java.util.Objects;

import fr.cy.model.fire.Fire;
import fr.cy.model.stress.StressInducing;

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
public abstract class GraphElement implements StressInducing {
    /** Identifiant unique de l'élément du graphe */
    private final int id;

    /** Total stress induced by this element and its neighbors */
    private double totalStressInducedIncludingNeighbors = 0;

    private Fire fire;

    /**
     * Constructeur initialisant un élément du graphe.
     * 
     * @param id l'identifiant unique de l'élément
     */
    protected GraphElement(int id) {
        this.id = id;
        removeFire();
    }

    /**ça
     * Le stress ne dépend pas de la même chose dans un node ou arrête étant donné
     * qu'il n'y a pas de congestion dans les noeuds
     */
    @Override
    public abstract double getStressInducingFactor();

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }

        if (!(o instanceof GraphElement)) {
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
        return fire != null;
    }

    public Fire getFire() {
        return fire;
    }

    public void setFire(Fire fire) {
        this.fire = fire;
    }

    public void removeFire() {
        this.fire = null;
    }

    /**
     * @return the total stress induced by this element and its neighbors, which is
     *         a value between 0 and 1
     */
    public double getTotalStressInducedIncludingNeighbors() {
        return totalStressInducedIncludingNeighbors;
    }

    /**
     * Set the total stress induced by this element and its neighbors.
     * 
     * @param totalStressInducedIncludingNeighbors the new total stress induced by
     *                                             this element and its neighbors,
     *                                             which should be a value between 0
     *                                             and 1
     */
    public void setTotalStressInducedIncludingNeighbors(double totalStressInducedIncludingNeighbors) {
        if (totalStressInducedIncludingNeighbors < 0 || totalStressInducedIncludingNeighbors > 1) {
            throw new IllegalArgumentException("Total stress induced including neighbors must be between 0 and 1");
        }
        this.totalStressInducedIncludingNeighbors = totalStressInducedIncludingNeighbors;
    }

}
