package fr.cy.view;

import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/**
 * The {@code SimulationStatsPanel} class represents the left-hand side panel
 * that displays real-time global statistics of the simulation.
 * <p>
 * It acts as a read-only view component, updating its labels based on data
 * provided by the controller.
 * </p>
 */
public class SimulationStatsPanel extends VBox {

    // Style constants for the UI theme
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

    /**
     * Constructs the {@code SimulationStatsPanel} and initializes the UI layout.
     */
    public SimulationStatsPanel() {
        setPrefWidth(215);
        setSpacing(0);
        setStyle("-fx-background-color: " + BG + "; -fx-border-color: " + ACCENT + "; -fx-border-width: 0 3 0 0;");

        Label titleLabel = new Label("STATISTICS");
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
                sectionHeader("FIRE"),
                fireNodesLabel, fireEdgesLabel,
                sectionHeader("GRAPH"),
                graphNodesLabel, graphEdgesLabel, globalCongLabel);
    }

    /**
     * Creates a styled section header label.
     * * @param text The section title text.
     * 
     * @return A styled {@link Label}.
     */
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
     * Updates the displayed statistics.
     * All values are calculated by the controller.
     *
     * @param tick          The current simulation tick.
     * @param running       Whether the simulation is currently running.
     * @param totalAgents   Total number of agents in the simulation.
     * @param onNodes       Number of agents currently on nodes.
     * @param onEdges       Number of agents currently on edges.
     * @param fireNodes     Number of nodes currently on fire.
     * @param fireEdges     Number of edges currently on fire.
     * @param totalNodes    Total number of nodes in the graph.
     * @param totalEdges    Total number of edges in the graph.
     * @param exitNodes     Number of exit nodes.
     * @param avgCongestion Average congestion level (0.0–1.0).
     */
    public void update(int tick, boolean running,
            int totalAgents, int onNodes, int onEdges,
            int fireNodes, int fireEdges,
            int totalNodes, int totalEdges, int exitNodes,
            double avgCongestion) {

        String data = "-fx-text-fill: " + TEXT + "; -fx-font-size: 12; -fx-padding: 3 16;";

        simStateLabel.setText("Status: " + (running ? "Running" : "Paused"));
        simStateLabel.setStyle(data + "-fx-text-fill: " + (running ? GREEN : ORANGE) + ";");
        tickLabel.setText("Tick: " + tick);

        agentTotalLabel.setText("Total: " + totalAgents);
        agentNodesLabel.setText("On nodes: " + onNodes);
        agentEdgesLabel.setText("On edges: " + onEdges);

        fireNodesLabel.setText("Nodes on fire: " + fireNodes);
        fireNodesLabel.setStyle(data + "-fx-text-fill: " + (fireNodes > 0 ? RED : GREEN) + ";");
        fireEdgesLabel.setText("Edges on fire: " + fireEdges);
        fireEdgesLabel.setStyle(data + "-fx-text-fill: " + (fireEdges > 0 ? RED : GREEN) + ";");

        graphNodesLabel.setText("Nodes: " + totalNodes + " (" + exitNodes + " exits)");
        graphEdgesLabel.setText("Edges: " + totalEdges);

        String congColor = avgCongestion > 0.7 ? RED : (avgCongestion > 0.4 ? ORANGE : GREEN);
        globalCongLabel.setText("Avg. Congestion: " + String.format("%.0f%%", avgCongestion * 100));
        globalCongLabel.setStyle(data + "-fx-text-fill: " + congColor + ";");
    }
}