package fr.cy.view;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * The {@code SimulationStatsPanel} class represents the left-hand side panel
 * that displays real-time global statistics of the simulation.
 * <p>
 * It acts as a read-only view component, updating its labels based on data
 * provided by the controller. It leverages the global CSS theme for a unified
 * dashboard appearance and includes a scrollable container for extended
 * metrics.
 * </p>
 */
public class SimulationStatsPanel extends VBox {

        // --- Core Simulation Labels ---
        private final Label simStateLabel = new Label();
        private final Label tickLabel = new Label();
        private final Label tpsLabel = new Label();
        private final Label engineLoadLabel = new Label();

        // --- Evacuation & Survival Labels ---
        private final Label evacuatedLabel = new Label();
        private final Label deadLabel = new Label();
        private final Label survivalRateLabel = new Label();
        private final Label avgEvacTimeLabel = new Label();

        // --- Active Agents Labels ---
        private final Label agentTotalLabel = new Label();
        private final Label agentNodesLabel = new Label();
        private final Label agentEdgesLabel = new Label();
        private final Label globalAvgHealthLabel = new Label();
        private final Label globalAvgSpeedLabel = new Label();
        private final Label globalMoodLabel = new Label();

        // --- Fire Hazards Labels ---
        private final Label fireNodesLabel = new Label();
        private final Label fireEdgesLabel = new Label();
        private final Label hazardSpreadLabel = new Label();

        // --- Graph Infrastructure Labels ---
        private final Label graphNodesLabel = new Label();
        private final Label graphEdgesLabel = new Label();
        private final Label globalCongLabel = new Label();
        private final Label worstBottleneckLabel = new Label();

        /**
         * Constructs the {@code SimulationStatsPanel} and initializes the dashboard
         * layout.
         */
        public SimulationStatsPanel() {
                this.setPrefWidth(280);
                this.getStyleClass().add("details-panel"); // Uses your global CSS

                Label titleLabel = new Label("GLOBAL STATISTICS");
                titleLabel.setStyle(
                                "-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white; -fx-padding: 0 0 10 0;");

                // Build modular section cards
                VBox simCard = createCard("ENGINE",
                                createRow("Status", simStateLabel),
                                createRow("Tick", tickLabel),
                                createRow("TPS", tpsLabel),
                                createRow("Engine Load", engineLoadLabel));

                VBox evacCard = createCard("EVACUATION",
                                createRow("Evacuated", evacuatedLabel),
                                createRow("Casualties", deadLabel),
                                createRow("Survival Rate", survivalRateLabel),
                                createRow("Avg. Evac Time", avgEvacTimeLabel));

                VBox agentsCard = createCard("ACTIVE CROWD",
                                createRow("Total Active", agentTotalLabel),
                                createRow("On Nodes", agentNodesLabel),
                                createRow("On Edges", agentEdgesLabel),
                                createRow("Avg. Health", globalAvgHealthLabel),
                                createRow("Avg. Speed", globalAvgSpeedLabel),
                                createRow("Dominant Mood", globalMoodLabel));

                VBox fireCard = createCard("HAZARDS (FIRE)",
                                createRow("Nodes on fire", fireNodesLabel),
                                createRow("Edges on fire", fireEdgesLabel),
                                createRow("Graph Spread", hazardSpreadLabel));

                VBox graphCard = createCard("INFRASTRUCTURE",
                                createRow("Nodes", graphNodesLabel),
                                createRow("Edges", graphEdgesLabel),
                                createRow("Avg. Congestion", globalCongLabel),
                                createRow("Worst Bottleneck", worstBottleneckLabel));

                // Wrap everything inside a ScrollPane to prevent clipping on small screens
                VBox contentBox = new VBox(15);
                contentBox.getChildren().addAll(simCard, evacCard, agentsCard, fireCard, graphCard);
                contentBox.setStyle("-fx-background-color: transparent;");

                ScrollPane scrollPane = new ScrollPane(contentBox);
                scrollPane.setFitToWidth(true);
                scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
                scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
                scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

                // Remove borders from ScrollPane
                scrollPane.getStyleClass().addAll("edge-to-edge", "no-scrollbar-pane");

                this.getChildren().addAll(titleLabel, scrollPane);
                VBox.setVgrow(scrollPane, Priority.ALWAYS);
        }

        /**
         * Helper: Creates a styled dashboard card with an orange section header.
         */
        private VBox createCard(String titleText, HBox... rows) {
                VBox card = new VBox(12);
                card.getStyleClass().add("stat-card");

                Label title = new Label(titleText);
                title.setStyle("-fx-text-fill: " + ThemeConstants.WARNING_ORANGE
                                + "; -fx-font-size: 12px; -fx-font-weight: bold;");

                card.getChildren().add(title);
                card.getChildren().addAll(rows);
                return card;
        }

        /**
         * Helper: Creates a justified horizontal row (Title on the left, Value on the
         * right).
         */
        private HBox createRow(String labelText, Label valueLabel) {
                Label title = new Label(labelText);
                title.getStyleClass().add("stat-title");
                valueLabel.getStyleClass().add("stat-value");

                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);

