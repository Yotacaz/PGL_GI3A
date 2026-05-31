package fr.cy.view;

import fr.cy.model.agent.Agent;
import fr.cy.model.agent.behaviour.properties.EmotionalState;
import fr.cy.model.graph.element.Edge;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.util.List;

/**
 * Panneau d'informations d'une arête — thème sombre moderne.
 * S'affiche à droite quand on clique sur une arête.
 */
public class EdgeInfoPanel extends VBox {

    // ---- Couleurs du thème (identiques à NodeInfoPanel) ----
    private static final String BG         = "#0F0F1A";
    private static final String HEADER_BG  = "#141428";
    private static final String SECTION_BG = "#1C1C32";
    private static final String ACCENT     = "#5B7FFF";
    private static final String TEXT       = "#E8E8FF";
    private static final String TEXT_DIM   = "#888AB0";
    private static final String GREEN      = "#4ADE80";
    private static final String ORANGE     = "#FFA94D";
    private static final String RED        = "#FF6B6B";

    // ---- Labels ----
    private final Label titleLabel         = new Label("Cliquez sur une arête");
    private final Label startLabel         = new Label();
    private final Label endLabel           = new Label();
    private final Label directedLabel      = new Label();
    private final Label lengthLabel        = new Label();
    private final Label widthLabel         = new Label();
    private final Label capacityLabel      = new Label();
    private final Label agentsLabel        = new Label();
    private final Label occupiedLabel      = new Label();
    private final Label congestionLabel    = new Label();
    private final Label accessibleLabel    = new Label();
    private final Label maxSpeedLabel      = new Label();
    private final Label avgStressLabel     = new Label();
    private final Label dominantStateLabel = new Label();
    private final Label globalStressLabel  = new Label();
    private final Label fireLabel          = new Label();
    private final Label fireIntensityLabel = new Label();
    private final Label fireSmokeLabel     = new Label();
    private final Label fireSpreadLabel    = new Label();
    private final Label fireTicksLabel     = new Label();

