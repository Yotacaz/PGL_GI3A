package fr.cy.view;

import fr.cy.model.agent.Agent;
import fr.cy.model.agent.behaviour.properties.EmotionalState;
import fr.cy.model.graph.element.Node;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.List;

/**
 * Panneau d'informations d'un nœud.
 *
 * S'affiche à droite de l'écran quand on clique sur un nœud.
 * Contient des Labels mis à jour à chaque clic.
 */
public class NodeInfoPanel extends VBox {

    // --- Labels ---
    private final Label titleLabel        = new Label("Cliquez sur un nœud");
    private final Label coordLabel        = new Label();
    private final Label exitLabel         = new Label();
    private final Label edgesLabel        = new Label();
    private final Label separator1        = new Label("── Occupation ──");
    private final Label agentsLabel       = new Label();
    private final Label capacityLabel     = new Label();
    private final Label occupiedLabel     = new Label();
    private final Label congestionLabel   = new Label();
    private final Label accessibleLabel   = new Label();
    private final Label separator2        = new Label("── Agents ──");
    private final Label avgSpeedLabel     = new Label();
    private final Label avgStressLabel    = new Label();
    private final Label dominantStateLabel= new Label();
    private final Label separator3        = new Label("── Stress global ──");
    private final Label globalStressLabel = new Label();
    private final Label separator4        = new Label("── Feu ──");
    private final Label fireLabel         = new Label();
    private final Label fireIntensityLabel= new Label();
    private final Label fireSmokeLabel    = new Label();
    private final Label fireSpreadLabel   = new Label();
    private final Label fireTicksLabel    = new Label();

    public NodeInfoPanel() {
        setSpacing(6);
        setPadding(new Insets(15));
        setPrefWidth(230);
        setStyle("-fx-background-color: #FAFAFA; -fx-border-color: #CCCCCC; -fx-border-width: 0 0 0 1;");

        // Titre en gras
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 14));

        // Style des séparateurs de section
        for (Label sep : new Label[]{separator1, separator2, separator3, separator4}) {
            sep.setFont(Font.font("System", FontWeight.BOLD, 11));
            sep.setTextFill(Color.GRAY);
        }

        getChildren().addAll(
            titleLabel,
            new Separator(),
            coordLabel, exitLabel, edgesLabel,
            separator1,
            agentsLabel, capacityLabel, occupiedLabel, congestionLabel, accessibleLabel,
            separator2,
            avgSpeedLabel, avgStressLabel, dominantStateLabel,
            separator3,
            globalStressLabel,
            separator4,
            fireLabel, fireIntensityLabel, fireSmokeLabel, fireSpreadLabel, fireTicksLabel
        );

        // Cacher les infos feu par défaut
        setFireVisible(false);
    }

    /**
     * Met à jour le panneau avec les informations du nœud cliqué.
     *
     * @param node le nœud sur lequel on a cliqué
     */
    public void display(Node node) {
        List<Agent> agents = node.getAgents();

        // --- Infos générales ---
        titleLabel.setText("Nœud #" + node.getId());
        coordLabel.setText("Position : (" + (int)node.getX() + ", " + (int)node.getY() + ")");
        exitLabel.setText("Sortie : " + (node.isExit() ? "Oui ✅" : "Non"));
        edgesLabel.setText("Arêtes connectées : " + node.getEdges().size());

        // --- Occupation ---
        agentsLabel.setText("Agents présents : " + agents.size());
        capacityLabel.setText("Capacité : " + String.format("%.1f", node.getCapacity()));
        occupiedLabel.setText("Espace occupé : " + String.format("%.1f", node.getOccupiedSpace()));
        congestionLabel.setText("Congestion : " + String.format("%.0f%%", node.getCongestion() * 100));
        accessibleLabel.setText("Accessible : " + (!node.isFull() ? "Oui ✅" : "Non — plein ❌"));

        // --- Agents ---
        if (agents.isEmpty()) {
            avgSpeedLabel.setText("Vitesse moy. : —");
            avgStressLabel.setText("Stress moy. : —");
            dominantStateLabel.setText("État dominant : —");
        } else {
            double avgSpeed = agents.stream()
                    .mapToDouble(Agent::getMaxSpeed)
                    .average().orElse(0);
            avgSpeedLabel.setText("Vitesse moy. : " + String.format("%.1f", avgSpeed));

            double avgStress = agents.stream()
                    .mapToDouble(Agent::getStressLevel)
                    .average().orElse(0);
            avgStressLabel.setText("Stress moy. : " + String.format("%.0f%%", avgStress * 100));

            EmotionalState dominant = getDominantState(agents);
            dominantStateLabel.setText("État dominant : " + dominant.name());
        }

        // --- Stress global ---
        double globalStress = node.getTotalStressInducedIncludingNeighbors();
        globalStressLabel.setText("Stress (+ voisins) : " + String.format("%.0f%%", globalStress * 100));

        // --- Feu ---
        if (node.isOnFire()) {
            setFireVisible(true);
            fireLabel.setText("🔥 EN FEU");
            fireLabel.setTextFill(Color.RED);
            fireIntensityLabel.setText("Intensité : " + String.format("%.2f", node.getFire().getIntensity()));
            fireSmokeLabel.setText("Fumée : " + String.format("%.2f", node.getFire().getSmokeLevel()));
            fireSpreadLabel.setText("Propagation : " + String.format("%.2f", node.getFire().getSpreadRate()));
            fireTicksLabel.setText("Brûle depuis : " + node.getFire().getBurningTicks() + " ticks");
        } else {
            setFireVisible(true);
            fireLabel.setText("Pas de feu ✅");
            fireLabel.setTextFill(Color.GREEN);
            fireIntensityLabel.setVisible(false);
            fireSmokeLabel.setVisible(false);
            fireSpreadLabel.setVisible(false);
            fireTicksLabel.setVisible(false);
        }
    }

    /**
     * Détermine l'état le plus fréquent parmi les agents.
     */
    private EmotionalState getDominantState(List<Agent> agents) {
        int calm = 0, selfish = 0, panicking = 0;
        for (Agent a : agents) {
            switch (a.getState()) {
                case CALM -> calm++;
                case SELFISH -> selfish++;
                case PANICKING -> panicking++;
            }
        }
        if (panicking >= calm && panicking >= selfish) return EmotionalState.PANICKING;
        if (selfish >= calm) return EmotionalState.SELFISH;
        return EmotionalState.CALM;
    }

    /** Affiche ou cache les labels liés au feu. */
    private void setFireVisible(boolean visible) {
        fireLabel.setVisible(visible);
        fireIntensityLabel.setVisible(visible);
        fireSmokeLabel.setVisible(visible);
        fireSpreadLabel.setVisible(visible);
        fireTicksLabel.setVisible(visible);
    }
}
