package fr.cy.view;

import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/**
 * Panneau latéral gauche affichant les statistiques globales de la simulation.
 * Ne fait qu'afficher des valeurs pré-calculées par le contrôleur.
 */
public class SimulationStatsPanel extends VBox {

    private static final String BG = "#0F0F1A";
    private static final String HEADER_BG = "#141428";
    private static final String SECTION_BG = "#1C1C32";
    private static final String ACCENT = "#5B7FFF";
    private static final String TEXT = "#E8E8FF";
    private static final String GREEN = "#4ADE80";
    private static final String ORANGE = "#FFA94D";
    private static final String RED = "#FF6B6B";

    private final Label simStateLabel = new Label();
    private final Label tickLabel = new Label();
    private final Label agentTotalLabel = new Label();
    private final Label agentNodesLabel = new Label();
    private final Label agentEdgesLabel = new Label();
    private final Label fireNodesLabel = new Label();
    private final Label fireEdgesLabel = new Label();
    private final Label graphNodesLabel = new Label();
    private final Label graphEdgesLabel = new Label();
    private final Label globalCongLabel = new Label();

    public SimulationStatsPanel() {
        setPrefWidth(215);
        setSpacing(0);
        setStyle("-fx-background-color: " + BG + "; -fx-border-color: " + ACCENT + "; -fx-border-width: 0 3 0 0;");

        Label titleLabel = new Label("STATISTIQUES");
        titleLabel.setMaxWidth(Double.MAX_VALUE);
        titleLabel.setStyle(
                "-fx-background-color: " + HEADER_BG + "; " +
                        "-fx-text-fill: white; -fx-font-size: 14; -fx-font-weight: bold; " +
                        "-fx-padding: 16 16 14 16;");

        String dataStyle = "-fx-text-fill: " + TEXT + "; -fx-font-size: 12; -fx-padding: 3 16;";
        for (Label l : new Label[] { simStateLabel, tickLabel, agentTotalLabel, agentNodesLabel,
                agentEdgesLabel, fireNodesLabel, fireEdgesLabel, graphNodesLabel,
                graphEdgesLabel, globalCongLabel }) {
            l.setStyle(dataStyle);
        }

        getChildren().addAll(
                titleLabel,
                sectionHeader("SIMULATION"),
                simStateLabel, tickLabel,
                sectionHeader("AGENTS"),
                agentTotalLabel, agentNodesLabel, agentEdgesLabel,
                sectionHeader("INCENDIES"),
                fireNodesLabel, fireEdgesLabel,
                sectionHeader("GRAPHE"),
                graphNodesLabel, graphEdgesLabel, globalCongLabel);
    }

    private Label sectionHeader(String text) {
        Label l = new Label(text);
        l.setMaxWidth(Double.MAX_VALUE);
        l.setStyle(
                "-fx-background-color: " + SECTION_BG + "; " +
                        "-fx-text-fill: " + ACCENT + "; " +
                        "-fx-font-size: 10; -fx-font-weight: bold; " +
                        "-fx-padding: 7 16 5 16;");
        return l;
    }

    /**
     * Met à jour tous les labels. Toutes les valeurs sont calculées dans le
     * contrôleur.
     *
     * @param tick          tick courant
     * @param running       simulation en cours
     * @param totalAgents   nombre total d'agents dans la simulation
     * @param onNodes       agents actuellement sur des nœuds
     * @param onEdges       agents actuellement sur des arêtes
     * @param fireNodes     nœuds en feu
     * @param fireEdges     arêtes en feu
     * @param totalNodes    total de nœuds dans le graphe
     * @param totalEdges    total d'arêtes dans le graphe
     * @param exitNodes     nœuds sorties
     * @param avgCongestion congestion moyenne (0.0–1.0)
     */
    public void update(int tick, boolean running,
            int totalAgents, int onNodes, int onEdges,
            int fireNodes, int fireEdges,
            int totalNodes, int totalEdges, int exitNodes,
            double avgCongestion) {

        String data = "-fx-text-fill: " + TEXT + "; -fx-font-size: 12; -fx-padding: 3 16;";

        simStateLabel.setText("État : " + (running ? "En cours" : "En pause"));
        simStateLabel.setStyle(data + "-fx-text-fill: " + (running ? GREEN : ORANGE) + ";");
        tickLabel.setText("Tick : " + tick);

        agentTotalLabel.setText("Total : " + totalAgents);
        agentNodesLabel.setText("Sur nœuds : " + onNodes);
        agentEdgesLabel.setText("Sur arêtes : " + onEdges);

        fireNodesLabel.setText("Nœuds en feu : " + fireNodes);
        fireNodesLabel.setStyle(data + "-fx-text-fill: " + (fireNodes > 0 ? RED : GREEN) + ";");
        fireEdgesLabel.setText("Arêtes en feu : " + fireEdges);
        fireEdgesLabel.setStyle(data + "-fx-text-fill: " + (fireEdges > 0 ? RED : GREEN) + ";");

        graphNodesLabel.setText("Nœuds : " + totalNodes + " (" + exitNodes + " sorties)");
        graphEdgesLabel.setText("Arêtes : " + totalEdges);

        String congColor = avgCongestion > 0.7 ? RED : (avgCongestion > 0.4 ? ORANGE : GREEN);
        globalCongLabel.setText("Congestion moy. : " + String.format("%.0f%%", avgCongestion * 100));
        globalCongLabel.setStyle(data + "-fx-text-fill: " + congColor + ";");
    }
}