    public EdgeInfoPanel() {
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
                startLabel, endLabel, directedLabel,
                lengthLabel, widthLabel, capacityLabel,
                agentsLabel, occupiedLabel, congestionLabel, accessibleLabel,
                maxSpeedLabel, avgStressLabel, dominantStateLabel,
                globalStressLabel,
                fireLabel, fireIntensityLabel, fireSmokeLabel, fireSpreadLabel, fireTicksLabel
        }) {
            l.setStyle(dataStyle);
        }

        getChildren().addAll(
            titleLabel,
            sectionHeader("GÉNÉRAL"),
            startLabel, endLabel, directedLabel,
            sectionHeader("DIMENSIONS"),
            lengthLabel, widthLabel, capacityLabel,
            sectionHeader("OCCUPATION"),
            agentsLabel, occupiedLabel, congestionLabel, accessibleLabel,
            sectionHeader("AGENTS"),
            maxSpeedLabel, avgStressLabel, dominantStateLabel,
            sectionHeader("STRESS GLOBAL"),
            globalStressLabel,
            sectionHeader("FEU"),
            fireLabel, fireIntensityLabel, fireSmokeLabel, fireSpreadLabel, fireTicksLabel
        );

        setFireDetailsVisible(false);
    }

    // -----------------------------------------------------------------------

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

    public void display(Edge edge) {
        List<Agent> agents = edge.getAgents();
        String data = "-fx-text-fill: " + TEXT + "; -fx-font-size: 12; -fx-padding: 3 16;";

        // --- Titre ---
        titleLabel.setText("Arête  #" + edge.getId());

        // --- Général ---
        startLabel.setText("De : Nœud #" + edge.getStart().getId());
        endLabel.setText("Vers : Nœud #" + edge.getEnd().getId());
        directedLabel.setText("Dirigée : " + (edge.isDirected() ? "Oui" : "Non"));

        // --- Dimensions ---
        lengthLabel.setText("Longueur : " + String.format("%.1f", edge.getLength()));
        widthLabel.setText("Largeur : " + String.format("%.1f", edge.getWidth()));
        capacityLabel.setText("Capacité : " + String.format("%.1f", edge.getCapacity()));

        // --- Occupation ---
        agentsLabel.setText("Agents présents : " + agents.size());
        agentsLabel.setStyle(data + "-fx-text-fill: " + (agents.size() > 0 ? TEXT : TEXT_DIM) + ";");
        occupiedLabel.setText("Espace occupé : " + String.format("%.1f", edge.getOccupiedSpace()));

        double cong = edge.getCongestion();
        congestionLabel.setText("Congestion : " + String.format("%.0f%%", cong * 100));
        congestionLabel.setStyle(data + "-fx-text-fill: " + congestionColor(cong) + ";");

        accessibleLabel.setText("Accessible : " + (!edge.isFull() ? "Oui ✓" : "Plein ✗"));
        accessibleLabel.setStyle(data + "-fx-text-fill: " + (!edge.isFull() ? GREEN : RED) + ";");

        // --- Agents ---
        maxSpeedLabel.setText("Vitesse max : " + String.format("%.1f", edge.getMaxAgentSpeed()));
        if (agents.isEmpty()) {
            avgStressLabel.setText("Stress moy. : —");
            dominantStateLabel.setText("État dominant : —");
            avgStressLabel.setStyle(data + "-fx-text-fill: " + TEXT_DIM + ";");
            dominantStateLabel.setStyle(data + "-fx-text-fill: " + TEXT_DIM + ";");
        } else {
            double avgStress = agents.stream().mapToDouble(Agent::getStressLevel).average().orElse(0);
            avgStressLabel.setText("Stress moy. : " + String.format("%.0f%%", avgStress * 100));
            avgStressLabel.setStyle(data + "-fx-text-fill: " + stressColor(avgStress) + ";");

            EmotionalState dominant = getDominantState(agents);
            dominantStateLabel.setText("État dominant : " + dominant.name());
            dominantStateLabel.setStyle(data + "-fx-text-fill: " + stateColor(dominant) + ";");
        }

        // --- Stress global ---
        double gs = edge.getCachedTotalStressInducedIncludingNeighbors();
        globalStressLabel.setText("Stress (+ voisins) : " + String.format("%.0f%%", gs * 100));
        globalStressLabel.setStyle(data + "-fx-text-fill: " + stressColor(gs) + ";");

        // --- Feu ---
        if (edge.isOnFire()) {
            setFireDetailsVisible(true);
            fireLabel.setText("EN FEU");
            fireLabel.setStyle(data + "-fx-text-fill: " + RED + "; -fx-font-weight: bold;");
            fireIntensityLabel.setText("Intensité : " + String.format("%.2f", edge.getFire().getIntensity()));
            fireSmokeLabel.setText("Fumée : " + String.format("%.2f", edge.getFire().getSmokeLevel()));
            fireSpreadLabel.setText("Propagation : " + String.format("%.2f", edge.getFire().getSpreadRate()));
            fireTicksLabel.setText("Brûle depuis : " + edge.getFire().getBurningTicks() + " ticks");
        } else {
            setFireDetailsVisible(false);
            fireLabel.setVisible(true);
            fireLabel.setText("Pas de feu ✓");
            fireLabel.setStyle(data + "-fx-text-fill: " + GREEN + ";");
        }
    }

    // -----------------------------------------------------------------------

    private String congestionColor(double cong) {
        if (cong > 0.7) return RED;
        if (cong > 0.4) return ORANGE;
        return GREEN;
    }

    private String stressColor(double stress) {
        if (stress > 0.7) return RED;
        if (stress > 0.4) return ORANGE;
        return GREEN;
    }

    private String stateColor(EmotionalState state) {
        return switch (state) {
            case CALM      -> GREEN;
            case SELFISH   -> ORANGE;
            case PANICKING -> RED;
        };
    }

    private EmotionalState getDominantState(List<Agent> agents) {
        int calm = 0, selfish = 0, panicking = 0;
        for (Agent a : agents) {
            switch (a.getEmotionalState()) {
                case CALM      -> calm++;
                case SELFISH   -> selfish++;
                case PANICKING -> panicking++;
            }
        }
        if (panicking >= calm && panicking >= selfish) return EmotionalState.PANICKING;
        if (selfish >= calm)                           return EmotionalState.SELFISH;
        return EmotionalState.CALM;
    }

    private void setFireDetailsVisible(boolean v) {
        for (Label l : new Label[]{fireLabel, fireIntensityLabel, fireSmokeLabel, fireSpreadLabel, fireTicksLabel})
            l.setVisible(v);
    }
}
