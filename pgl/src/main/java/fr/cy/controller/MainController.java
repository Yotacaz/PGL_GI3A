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
import fr.cy.view.GraphEditingToolBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;

import fr.cy.view.DialogHelper;

public class MainController {

    private final BorderPane root;
    private GraphCanvas graphCanvas;
    private SimulationController simController;

    private SimulationToolBar toolBar;
    private GraphEditingToolBar editingToolBar;
    private DetailsSidePanel detailsPanel;
    private SimulationStatsPanel statsPanel;
    private CanvasInteractionController interactionController;

    private Object currentSelectedEntity = null;

    public MainController(Simulation simulation) {
        this.root = new BorderPane();
        this.root.getStyleClass().add("main-pane");

        // 1. Initialise the Canvas
        Pane canvasContainer = new Pane();
        canvasContainer.getStyleClass().add("canvas-container");
        this.graphCanvas = new GraphCanvas();
        this.graphCanvas.widthProperty().bind(canvasContainer.widthProperty());
        this.graphCanvas.heightProperty().bind(canvasContainer.heightProperty());
        canvasContainer.getChildren().add(graphCanvas);
        root.setCenter(canvasContainer);

        // 2. Initialise the Simulation Controller
        this.simController = new SimulationController(simulation, graphCanvas);
        this.simController.setOnRender(this::refreshStatsPanel);

        // 3. Initialisation of the side panels
        this.statsPanel = new SimulationStatsPanel();
        root.setLeft(statsPanel);

        this.detailsPanel = new DetailsSidePanel();

        // ==========================================================
        // WIRING OF THE SIDE PANEL ACTIONS (MVC)
        // ==========================================================

        // Action 1 : The fire
        this.detailsPanel.setOnToggleFireRequested(element -> {
            if (element.isOnFire()) {
                element.removeFire();
                AgentSettings settings = simController.getSimulation().getAgentManager().getAgentSettings();
                detailsPanel.update(element, settings);
            } else {
                // ---> WA ADD 'detailsPanel' AS FOR THE PARENT <---
                fr.cy.view.DialogHelper.showFireDialog(element, detailsPanel).ifPresent(newFire -> {

                    element.setFire(newFire);
                    if (element instanceof Edge edge) {
                        edge.igniteFrom(edge.getStart(), newFire);
                    }

                    AgentSettings settings = simController.getSimulation().getAgentManager().getAgentSettings();
                    detailsPanel.update(element, settings);
                });
            }
        });

        // Action 2 : Deletion
        this.detailsPanel.setOnDeleteRequested(element -> {
            Graph graph = simController.getSimulation().getGraph();

            // Delete from the Graph
            if (element instanceof Node node) {
                graph.removeNode(node);
            } else if (element instanceof Edge edge) {
                graph.removeEdge(edge);
            }

            // We deselect cleanly
            this.currentSelectedEntity = null;
            //If you have a method clearSelection() in interactionController or graphCanvas, call it.
            // ex: interactionController.clearSelection();
            // or : graphCanvas.setSelectedNode(null); graphCanvas.setSelectedEdge(null);
        });

        // ==========================================================

        root.setRight(detailsPanel); // Linked but start hidden

        // 4. Initialise Toolbar
        this.toolBar = new SimulationToolBar(simController);
        root.setBottom(toolBar);

        this.editingToolBar = new GraphEditingToolBar();
        root.setTop(editingToolBar);

        // 5. Initialise Interaction controleur (mouse + camera)
        this.interactionController = new CanvasInteractionController(graphCanvas, simController);

        this.editingToolBar.setOnModeChange(mode -> this.interactionController.setMode(mode));
        this.editingToolBar.setOnGenerateRandom(this::generateRandomMass);

        // Add Agents on Node action (from the interaction controller, which detects the click on a node, to the details panel which shows the update)
        this.interactionController.setOnAddAgentRequested(this::promptAndAddAgentsToNode);
        this.editingToolBar.setOnGenerateRandomAgents(this::generateRandomAgents);

        this.interactionController.setOnEntitySelected(entity -> {
            this.currentSelectedEntity = entity;

            graphCanvas.setSelectedEntity(entity);

            if (entity == null) {
                // if you click on the empty space, we hide the details panel
                this.detailsPanel.hidePanel();
            } else {
                AgentSettings settings = simController.getSimulation().getAgentManager().getAgentSettings();
                this.detailsPanel.update(entity, settings);
                // And we show it if it was hidden
                this.detailsPanel.showPanel();
            }
        });

        this.simController.startLoop();
    }

