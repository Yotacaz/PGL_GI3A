package fr.cy.controller;

import fr.cy.model.agent.AgentSettings;
import fr.cy.model.graph.Graph;
import fr.cy.model.graph.element.Edge;
import fr.cy.model.graph.element.Node;
import fr.cy.model.simulation.Simulation;
import fr.cy.view.DetailsSidePanel;
import fr.cy.view.GraphCanvas;
import fr.cy.view.SimulationStatsPanel;
import fr.cy.view.SimulationToolBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;

public class MainController {

    private final BorderPane root;
    private GraphCanvas graphCanvas;
    private SimulationController simController;

    private SimulationToolBar toolBar;
    private DetailsSidePanel detailsPanel;
    private SimulationStatsPanel statsPanel;
    private CanvasInteractionController interactionController;

    private Object currentSelectedEntity = null;

    public MainController(Simulation simulation) {
        this.root = new BorderPane();
        this.root.getStyleClass().add("main-pane");

        // 1. Initialisation du Canvas central
        Pane canvasContainer = new Pane();
        canvasContainer.getStyleClass().add("canvas-container");
        this.graphCanvas = new GraphCanvas();
        this.graphCanvas.widthProperty().bind(canvasContainer.widthProperty());
        this.graphCanvas.heightProperty().bind(canvasContainer.heightProperty());
        canvasContainer.getChildren().add(graphCanvas);
        root.setCenter(canvasContainer);

        // 2. Initialisation du Moteur de Simulation
        this.simController = new SimulationController(simulation, graphCanvas);
        this.simController.setOnRender(this::refreshStatsPanel);

        // 3. Initialisation des panneaux latéraux
        this.statsPanel = new SimulationStatsPanel();
        root.setLeft(statsPanel);

        this.detailsPanel = new DetailsSidePanel();
        root.setRight(detailsPanel); // Il est rattaché mais commence "caché" (largeur 0)

        // 4. Initialisation de la Toolbar
        this.toolBar = new SimulationToolBar(simController);
        root.setTop(toolBar);

        // 5. Initialisation du contrôleur d'interactions (Souris/Caméra)
        this.interactionController = new CanvasInteractionController(graphCanvas, simController);

        // --- LA MAGIE DES ANIMATIONS EST APPELÉE ICI ---
        this.interactionController.setOnEntitySelected(entity -> {
            this.currentSelectedEntity = entity;

            if (entity == null) {
                // Si on clique dans le vide, on range le panneau !
                this.detailsPanel.hidePanel();
            } else {
                AgentSettings settings = simController.getSimulation().getAgentManager().getAgentSettings();
                this.detailsPanel.update(entity, settings);
                // Et on le fait apparaitre en glissant
                this.detailsPanel.showPanel();
            }
        });

        this.simController.startLoop();
    }

    private void refreshStatsPanel() {
        if (simController.getSimulation() == null)
            return;

        Graph graph = simController.getSimulation().getGraph();
        int totalAgents = simController.getSimulation().getAgentManager().getAgentsToEvacuate().size();
        int onNodes = graph.getNodes().stream().mapToInt(n -> n.getAgents().size()).sum();
        int onEdges = graph.getEdges().stream().mapToInt(e -> e.getAgents().size()).sum();
        int fireNodes = (int) graph.getNodes().stream().filter(Node::isOnFire).count();
        int fireEdges = (int) graph.getEdges().stream().filter(Edge::isOnFire).count();
        double avgCong = graph.getNodes().stream().mapToDouble(n -> n.getCongestion()).average().orElse(0);

        statsPanel.update(
                simController.getSimulation().getCurrentTick(),
                simController.isRunning(),
                totalAgents, onNodes, onEdges,
                fireNodes, fireEdges,
                graph.getNodes().size(), graph.getEdges().size(), graph.getExits().size(),
                avgCong);

        // Suivi en temps réel de l'élément sélectionné
        if (currentSelectedEntity != null && simController.isRunning()) {
            AgentSettings settings = simController.getSimulation().getAgentManager().getAgentSettings();
            detailsPanel.update(currentSelectedEntity, settings);
        }
    }

    public BorderPane getRoot() {
        return root;
    }
}