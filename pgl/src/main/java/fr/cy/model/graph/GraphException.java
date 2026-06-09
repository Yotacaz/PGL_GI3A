package fr.cy.model.graph;

/**
 * Exception for the graph module.
 * <p>
 * It is thrown when an operation requested on the graph
 * is invalid with regard to its current state.
 */
public class GraphException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    /**
     * Creates a graph exception with a message.
     *
     * @param message description of the error
     */
    public GraphException(String message) {
        super(message);
    }

    /**
     * Creates a graph exception with a message and a cause.
     *
     * @param message description of the error
     * @param cause   initial cause
     */
    public GraphException(String message, Throwable cause) {
        super(message, cause);
    }
}