                HBox row = new HBox(10);
                row.setAlignment(Pos.CENTER_LEFT);
                row.getChildren().addAll(title, spacer, valueLabel);
                return row;
        }

        /**
         * Master update method for all dashboard statistics.
         * Called by the controller during the animation loop.
         */
        public void update(
                        // Engine Data
                        int tick, boolean running, int tps, double engineLoadMs,
                        // Evacuation Data
                        int evacuated, int dead, double avgEvacTime,
                        // Crowd Data
                        int totalAgents, int onNodes, int onEdges, double avgHealth, double avgSpeed, String globalMood,
                        // Hazard Data
                        int fireNodes, int fireEdges, double fireSpreadPercentage,
                        // Graph Data
                        int totalNodes, int totalEdges, int exitNodes, double avgCongestion, String worstBottleneckId) {
                // --- 1. ENGINE ---
                simStateLabel.setText(running ? "Running" : "Paused");
                simStateLabel.setStyle("-fx-text-fill: "
                                + (running ? ThemeConstants.SUCCESS_GREEN : ThemeConstants.WARNING_ORANGE) + ";");
                tickLabel.setText(String.valueOf(tick));
                tpsLabel.setText(tps + " t/s");
                engineLoadLabel.setText(String.format("%.2f ms", engineLoadMs));
                engineLoadLabel.setStyle("-fx-text-fill: "
                                + (engineLoadMs > 16.0 ? ThemeConstants.DANGER_RED : ThemeConstants.NEUTRAL_TEXT)
                                + ";");
                // >16ms
                // starts
                // lagging
                // 60fps

                // --- 2. EVACUATION ---
                evacuatedLabel.setText(String.valueOf(evacuated));
                evacuatedLabel.setStyle("-fx-text-fill: " + ThemeConstants.SUCCESS_GREEN + ";");
                deadLabel.setText(String.valueOf(dead));
                deadLabel.setStyle("-fx-text-fill: "
                                + (dead > 0 ? ThemeConstants.DANGER_RED : ThemeConstants.NEUTRAL_TEXT) + ";");

                // Calculate dynamic survival rate safely
                int totalPopulation = totalAgents + evacuated + dead;
                double survivalRate = totalPopulation > 0 ? ((double) evacuated / totalPopulation) * 100 : 0.0;
                survivalRateLabel.setText(String.format("%.1f%%", survivalRate));
                survivalRateLabel.setStyle("-fx-text-fill: "
                                + (survivalRate > 80 ? ThemeConstants.SUCCESS_GREEN : ThemeConstants.DANGER_RED) + ";");

                avgEvacTimeLabel.setText(avgEvacTime > 0 ? String.format("%.1f s", avgEvacTime) : "--");

                // --- 3. ACTIVE CROWD ---
                agentTotalLabel.setText(String.valueOf(totalAgents));
                agentNodesLabel.setText(String.valueOf(onNodes));
                agentEdgesLabel.setText(String.valueOf(onEdges));
                globalAvgHealthLabel.setText(String.format("%.1f%%", avgHealth));
                globalAvgSpeedLabel.setText(String.format("%.2f m/s", avgSpeed));

                globalMoodLabel.setText(globalMood != null ? globalMood : "NONE");
                if ("PANICKING".equalsIgnoreCase(globalMood)) {
                        globalMoodLabel.setStyle("-fx-text-fill: " + ThemeConstants.DANGER_RED + ";");
                } else if ("CALM".equalsIgnoreCase(globalMood)) {
                        globalMoodLabel.setStyle("-fx-text-fill: " + ThemeConstants.SUCCESS_GREEN + ";");
                } else {
                        globalMoodLabel.setStyle("-fx-text-fill: " + ThemeConstants.WARNING_ORANGE + ";");
                }

                // --- 4. FIRE HAZARDS ---
                fireNodesLabel.setText(String.valueOf(fireNodes));
                fireNodesLabel.setStyle("-fx-text-fill: "
                                + (fireNodes > 0 ? ThemeConstants.DANGER_RED : ThemeConstants.NEUTRAL_TEXT) + ";");
                fireEdgesLabel.setText(String.valueOf(fireEdges));
                fireEdgesLabel.setStyle("-fx-text-fill: "
                                + (fireEdges > 0 ? ThemeConstants.DANGER_RED : ThemeConstants.NEUTRAL_TEXT) + ";");
                hazardSpreadLabel.setText(String.format("%.1f%%", fireSpreadPercentage * 100));

                // --- 5. INFRASTRUCTURE ---
                graphNodesLabel.setText(totalNodes + " (" + exitNodes + " exits)");
                graphEdgesLabel.setText(String.valueOf(totalEdges));

                globalCongLabel.setText(String.format("%.0f%%", avgCongestion * 100));
                String congColor = avgCongestion > 0.7 ? ThemeConstants.DANGER_RED
                                : (avgCongestion > 0.4 ? ThemeConstants.WARNING_ORANGE : ThemeConstants.SUCCESS_GREEN);
                globalCongLabel.setStyle("-fx-text-fill: " + congColor + ";");

                worstBottleneckLabel.setText(worstBottleneckId != null ? worstBottleneckId : "None");
                worstBottleneckLabel
                                .setStyle("-fx-text-fill: " + (worstBottleneckId != null ? ThemeConstants.WARNING_ORANGE
                                                : ThemeConstants.NEUTRAL_TEXT) + ";");
        }
}