package fr.cy.model.agent.behaviour;

/**
 * Custom exception class for errors related to agent behavior processing.
 * This can be used to signal issues in decision-making, action execution, or
 * any other aspect of the agent's behavior logic.
 */
public class AgentBehaviourException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public AgentBehaviourException(String message) {
        super(message);
    }

    public AgentBehaviourException(String message, Throwable cause) {
        super(message, cause);
    }
    
}
