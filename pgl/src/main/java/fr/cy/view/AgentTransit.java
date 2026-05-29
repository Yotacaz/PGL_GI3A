package fr.cy.view;

import fr.cy.model.agent.Agent;
import fr.cy.model.graph.element.Node;

public class AgentTransit {

    public final Agent  agent;
    public final Node   from;
    public final Node   to;

    public double progress;

    // Vitesse de base : 0.2 par tick
    // Vitesse 1 → 0.20/tick → 5 ticks pour traverser
    // Vitesse 2 → 0.40/tick → 3 ticks pour traverser
    // Vitesse 3 → 0.60/tick → 2 ticks pour traverser
    // Le feu se propage en ~5 ticks (P=0.2/tick) → les agents vitesse 2+ s'échappent
    public static final double BASE_STEP = 0.20;
    public final double step;

    public AgentTransit(Agent agent, Node from, Node to) {
        this.agent    = agent;
        this.from     = from;
        this.to       = to;
        this.progress = 0.0;
        this.step     = BASE_STEP * Math.max(1, agent.getMaxSpeed());
    }

    public boolean isCompleted() {
        return progress >= 1.0;
    }
}
