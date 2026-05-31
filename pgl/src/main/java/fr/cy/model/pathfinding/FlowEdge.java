package fr.cy.model.pathfinding;

import java.io.Serializable;
import fr.cy.model.graph.element.Node;

/**
 * Représente une arête avec flux, capacité et coût pour l'algorithme min-cost
 * max-flow.
 *
 * @author GI3A
 * @version 1.0
 */
public class FlowEdge implements Serializable {
    private static final long serialVersionUID = 1L;
    private final Node from;
    private final Node to;
    private final double capacity;
    private final double cost;
    private double flow;

    /**
     * Constructeur d'une arête de flux.
     *
     * @param from     nœud source
     * @param to       nœud destination
     * @param capacity capacité de l'arête
     * @param cost     coût unitaire du flux
     */
    public FlowEdge(Node from, Node to, double capacity, double cost) {
        this.from = from;
        this.to = to;
        this.capacity = capacity;
        this.cost = cost;
        this.flow = 0;
    }

    /**
     * Retourne la capacité résiduelle (capacité - flux actuel).
     *
     * @return la capacité résiduelle
     */
    public double getResidualCapacity() {
        return capacity - flow;
    }

    /**
     * Ajoute du flux à cette arête.
     *
     * @param amount montant du flux à ajouter
     */
    public void addFlow(double amount) {
        this.flow += amount;
    }

    /**
     * Retrait du flux de cette arête (pour l'arête inverse).
     *
     * @param amount montant du flux à retirer
     */
    public void removeFlow(double amount) {
        this.flow -= amount;
    }

    // Getters
    public Node getFrom() {
        return from;
    }

    public Node getTo() {
        return to;
    }

    public double getCapacity() {
        return capacity;
    }

    public double getCost() {
        return cost;
    }

    public double getFlow() {
        return flow;
    }
}
