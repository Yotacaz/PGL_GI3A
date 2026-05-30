package fr.cy.model.graph.element;

import fr.cy.model.graph.GraphConfig;
import fr.cy.model.agent.Agent;
import fr.cy.model.fire.Fire;
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
    /** Direction du feu */
    private boolean burningFromStart = false;
    private boolean burningFromEnd = false;

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
     * Count agents currently on this edge that entered from the start node.
     *
     * <p>
     * This method inspects each {@link fr.cy.model.agent.Agent} present on the
     * edge and counts those whose last known node equals the edge's
     * {@code start} node. It is intended to provide a lightweight direction
     * estimate (number of agents moving from start → end) without storing
     * agent lists inside the edge.
     * </p>
     *
     * @return number of agents that most recently came from the start node
     */
    public int countAgentsGoingFromStartToEnd() {
        int nb = 0;
        for (Agent agent : getAgents()) {
            Node prev = agent.getPreviousOrCurrentNode();
            if (prev != null && prev.equals(start)) {
                nb++;
            }
        }
        return nb;
    }

    /**
     * Count agents currently on this edge that entered from the end node.
     *
     * <p>
     * Symmetric to {@link #countAgentsGoingFromStartToEnd()}: inspects
     * agents on the edge and returns how many have their previous/current
     * node equal to the edge's {@code end} node (estimate for end → start flow).
     * </p>
     *
     * @return number of agents that most recently came from the end node
     */
    public int countAgentsGoingFromEndToStart() {
        int nb = 0;
        for (Agent agent : getAgents()) {
            Node prev = agent.getPreviousOrCurrentNode();
            if (prev != null && prev.equals(end)) {
                nb++;
            }
        }
        return nb;
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

    /**
     * Compute the maximum allowed agent speed when entering this edge from
     * the given node.
     *
     * <p>
     * The base speed is computed by {@link #getMaxAgentSpeed()} which already
     * accounts for fires and congestion. For directed edges this base speed is
     * returned unchanged. For non-directed edges a simple counter-flow penalty is
     * applied: the method estimates how many agents are moving in the opposite
     * direction (using {@link #countAgentsGoingFromStartToEnd()} and
     * {@link #countAgentsGoingFromEndToStart()}) and reduces the speed by a
     * factor proportional to the ratio of opposite-flow agents.
     * </p>
     *
     * <p>
     * Current penalty formula: speed * (1 - 0.5 * oppositeRatio), where
     * {@code oppositeRatio} = opposite / (same + opposite). The coefficient
     * {@code 0.5} is a tunable penalty constant.
     * </p>
     *
     * @param fromNode node from which the agent enters this edge (must be
     *                 either the edge's {@code start} or {@code end})
     * @return maximum agent speed allowed when entering from {@code fromNode}
     * @throws IllegalArgumentException if {@code fromNode} is not connected to
     *                                  this edge
     */
    public double getMaxAgentSpeedInDirection(Node fromNode) {

        double speed = getMaxAgentSpeed();

        if (!directed) { // only apply counter-flow penalty on non-directed edges
            int sameDirection;
            int oppositeDirection;

            if (fromNode.equals(start)) {
                sameDirection = countAgentsGoingFromStartToEnd();
                oppositeDirection = countAgentsGoingFromEndToStart();
            } else if (fromNode.equals(end)) {
                sameDirection = countAgentsGoingFromEndToStart();
                oppositeDirection = countAgentsGoingFromStartToEnd();
            } else {
                throw new IllegalArgumentException("Le nœud n'appartient pas à l'arête");
            }

            int total = sameDirection + oppositeDirection;
            if (total == 0) {
                return speed;
            }

            double oppositeRatio = oppositeDirection / (double) total;

            double counterFlowFactor = 1.0 - 0.5 * oppositeRatio; // 0.5 = penalty coefficient

            return speed * counterFlowFactor;
        }

        return speed;
    }

    @Override
    public double getStressInducingFactor() {
        double stress = 0;

        stress += getCongestion() * 0.5;

        if (isOnFire()) {
            stress += 0.2 * getFire().getIntensity();
        }

        return Math.min(stress, 1.0);
    }

    public boolean isCongested() {
        return getCongestion() > 0.7;
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

    public boolean isBurningFromStart() {
        return burningFromStart;
    }

    public boolean isBurningFromEnd() {
        return burningFromEnd;
    }

    /**
     * Allume l'arête depuis un nœud spécifique.
     */
    public void igniteFrom(Node source, Fire newFire) {
        if (!isOnFire()) {
            setFire(newFire);
        }
        // On enregistre d'où viennent les flammes
        if (source.equals(start)) {
            burningFromStart = true;
        } else if (source.equals(end)) {
            burningFromEnd = true;
        }
    }

    public double getBurnedDistance() {
        if (!isOnFire()) {
            return 0.0;
        }

        return getFire().getBurningTicks() * getFire().getSpreadRate();
    }

    public boolean isFullyBurned() {
        if (!isOnFire()) {
            return false;
        }

        double distance = getBurnedDistance();

        /** Cas ou les flammes proviennent des deux Nodes */
        if (burningFromEnd && burningFromStart) {
            return (distance * 2) >= length;
        }
        return distance >= length;
    }

    /**
     * Calcule le pourcentage de l'arête recouvert par les flammes (de 0.0 à 1.0).
     * 
     * @return Pourcentage
     */
    public double getBurnPercentage() {
        if (!isOnFire()) {
            return 0.0;
        }

        // Distance = Temps (ticks) * Vitesse de propagation
        double burnedDistance = getFire().getBurningTicks() * getFire().getSpreadRate();

        return Math.min(1.0, burnedDistance / getLength());
    }
}
