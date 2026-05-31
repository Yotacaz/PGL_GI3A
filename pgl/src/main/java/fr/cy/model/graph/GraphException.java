package fr.cy.model.graph;

/**
 * Exception métier du module graphe.
 * <p>
 * Elle est levée lorsqu'une opération demandée sur le graphe
 * est invalide au regard de son état courant.
 */
public class GraphException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    /**
     * Crée une exception de graphe avec un message.
     *
     * @param message description de l'erreur
     */
    public GraphException(String message) {
        super(message);
    }

    /**
     * Crée une exception de graphe avec un message et une cause.
     *
     * @param message description de l'erreur
     * @param cause   cause initiale
     */
    public GraphException(String message, Throwable cause) {
        super(message, cause);
    }
}
