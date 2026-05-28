package fr.cy.model.graph.element;

import fr.cy.model.graph.GraphConfig;
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

        super(id, width * length);

        this.start = start;
        this.end = end;

        this.directed = directed;

        setLength(length);
        setWidth(width);
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
    @Override
    public double getCapacity() {
        return width * length;
    }

    /**
     * Calcule la vitesse maximale de déplacement des agents dans cette arête
     * en fonction de la congestion et de la longueur.
     * 
     * @return la vitesse maximale calculée
     */
    public double getMaxAgentSpeed() {
        if (isOnFire()) {
            return 0.0;
        }

        double congestionFactor = 1.0 - getCongestion();

        double calculatedSpeed = GraphConfig.DEFAULT_EDGE_MAX_AGENT_SPEED * congestionFactor;

        return Math.max(calculatedSpeed, 0.0);
    }

    public double getMaxAgentSpeedInDirection(Node fromNode) {  //TODO
        if (directed) {
            if (fromNode.equals(start)) {
                return getMaxAgentSpeed();
            } else if (fromNode.equals(end)) {
                return getMaxAgentSpeed(); 
            } else {
                throw new IllegalArgumentException("Le nœud spécifié n'est pas connecté à cette arête");
            }
        } else {
            return getMaxAgentSpeed();
        }
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

    @Override
    public List<GraphElement> getNeighbors() {
        List<GraphElement> neighbors = new ArrayList<>();

        neighbors.add(start);
        neighbors.add(end);

        return neighbors;
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
                ", maxAgentSpeed=" + String.format("%.2f", getMaxAgentSpeed()) +
                ", directed=" + directed +
                ", onFire=" + isOnFire() +
                ", congestion=" + String.format("%.2f", getCongestion()) +
                '}';
    }
}
