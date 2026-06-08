package fr.cy.model.agent.exceptions;

/**
 * Custom exception class for errors related to invalid agent states.
 * 
 * <p>This exception is thrown when an agent is found in an inconsistent or
 * invalid state, such as being on an edge when expected to be on a node,
 * having null references when they should be populated, or other state-related
 * inconsistencies that violate the agent's expected behavior.</p>
 */
public class AgentStateException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public AgentStateException(String message) {
        super(message);
    }

    public AgentStateException(String message, Throwable cause) {
        super(message, cause);
    }
}
