package fr.cy.view;

import fr.cy.model.agent.Agent;
import fr.cy.model.agent.behaviour.properties.EmotionalState;
import fr.cy.model.graph.element.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.util.List;

public class NodeInfoPanel extends VBox {

    private static final String BG         = "#0F0F1A";
    private static final String HEADER_BG  = "#141428";
    private static final String SECTION_BG = "#1C1C32";
    private static final String ACCENT     = "#5B7FFF";
    private static final String TEXT       = "#E8E8FF";
    private static final String TEXT_DIM   = "#888AB0";
    private static final String GREEN      = "#4ADE80";
    private static final String ORANGE     = "#FFA94D";
    private static final String RED        = "#FF6B6B";

    private final Label titleLabel         = new Label("Cliquez sur un noeud");
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

        titleLabel.setMaxWidth(Double.MAX_VALUE);
        titleLabel.setStyle(
            "-fx-background-color: " + HEADER_BG + "; " +
            "-fx-text-fill: white; -fx-font-size: 15; -fx-font-weight: bold; " +
            "-fx-padding: 16 16 14 16;"
        );

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
            sectionHeader("GENERAL"),
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

    public void display(Node node) {
        List<Agent> agents = node.getAgents();
        String data = "-fx-text-fill: " + TEXT + "; -fx-font-size: 12; -fx-padding: 3 16;";

        titleLabel.setText((node.isExit() ? "Sortie" : "Noeud") + "  #" + node.getId());

        coordLabel.setText("Position : (" + (int)node.getX() + ", " + (int)node.getY() + ")");
        exitLabel.setText("Sortie : " + (node.isExit() ? "Oui" : "Non"));
        exitLabel.setStyle(data + "-fx-text-fill: " + (node.isExit() ? GREEN : TEXT_DIM) + ";");
        edgesLabel.setText("Aretes connectees : " + node.getEdges().size());

        agentsLabel.setText("Agents presents : " + agents.size());
        agentsLabel.setStyle(data + "-fx-text-fill: " + (agents.size() > 0 ? TEXT : TEXT_DIM) + ";");
        capacityLabel.setText("Capacite : " + String.format("%.1f", node.getCapacity()));
        occupiedLabel.setText("Espace occupe : " + String.format("%.1f", node.getOccupiedSpace()));

        double cong = node.getCongestion();
        congestionLabel.setText("Congestion : " + String.format("%.0f%%", cong * 100));
        congestionLabel.setStyle(data + "-fx-text-fill: " + congestionColor(cong) + ";");

        accessibleLabel.setText("Accessible : " + (!node.isFull() ? "Oui" : "Plein"));
        accessibleLabel.setStyle(data + "-fx-text-fill: " + (!node.isFull() ? GREEN : RED) + ";");

        if (agents.isEmpty()) {
            avgSpeedLabel.setText("Vitesse moy. : -");
            avgStressLabel.setText("Stress moy. : -");
            dominantStateLabel.setText("Etat dominant : -");
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
            dominantStateLabel.setText("Etat dominant : " + dominant.name());
            dominantStateLabel.setStyle(data + "-fx-text-fill: " + stateColor(dominant) + ";");
        }

        double gs = node.getTotalStressInducedIncludingNeighbors();
        globalStressLabel.setText("Stress (+ voisins) : " + String.format("%.0f%%", gs * 100));
        globalStressLabel.setStyle(data + "-fx-text-fill: " + stressColor(gs) + ";");

        if (node.isOnFire()) {
            setFireDetailsVisible(true);
            fireLabel.setText("EN FEU");
            fireLabel.setStyle(data + "-fx-text-fill: " + RED + "; -fx-font-weight: bold;");
            fireIntensityLabel.setText("Intensite : " + String.format("%.2f", node.getFire().getIntensity()));
            fireSmokeLabel.setText("Fumee : " + String.format("%.2f", node.getFire().getSmokeLevel()));
            fireSpreadLabel.setText("Propagation : " + String.format("%.2f", node.getFire().getSpreadRate()));
            fireTicksLabel.setText("Brule depuis : " + node.getFire().getBurningTicks() + " ticks");
        } else {
            setFireDetailsVisible(false);
            fireLabel.setVisible(true);
            fireLabel.setText("Pas de feu");
            fireLabel.setStyle(data + "-fx-text-fill: " + GREEN + ";");
        }
    }

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
            switch (a.getState()) {
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
