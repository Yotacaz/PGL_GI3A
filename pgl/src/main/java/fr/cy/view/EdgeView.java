package fr.cy.view;

import fr.cy.model.graph.element.Edge;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;

/**
 * Représentation visuelle d'une arête du graphe.
 *
 * Un EdgeView est une ligne JavaFX tracée entre les coordonnées
 * du nœud de départ et du nœud d'arrivée de l'arête modèle.
 */
public class EdgeView extends Line {

    /** L'arête du modèle associée à cette vue */
    private final Edge edge;

    public EdgeView(Edge edge) {
        // Line(startX, startY, endX, endY)
        super(
            edge.getStart().getX(), edge.getStart().getY(),
            edge.getEnd().getX(),   edge.getEnd().getY()
        );
        this.edge = edge;

        // Style visuel
        setStroke(Color.GRAY);
        setStrokeWidth(3);
    }

    public Edge getEdge() {
        return edge;
    }
}
