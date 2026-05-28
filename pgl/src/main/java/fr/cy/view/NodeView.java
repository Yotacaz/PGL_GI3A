package fr.cy.view;

import fr.cy.model.graph.element.Node;
import javafx.scene.Cursor;
import javafx.scene.effect.DropShadow;
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
    private static final double RADIUS = 18;

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
        setStrokeWidth(2.5);
        setEffect(new DropShadow(10, Color.BLACK));

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

    /**
     * Met à jour la couleur du nœud selon son état actuel.
     * Appelé à chaque tick de la simulation.
     * - Rouge    : en feu
     * - Bleu→Orange : dégradé selon la congestion
     * - Vert     : sortie (inchangé)
     */
    public void refresh() {
        if (node.isOnFire()) {
            setFill(Color.RED);
            setStroke(Color.DARKRED);
        } else if (node.isExit()) {
            setFill(Color.LIMEGREEN);
            setStroke(Color.DARKGREEN);
        } else {
            Color base = Color.STEELBLUE.interpolate(Color.ORANGE, node.getCongestion());
            setFill(base);
            setStroke(Color.DARKBLUE);
        }
    }
}
