package fr.cy.view;

import fr.cy.model.graph.element.Node;
import javafx.scene.Cursor;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

/**
 * Représentation visuelle d'un nœud du graphe.
 *
 * Un NodeView est un cercle JavaFX positionné aux coordonnées (x, y)
 * du nœud modèle. Le cercle est centré sur ces coordonnées.
 * On peut le déplacer à la souris (drag).
 */
public class NodeView extends Circle {

    /** Rayon du cercle en pixels */
    private static final double RADIUS = 15;

    /** Le nœud du modèle associé à cette vue */
    private final Node node;

    /** Mémorise la position de la souris au moment où on appuie */
    private double mouseAnchorX;
    private double mouseAnchorY;

    public NodeView(Node node) {
        // Circle(centerX, centerY, radius)
        super(node.getX(), node.getY(), RADIUS);
        this.node = node;

        // Style visuel : vert si sortie, bleu sinon
        if (node.isExit()) {
            setFill(Color.LIMEGREEN);
            setStroke(Color.DARKGREEN);
        } else {
            setFill(Color.STEELBLUE);
            setStroke(Color.DARKBLUE);
        }
        setStrokeWidth(2);

        // Curseur main quand on survole le nœud
        setCursor(Cursor.HAND);

        // --- Drag ---

        // 1. Au moment où on appuie : mémoriser la position de la souris
        setOnMousePressed(event -> {
            mouseAnchorX = event.getSceneX();
            mouseAnchorY = event.getSceneY();
            setCursor(Cursor.CLOSED_HAND); // curseur "main fermée" pendant le drag
        });

        // 2. Pendant le drag : déplacer le cercle du même delta que la souris
        setOnMouseDragged(event -> {
            double dx = event.getSceneX() - mouseAnchorX;
            double dy = event.getSceneY() - mouseAnchorY;

            setCenterX(getCenterX() + dx);
            setCenterY(getCenterY() + dy);

            // Mettre à jour l'ancre pour le prochain événement
            mouseAnchorX = event.getSceneX();
            mouseAnchorY = event.getSceneY();
        });

        // 3. Quand on relâche : revenir au curseur main
        setOnMouseReleased(event -> setCursor(Cursor.HAND));
    }

    public Node getNode() {
        return node;
    }
}
