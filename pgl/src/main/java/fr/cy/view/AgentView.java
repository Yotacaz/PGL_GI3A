package fr.cy.view;

import fr.cy.model.agent.Agent;
import fr.cy.model.agent.behaviour.properties.EmotionalState;
import javafx.scene.Cursor;
import javafx.scene.control.Tooltip;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

/**
 * Représentation visuelle d'un agent.
 *
 * Un AgentView est un petit cercle coloré selon l'état de l'agent :
 * - CALM      → jaune
 * - SELFISH   → orange
 * - PANICKING → rouge
 *
 * Un tooltip affiche le nom et l'état de l'agent au survol.
 */
public class AgentView extends Circle {

    private static final double RADIUS = 6;

    public AgentView(Agent agent) {
        super(RADIUS);

        // Couleur selon l'état
        setFill(colorForState(agent.getEmotionalState()));
        setStroke(Color.BLACK);
        setStrokeWidth(1);
        setCursor(Cursor.HAND);

        // Tooltip : infos de l'agent au survol
        Tooltip tooltip = new Tooltip(
            "Agent : " + agent.getName() +
            "\nÉtat : " + agent.getEmotionalState().name() +
            "\nStress : " + String.format("%.0f%%", agent.getStressLevel() * 100) +
            "\nVitesse max : " + agent.getMaxSpeed()
        );
        Tooltip.install(this, tooltip);
    }

    private Color colorForState(EmotionalState state) {
        return switch (state) {
            case CALM      -> Color.YELLOW;
            case SELFISH   -> Color.ORANGE;
            case PANICKING -> Color.RED;
        };
    }
}