    private void generateRandomMass() {
        DialogHelper.showRandomGraphDialog(graphCanvas).ifPresent(count -> {
            Graph graph = simController.getSimulation().getGraph();
            java.util.Random rand = new java.util.Random();

            double areaWidth = 1000.0;
            double areaHeight = 1000.0;

            java.util.List<Node> generatedNodes = new java.util.ArrayList<>();

            // 1. Creation of Nodes via your Factory (createNode manages the ID and the addNode!)
            for (int i = 0; i < count; i++) {
                Node n = graph.createNode(rand.nextDouble() * areaWidth, rand.nextDouble() * areaHeight, 50.0);
                generatedNodes.add(n);
            }

            // 2. Creation of Edges (Link each node to its 2 nearest neighbors)
            for (Node n1 : generatedNodes) {
                generatedNodes.stream()
                        .filter(n2 -> n1 != n2)
                        .sorted(java.util.Comparator
                                .comparingDouble(n2 -> Math.hypot(n1.getX() - n2.getX(), n1.getY() - n2.getY())))
                        .limit(2)
                        .forEach(n2 -> {
                            boolean alreadyConnected = n1.getEdges().stream()
                                    .anyMatch(e -> e.getOppositeNode(n1).equals(n2));

                            if (!alreadyConnected) {
                                double pixelDistance = Math.hypot(n1.getX() - n2.getX(), n1.getY() - n2.getY());
                                double logicalLength = pixelDistance / 10.0;
                                graph.createEdge(n1, n2, logicalLength, 2.0, false);
                            }
                        });
            }
        });
    }

    private void promptAndAddAgentsToNode(fr.cy.model.graph.element.Node targetNode) {
        fr.cy.view.DialogHelper
                .showAgentCountDialog(graphCanvas, "Générer Foule", "Ajout d'agents sur le Nœud #" + targetNode.getId())
                .ifPresent(count -> {
                    simController.getSimulation().getAgentManager().generateAgentsOnNode("Agent_", targetNode, count);
                    // if the side panel is currently showing the details of this node, we update it to reflect the new agents
                    fr.cy.model.agent.AgentSettings settings = simController.getSimulation().getAgentManager()
                            .getAgentSettings();
                    detailsPanel.update(targetNode, settings);
                });
    }

    private void generateRandomAgents() {
        fr.cy.view.DialogHelper
                .showAgentCountDialog(graphCanvas, "Déploiement Aléatoire", "Répartir des agents sur tout le réseau")
                .ifPresent(count -> {
                    java.util.List<fr.cy.model.graph.element.Node> allNodes = simController.getSimulation().getGraph()
                            .getNodes();
                    if (allNodes.isEmpty())
                        return; // Safety if we click an empty graph

                    java.util.Random rand = new java.util.Random();
                    fr.cy.model.agent.AgentManager agentManager = simController.getSimulation().getAgentManager();

                    // Random spread: for each agent, we pick a random node
                    for (int i = 0; i < count; i++) {
                        fr.cy.model.graph.element.Node randomNode = allNodes.get(rand.nextInt(allNodes.size()));
                        agentManager.generateAgentsOnNode("Rnd_", randomNode, 1);
                    }
                });
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

        // Real-time tracking of the selected element
        if (currentSelectedEntity != null && simController.isRunning()) {
            AgentSettings settings = simController.getSimulation().getAgentManager().getAgentSettings();
            detailsPanel.update(currentSelectedEntity, settings);
        }
    }

    public BorderPane getRoot() {
        return root;
    }
}