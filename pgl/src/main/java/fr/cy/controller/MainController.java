package fr.cy.controller;

import fr.cy.model.agent.Agent;
import fr.cy.model.agent.AgentSettings;
import fr.cy.model.graph.Graph;
import fr.cy.model.graph.element.Edge;
import fr.cy.model.graph.element.GraphElement;
import fr.cy.model.graph.element.Node;
import fr.cy.model.simulation.Simulation;
import fr.cy.view.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;

/**
 * The {@code MainController} acts as the primary orchestrator of the
 * application.
 * <p>
 * It manages the integration between the UI components (toolbars, panels,
 * canvas)
 * and the underlying {@link SimulationController}, handling cross-component
 * events
 * such as selections, graph editing, rapid deletion, and agent population
 * control loops.
 * </p>
 */
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

    /**
     * Initializes the main interface and sets up event wiring.
     *
     * @param simulation The initial simulation instance.
     */
    public MainController(Simulation simulation) {
        this.root = new BorderPane();
        this.root.getStyleClass().add("main-pane");

        // 1. Initialize Canvas
        Pane canvasContainer = new Pane();
        canvasContainer.getStyleClass().add("canvas-container");
        this.graphCanvas = new GraphCanvas();
        this.graphCanvas.widthProperty().bind(canvasContainer.widthProperty());
        this.graphCanvas.heightProperty().bind(canvasContainer.heightProperty());
        canvasContainer.getChildren().add(graphCanvas);
        root.setCenter(canvasContainer);

        // 2. Initialize Simulation Controller
        this.simController = new SimulationController(simulation, graphCanvas);
        this.simController.setOnRender(this::refreshStatsPanel);

        // 3. Initialize Side Panels
        this.statsPanel = new SimulationStatsPanel();
        root.setLeft(statsPanel);
        this.detailsPanel = new DetailsSidePanel();

        // Wire detail panel fire management actions
        this.detailsPanel.setOnToggleFireRequested(element -> {
            if (element.isOnFire()) {
                element.removeFire();
                AgentSettings settings = simController.getSimulation().getAgentManager().getAgentSettings();
                detailsPanel.update(element, settings);
            } else {
                DialogHelper.showFireDialog(element, detailsPanel).ifPresent(newFire -> {
                    element.setFire(newFire);
                    if (element instanceof Edge edge)
                        edge.igniteFrom(edge.getStart(), newFire);
                    AgentSettings settings = simController.getSimulation().getAgentManager().getAgentSettings();
                    detailsPanel.update(element, settings);
                });
            }
        });

        // Wire details panel standard deletion channels to the unified factory pipeline
        this.detailsPanel.setOnDeleteRequested(this::deleteEntity);
        this.detailsPanel.setOnDeleteAgentRequested(this::deleteEntity);

        // Wire specific agent tracking execution states (Casualties)
        this.detailsPanel.setOnKillAgentRequested(agent -> {
            var agentManager = simController.getSimulation().getAgentManager();
            if (agentManager != null) {
                agentManager.killAgent(agent);

                if (agent.isOnNode() && agent.getCurrentNode() != null) {
                    agent.getCurrentNode().getAgents().remove(agent);
                } else if (agent.getCurrentOrPreviousEdge() != null) {
                    agent.getCurrentOrPreviousEdge().getAgents().remove(agent);
                }

                this.currentSelectedEntity = null;
                this.graphCanvas.setSelectedEntity(null);
            }
        });

        root.setRight(detailsPanel);

        // 4. Initialize Toolbars
        this.toolBar = new SimulationToolBar(simController);
        root.setBottom(toolBar);
        this.editingToolBar = new GraphEditingToolBar();
        root.setTop(editingToolBar);

        // 5. Initialize Interaction Controller
        this.interactionController = new CanvasInteractionController(graphCanvas, simController);
        this.editingToolBar.setOnModeChange(mode -> this.interactionController.setMode(mode));
        this.editingToolBar.setOnGenerateRandom(this::generateRandomMass);
        this.interactionController.setOnAddAgentRequested(this::promptAndAddAgentsToNode);
        this.editingToolBar.setOnGenerateRandomAgents(this::generateRandomAgents);

        // Wire rapid canvas deletion mode clicks to the unified deletion pipeline
        this.interactionController.setOnDeleteElementRequested(this::deleteEntity);

        this.interactionController.setOnEntitySelected(entity -> {
            this.currentSelectedEntity = entity;
            graphCanvas.setSelectedEntity(entity);
            if (entity == null) {
                this.detailsPanel.hidePanel();
            } else {
                AgentSettings settings = simController.getSimulation().getAgentManager().getAgentSettings();
                this.detailsPanel.update(entity, settings);
                this.detailsPanel.showPanel();
            }
        });

        this.simController.startLoop();
    }

    /**
     * Unified orchestration routine handling removal tasks for Nodes, Edges, or
     * Agents.
     * Delegates agent physics and relocation tasks safely to the Model layer before
     * structural deletion.
     * Shared dynamically between side context bars and rapid canvas tool modes.
     *
     * @param entity The generic element target to scrub from the workspace.
     */
    private void deleteEntity(Object entity) {
        if (entity == null || simController.getSimulation() == null)
            return;

        var graph = simController.getSimulation().getGraph();
        var agentManager = simController.getSimulation().getAgentManager();

        if (entity instanceof Node) {
            Node node = (Node) entity;
            if (agentManager != null) {
                agentManager.evacuateAgentsBeforeDeletion(node); // 1. Évacuation
            }
            if (graph != null) {
                graph.removeNode(node); // 2. Destruction
            }
        } else if (entity instanceof Edge) {
            Edge edge = (Edge) entity;
            if (agentManager != null) {
                agentManager.evacuateAgentsBeforeDeletion(edge);
            }
            if (graph != null) {
                graph.removeEdge(edge);
            }
        } else if (entity instanceof Agent) {
            Agent agent = (Agent) entity;
            if (agentManager != null) {
                agentManager.deleteAgent(agent); // Suppression directe
            }
        }

        // Nettoyage visuel de la sélection
        if (entity.equals(currentSelectedEntity)) {
            this.currentSelectedEntity = null;
            this.graphCanvas.setSelectedEntity(null);
            this.detailsPanel.hidePanel();
        }
    }

    /** Generates a random graph layout. */
    private void generateRandomMass() {
        DialogHelper.showRandomGraphDialog(graphCanvas).ifPresent(count -> {
            Graph graph = simController.getSimulation().getGraph();
            java.util.Random rand = new java.util.Random();
            java.util.List<Node> generatedNodes = new java.util.ArrayList<>();
            for (int i = 0; i < count; i++) {
                Node n = graph.createNode(rand.nextDouble() * 1000, rand.nextDouble() * 1000, 50.0);
                generatedNodes.add(n);
            }
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
                                double dist = Math.hypot(n1.getX() - n2.getX(), n1.getY() - n2.getY());
                                graph.createEdge(n1, n2, dist / 10.0, 2.0, false);
                            }
                        });
            }
        });
    }

    /** Prompts for agent count and adds them to a specific node. */
    private void promptAndAddAgentsToNode(Node targetNode) {
        DialogHelper.showAgentCountDialog(graphCanvas, "Generate Crowd", "Add agents to node #" + targetNode.getId())
                .ifPresent(count -> {
                    simController.getSimulation().getAgentManager().generateAgentsOnNode("Agent_", targetNode, count);
                    AgentSettings settings = simController.getSimulation().getAgentManager().getAgentSettings();
                    detailsPanel.update(targetNode, settings);
                });
    }

    /** Distributes random agents across the entire network. */
    private void generateRandomAgents() {
        DialogHelper.showAgentCountDialog(graphCanvas, "Random Deployment", "Deploy agents across network")
                .ifPresent(count -> {
                    java.util.List<Node> allNodes = simController.getSimulation().getGraph().getNodes();
                    if (allNodes.isEmpty())
                        return;
                    java.util.Random rand = new java.util.Random();
                    for (int i = 0; i < count; i++) {
                        Node randomNode = allNodes.get(rand.nextInt(allNodes.size()));
                        simController.getSimulation().getAgentManager().generateAgentsOnNode("Rnd_", randomNode, 1);
                    }
                });
    }

    /** Refreshes the statistics panel and detail panel content every frame. */
    private void refreshStatsPanel() {
        if (simController.getSimulation() == null)
            return;
        Graph graph = simController.getSimulation().getGraph();
        int totalAgents = simController.getSimulation().getAgentManager().getAgentsToEvacuate().size();
        int onNodes = graph.getNodes().stream().mapToInt(n -> n.getAgents().size()).sum();
        int onEdges = graph.getEdges().stream().mapToInt(e -> e.getAgents().size()).sum();
        int fireNodes = (int) graph.getNodes().stream().filter(Node::isOnFire).count();
        int fireEdges = (int) graph.getEdges().stream().filter(Edge::isOnFire).count();
        double avgCong = graph.getNodes().stream().mapToDouble(Node::getCongestion).average().orElse(0);

        statsPanel.update(simController.getSimulation().getCurrentTick(), simController.isRunning(),
                totalAgents, onNodes, onEdges, fireNodes, fireEdges,
                graph.getNodes().size(), graph.getEdges().size(), graph.getExits().size(), avgCong);

        if (currentSelectedEntity != null && simController.isRunning()) {
            AgentSettings settings = simController.getSimulation().getAgentManager().getAgentSettings();
            detailsPanel.update(currentSelectedEntity, settings);
        }
    }

    /** @return The root pane of the application. */
    public BorderPane getRoot() {
        return root;
    }
}