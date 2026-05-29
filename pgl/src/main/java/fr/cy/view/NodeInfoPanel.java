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
 * Panneau d'informations d'un nœud — thème sombre moderne.
 * S'affiche à droite quand on clique sur un nœud.
 */
public class NodeInfoPanel extends VBox {

    // ---- Couleurs du thème ----
    private static final String BG          = "#0F0F1A";
    private static final String HEADER_BG   = "#141428";
    private static final String SECTION_BG  = "#1C1C32";
    private static final String ACCENT      = "#5B7FFF";
    private static final String TEXT        = "#E8E8FF";
    private static final String TEXT_DIM    = "#888AB0";
    private static final String GREEN       = "#4ADE80";
    private static final String ORANGE      = "#FFA94D";
    private static final String RED         = "#FF6B6B";

    // ---- Labels ----
    private final Label titleLabel         = new Label("Cliquez sur un nœud");
    private final Label coordLabel         = new Label();
    private final Label exitLabel          = new Label();
    private final Label edgesLabel         = new Label();
    private final Label agentsLabel        = new Label();
    private final Label capacityLabel      = new Label();
    private final Label occupiedLabel      = new Label();
    private final Label congestionLabel    = new Label();
    private final Label accessibleLabel    = new Label();
    private final Label avgSpeedLabel      = new Label();
    private final Label avgStressLabel     = new Label();
    private final Label dominantStateLabel = new Label();
    private final Label globalStressLabel  = new Label();
    private final Label fireLabel          = new Label();
    private final Label fireIntensityLabel = new Label();
    private final Label fireSmokeLabel     = new Label();
    private final Label fireSpreadLabel    = new Label();
    private final Label fireTicksLabel     = new Label();

    public NodeInfoPanel() {
        setPrefWidth(250);
        setSpacing(0);
        setStyle("-fx-background-color: " + BG + "; -fx-border-color: " + ACCENT + "; -fx-border-width: 0 0 0 3;");

        // Titre
        titleLabel.setMaxWidth(Double.MAX_VALUE);
        titleLabel.setStyle(
            "-fx-background-color: " + HEADER_BG + "; " +
            "-fx-text-fill: white; -fx-font-size: 15; -fx-font-weight: bold; " +
            "-fx-padding: 16 16 14 16;"
        );

        // Style des données
        String dataStyle = "-fx-text-fill: " + TEXT + "; -fx-font-size: 12; -fx-padding: 3 16;";
        for (Label l : new Label[]{
                coordLabel, exitLabel, edgesLabel,
                agentsLabel, capacityLabel, occupiedLabel, congestionLabel, accessibleLabel,
                avgSpeedLabel, avgStressLabel, dominantStateLabel,
                globalStressLabel,
                fireLabel, fireIntensityLabel, fireSmokeLabel, fireSpreadLabel, fireTicksLabel
        }) {
            l.setStyle(dataStyle);
        }

        getChildren().addAll(
            titleLabel,
            sectionHeader("GÉNÉRAL"),
            coordLabel, exitLabel, edgesLabel,
            sectionHeader("OCCUPATION"),
            agentsLabel, capacityLabel, occupiedLabel, congestionLabel, accessibleLabel,
            sectionHeader("AGENTS"),
            avgSpeedLabel, avgStressLabel, dominantStateLabel,
            sectionHeader("STRESS GLOBAL"),
            globalStressLabel,
            sectionHeader("FEU"),
            fireLabel, fireIntensityLabel, fireSmokeLabel, fireSpreadLabel, fireTicksLabel
        );

        setFireDetailsVisible(false);
    }

    // -----------------------------------------------------------------------

    /** Crée un label de section avec fond coloré, style "badge". */
    private Label sectionHeader(String text) {
        Label l = new Label(text);
        l.setMaxWidth(Double.MAX_VALUE);
        l.setStyle(
            "-fx-background-color: " + SECTION_BG + "; " +
            "-fx-text-fill: " + ACCENT + "; " +
            "-fx-font-size: 10; -fx-font-weight: bold; " +
            "-fx-padding: 7 16 5 16;"
        );
        return l;
    }

    // -----------------------------------------------------------------------

