package fr.cy.model.pathfinding;

import fr.cy.model.graph.element.Node;

/**
 * Représente une arête avec flux, capacité et coût pour l'algorithme min-cost max-flow.
 *
 * @author GI3A
 * @version 1.0
 */
public class FlowEdge {
    
    private final Node to;
    private final double capacity;
    private final double cost;
    private double flow;

    /**
     * Constructeur d'une arête de flux.
     *
     *      * @param to       nœud destination
     * @param capacity capacité de l'arête
     * @param cost     coût unitaire du flux
     */
    public FlowEdge( Node to, double capacity, double cost) {
        
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

    

    // Getters
    

    public Node getTo() {
        return to;
    }

    public double getCost() {
        return cost;
    
    }
}
