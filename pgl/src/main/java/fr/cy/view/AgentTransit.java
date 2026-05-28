package fr.cy.view;

import fr.cy.model.agent.Agent;
import fr.cy.model.graph.element.Node;

/**
 * Représente un agent en déplacement sur une arête.
 *
 * Un agent en transit a quitté son nœud d'origine mais n'est pas encore
 * arrivé à destination. Sa position est interpolée selon la progression.
 */
public class AgentTransit {

    public final Agent  agent;
    public final Node   from;
    public final Node   to;

    /**
     * Progression sur l'arête : 0.0 = départ, 1.0 = arrivée.
     * Avance de STEP à chaque tick.
     */
    public double progress;

    public static final double STEP = 0.33; // 3 ticks pour traverser une arête

    public AgentTransit(Agent agent, Node from, Node to) {
        this.agent    = agent;
        this.from     = from;
        this.to       = to;
        this.progress = 0.0;
    }

    /** Retourne true si l'agent a atteint le nœud de destination. */
    public boolean isCompleted() {
        return progress >= 1.0;
    }
}