    public void display(Node node) {
        List<Agent> agents = node.getAgents();
        String data = "-fx-text-fill: " + TEXT + "; -fx-font-size: 12; -fx-padding: 3 16;";

        // --- Titre ---
        titleLabel.setText((node.isExit() ? "Sortie" : "Nœud") + "  #" + node.getId());

        // --- Général ---
        coordLabel.setText("Position : (" + (int)node.getX() + ", " + (int)node.getY() + ")");
        exitLabel.setText("Sortie : " + (node.isExit() ? "Oui ✓" : "Non"));
        exitLabel.setStyle(data + "-fx-text-fill: " + (node.isExit() ? GREEN : TEXT_DIM) + ";");
        edgesLabel.setText("Arêtes connectées : " + node.getEdges().size());

        // --- Occupation ---
        agentsLabel.setText("Agents présents : " + agents.size());
        agentsLabel.setStyle(data + "-fx-text-fill: " + (agents.size() > 0 ? TEXT : TEXT_DIM) + ";");
        capacityLabel.setText("Capacité : " + String.format("%.1f", node.getCapacity()));
        occupiedLabel.setText("Espace occupé : " + String.format("%.1f", node.getOccupiedSpace()));

        double cong = node.getCongestion();
        congestionLabel.setText("Congestion : " + String.format("%.0f%%", cong * 100));
        congestionLabel.setStyle(data + "-fx-text-fill: " + congestionColor(cong) + ";");

        accessibleLabel.setText("Accessible : " + (!node.isFull() ? "Oui ✓" : "Plein ✗"));
        accessibleLabel.setStyle(data + "-fx-text-fill: " + (!node.isFull() ? GREEN : RED) + ";");

        // --- Agents ---
        if (agents.isEmpty()) {
            avgSpeedLabel.setText("Vitesse moy. : —");
            avgStressLabel.setText("Stress moy. : —");
            dominantStateLabel.setText("État dominant : —");
            for (Label l : new Label[]{avgSpeedLabel, avgStressLabel, dominantStateLabel})
                l.setStyle(data + "-fx-text-fill: " + TEXT_DIM + ";");
        } else {
            double avgSpeed = agents.stream().mapToDouble(Agent::getMaxSpeed).average().orElse(0);
            avgSpeedLabel.setText("Vitesse moy. : " + String.format("%.1f", avgSpeed));
            avgSpeedLabel.setStyle(data);

            double avgStress = agents.stream().mapToDouble(Agent::getStressLevel).average().orElse(0);
            avgStressLabel.setText("Stress moy. : " + String.format("%.0f%%", avgStress * 100));
            avgStressLabel.setStyle(data + "-fx-text-fill: " + stressColor(avgStress) + ";");

            EmotionalState dominant = getDominantState(agents);
            dominantStateLabel.setText("État dominant : " + dominant.name());
            dominantStateLabel.setStyle(data + "-fx-text-fill: " + stateColor(dominant) + ";");
        }

        // --- Stress global ---
        double gs = node.getTotalStressInducedIncludingNeighbors();
        globalStressLabel.setText("Stress (+ voisins) : " + String.format("%.0f%%", gs * 100));
        globalStressLabel.setStyle(data + "-fx-text-fill: " + stressColor(gs) + ";");

        // --- Feu ---
        if (node.isOnFire()) {
            setFireDetailsVisible(true);
            fireLabel.setText("EN FEU");
            fireLabel.setStyle(data + "-fx-text-fill: " + RED + "; -fx-font-weight: bold;");
            fireIntensityLabel.setText("Intensité : " + String.format("%.2f", node.getFire().getIntensity()));
            fireSmokeLabel.setText("Fumée : " + String.format("%.2f", node.getFire().getSmokeLevel()));
            fireSpreadLabel.setText("Propagation : " + String.format("%.2f", node.getFire().getSpreadRate()));
            fireTicksLabel.setText("Brûle depuis : " + node.getFire().getBurningTicks() + " ticks");
        } else {
            setFireVisible(false);
            fireLabel.setText("Pas de feu ✅");
            fireLabel.setTextFill(Color.GREEN);
            setFireVisible(true); // on affiche juste "Pas de feu"
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

    private void setFireDetailsVisible(boolean v) {
        for (Label l : new Label[]{fireLabel, fireIntensityLabel, fireSmokeLabel, fireSpreadLabel, fireTicksLabel})
            l.setVisible(v);
    }
}
