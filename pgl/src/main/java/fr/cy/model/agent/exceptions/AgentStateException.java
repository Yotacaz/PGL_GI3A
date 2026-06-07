package fr.cy.model.agent.exceptions;

public class AgentStateException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public AgentStateException(String message) {
        super(message);
    }

    public AgentStateException(String message, Throwable cause) {
        super(message, cause);
    }
}
