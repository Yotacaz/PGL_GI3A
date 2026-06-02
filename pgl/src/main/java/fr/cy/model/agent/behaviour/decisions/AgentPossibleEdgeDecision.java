package fr.cy.model.agent.behaviour.decisions;

public enum AgentPossibleEdgeDecision {
    /** Agent decides to backtrack to the previous node */
    BACKTRACK, 
    /** Agent decides to continue on the current edge */
    CONTINUE,
    /** Agent decides to wait */
    WAIT;
    //TODO
}
