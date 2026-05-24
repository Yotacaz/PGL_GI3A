package fr.cy.model.graph.element;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import fr.cy.model.agent.Agent;
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

    private final List<Agent> agents;
    private double capacity;

    /** Total stress induced by this element and its neighbors */
    private double totalStressInducedIncludingNeighbors = 0;

    private Fire fire;

    /**
     * Constructeur initialisant un élément du graphe.
     * 
     * @param id l'identifiant unique de l'élément
     */
    protected GraphElement(int id, double capacity) {
        this.id = id;
        this.agents = new ArrayList<>();
        this.capacity = Math.max(0.1, capacity);
        removeFire();
    }

    @Override
    public abstract double getStressInducingFactor();

    /***
     * Determine la liste des elements voisins
     * 
     * @return liste de vosions
     */
    public abstract List<GraphElement> getNeighbors();

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

    public List<Agent> getAgents() {
        return agents;
    }

    /**
     * Ajoute un agent si la capacité le permet
     * 
     * @param a Agent
     */
    public void addAgent(Agent a) {
        if (!isFull()) {
            agents.add(a);
        }
    }

    /**
     * Enleve un agent
     * 
     * @param a agent
     */
    public void removeAgent(Agent a) {
        agents.remove(a);
    }

    public double getCapacity() {
        return capacity;
    }

    /**
     * Determine espace occupée par les agents
     * 
     * @return surface occupée
     */
    public double getOccupiedSpace() {
        double occupied = 0;
        for (Agent agent : agents) {
            occupied += agent.getSurfaceAreaTakenByAgent();
        }

        return occupied;
    }

    /**
     * Calcule la congestion entre 0 et 1 selon les agenrs présents
     * 
     * @return congestion
     */
    public double getCongestion() {
        return Math.min(getOccupiedSpace() / getCapacity(), 1);
    }

    /**
     * Determine si l'élement est full
     * 
     * @return true si oui
     */
    public boolean isFull() {
        return getOccupiedSpace() >= capacity;
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
