package fr.cy.model.graph.element;

import fr.cy.model.graph.GraphConfig;
import fr.cy.model.agent.Agent;
import java.util.*;

/**
 * Représente une arête entre deux nœuds du graphe.
 *
 * Une arête possède un nœud de départ et d'arrivée, une largeur et
 * une longueur, utilisées pour calculer la capacité. Elle peut être
 * orientée ou non et hérite des propriétés communes de
 * {@link GraphElement} (identifiant, état d'incendie, congestion).
 *
 * @author GI3A
 * @version 1.0
 */
public class Edge extends GraphElement {

    /** Nœud de départ/arrivée de l'arête */
    private final Node start;
    private final Node end;

    /** Indique si l'arête est dirigée */
    private boolean directed;

    /** Dimensions de l'arête (>= 0) */
    private double width;
    private double length;

    /** Liste d'agents présent dans l'arrête */
    private List<Agent> agents;

    /**
     * Constructeur simplifié utilisant les valeurs par défaut de
     * {@link GraphConfig} pour la largeur et la longueur.
     *
     * @param id       identifiant unique de l'arête
     * @param start    nœud de départ
     * @param end      nœud d'arrivée
     * @param directed true si l'arête est dirigée
     */
    public Edge(int id, Node start, Node end) {
        this(id, start, end, GraphConfig.DEFAULT_EDGE_DIRECTED, GraphConfig.DEFAULT_EDGE_WIDTH,
                GraphConfig.DEFAULT_EDGE_LENGTH);
    }

    /**
     * Constructeur complet d'une arête.
     *
     * @param id       identifiant unique de l'arête
     * @param start    nœud de départ
     * @param end      nœud d'arrivée
     * @param directed true si l'arête est dirigée
     * @param width    largeur de l'arête (valeur non-negative)
     * @param length   longueur de l'arête (valeur non-negative)
     */
    public Edge(int id, Node start, Node end, boolean directed, double width, double length) {

        super(id);

        this.start = start;
        this.end = end;

        this.directed = directed;

        setLength(length);
        setWidth(width);

        this.agents = new ArrayList<>();
    }

    /**
     * @return le nœud de départ
     */
    public Node getStart() {
        return start;
    }

    /**
     * @return le nœud d'arrivée
     */
    public Node getEnd() {
        return end;
    }

    public Node getOppositeNode(Node node) {
        if (node.equals(start)) {
            return end;
        }
        if (node.equals(end)) {
            return start;
        }

        return null;
    }

    /**
     * @return {@code true} si l'arête est dirigée
     */
    public boolean isDirected() {
        return directed;
    }

    /**
     * Définit si l'arête est dirigée.
     *
     * @param directed vrai pour arête dirigée
     */
    public void setDirected(boolean directed) {
        this.directed = directed;
    }

    /**
     * @return la longueur de l'arête
     */
    public double getLength() {
        return length;
    }

    /**
     * @return la largeur de l'arête
     */
    public double getWidth() {
        return width;
    }

    /**
     * Définit la longueur en veillant qu'elle soit non-négative.
     *
     * @param length la nouvelle longueur
     */
    public void setLength(double length) {
        this.length = Math.max(0, length);
    }

    /**
     * Définit la largeur en veillant qu'elle soit non-négative.
     *
     * @param width la nouvelle largeur
     */
    public void setWidth(double width) {
        this.width = Math.max(0, width);
    }

    /**
     * Calcule la capacité totale de l'arête en multipliant largeur et longueur.
     *
     * @return la capacité (width * length)
     */
    public double getCapacity() {
        return width * length;
    }

    /**
     * Calcule la surface occupée dans le couloir.
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
     * Calcule le ration d'occupation du couloir
     * 
     * @return ration de congestion
     */
    public double getCongestion() {
        return getOccupiedSpace() / getCapacity();
    }

    @Override
    public double getStressInducingFactor() {
        double stress = 0;

        stress += getCongestion() * 0.5;

        if (isOnFire()) {
            stress += 0.5;
        }

        return Math.min(stress, 1.0);
    }

    /**
     * Determine si un agent peut entrer dans le couloir selon l'espace restant.
     * 
     * @param agent Agent
     * @return {@code true} si l'agent peut entrer
     */
    public boolean canEnter(Agent agent) {
        return getOccupiedSpace() + agent.getSurfaceAreaTakenByAgent() <= getCapacity();
    }

    /**
     * Ajoute ou non un agent dans l'arrête
     * 
     * @param agent Agent
     * @return {@code true} si l'agent a été ajouté
     */
    public boolean addAgent(Agent agent) {
        if (canEnter(agent)) {
            agents.add(agent);

            return true;
        }
        return false;
    }

    /**
     * Supprime un agent de l'arrête
     * 
     * @param agent Agent
     */
    public void removeAgent(Agent agent) {
        agents.remove(agent);
    }

    /**
     * Représentation textuelle complète de l'arête.
     *
     * @return chaîne de description de l'arête
     */
    @Override
    public String toString() {

        return "Edge{" +
                "id=" + getId() +
                ", startNode=" + start.getId() +
                ", endNode=" + end.getId() +
                ", length=" + length +
                ", width=" + width +
                ", directed=" + directed +
                ", onFire=" + isOnFire() +
                ", congestion=" + getCongestion() +
                '}';
    }
}
