package fr.cy.controller;

import fr.cy.model.agent.Agent;
import fr.cy.model.agent.behaviour.properties.EmotionalState;
import fr.cy.model.graph.Graph;
import fr.cy.model.graph.element.Edge;
import fr.cy.model.graph.element.GraphElement;
import fr.cy.model.graph.element.Node;
import fr.cy.model.simulation.Simulation;
import fr.cy.view.GraphCanvas;
import fr.cy.view.SimulationStatsPanel;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.ToolBar;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * Contrôleur principal MVC de l'Interface Utilisateur.
 */
public class MainController {

    private final BorderPane root;
    private GraphCanvas graphCanvas;

    private SimulationController simController;

    private VBox detailsPanel;
    private Label panelTitle;
    private Label stressValue, fireValue, agentsValue, capacityValue, congestionValue, widthValue, lengthValue;
    private Label avgStressValue, dominantStateValue, histTotalValue, histMaxCongValue, histAvgCongValue;
    private Label nodeInfoValue; // position + exit status pour les nœuds
    private ProgressBar stressBar, fireBar, congestionBar;
    private VBox widthBox, lengthBox, agentStatsBox, historyBox, nodeInfoBox;

    private SimulationStatsPanel statsPanel;

    public MainController(Simulation simulation) {
        this.root = new BorderPane();
        this.root.getStyleClass().add("main-pane");

        initToolBar();
        initCenterPane();
        initDetailsPanel();

        // Panneau de statistiques globales à gauche
        statsPanel = new SimulationStatsPanel();
        root.setLeft(statsPanel);

        // Initialisation de la boucle (qui injecte au canvas le rendu)
        this.simController = new SimulationController(simulation, graphCanvas);
        this.simController.setOnRender(this::refreshStatsPanel);
        this.simController.startLoop(); // Démarre l'update Graphique, la simu elle, reste en Pause !
    }

    private void initToolBar() {
        ToolBar toolBar = new ToolBar();
        toolBar.getStyleClass().add("custom-toolbar");

        Button playBtn = new Button("▶ Play");
        Button pauseBtn = new Button("⏸ Pause");
        Button resetBtn = new Button("🔄 Reset");

        playBtn.getStyleClass().addAll("action-btn", "play-btn");
        pauseBtn.getStyleClass().addAll("action-btn", "pause-btn");
        resetBtn.getStyleClass().addAll("action-btn", "danger-btn");

        // Ajout de la logique asynchrone des boutons !
        playBtn.setOnAction(e -> simController.play());
        pauseBtn.setOnAction(e -> simController.pause());
        resetBtn.setOnAction(e -> simController.reset());

        toolBar.getItems().addAll(playBtn, pauseBtn, resetBtn);
        root.setTop(toolBar);
    }

    private void initCenterPane() {
        Pane canvasContainer = new Pane();
        canvasContainer.getStyleClass().add("canvas-container");

        graphCanvas = new GraphCanvas(); // <--- Nouvelle classe personnalisée
        graphCanvas.widthProperty().bind(canvasContainer.widthProperty());
        graphCanvas.heightProperty().bind(canvasContainer.heightProperty());

        // Ajout de la logique de Caméra : Drag (Pan) et Scroll (Zoom)
        setupCameraControls();

        graphCanvas.setOnMouseClicked(this::handleCanvasClick);

        canvasContainer.getChildren().add(graphCanvas);
        root.setCenter(canvasContainer);
    }

    private void initDetailsPanel() {
        detailsPanel = new VBox(20); // Plus d'espace entre les éléments
        detailsPanel.getStyleClass().add("details-panel");
        detailsPanel.setPrefWidth(300); // Panneau légèrement plus large

        panelTitle = new Label("SELECT AN ELEMENT");
        panelTitle.getStyleClass().add("panel-title");

        // Initialisation des valeurs
        stressValue = new Label("0.0");
        stressValue.getStyleClass().add("stat-value");
        fireValue = new Label("0.0");
        fireValue.getStyleClass().add("stat-value");
        agentsValue = new Label("0");
        agentsValue.getStyleClass().add("stat-value-highlight");
        capacityValue = new Label("0");
        capacityValue.getStyleClass().add("stat-value");
        congestionValue = new Label("0%");
        congestionValue.getStyleClass().add("stat-value");
        widthValue = new Label("0");
        widthValue.getStyleClass().add("stat-value");
        lengthValue = new Label("0");
        lengthValue.getStyleClass().add("stat-value");

        // Initialisation des barres de progression
        stressBar = new ProgressBar(0);
        stressBar.setMaxWidth(Double.MAX_VALUE);
        stressBar.getStyleClass().add("stress-bar");

        fireBar = new ProgressBar(0);
        fireBar.setMaxWidth(Double.MAX_VALUE);
        fireBar.getStyleClass().add("fire-bar");

        congestionBar = new ProgressBar(0);
        congestionBar.setMaxWidth(Double.MAX_VALUE);
        congestionBar.getStyleClass().add("congestion-bar");
        congestionBar.getStyleClass().add("progress-bar"); // fallback

        // Construction des encarts (Cartes)
        VBox capacityBox = createStatBox("CAPACITY", capacityValue);
        VBox congestionBox = createStatBox("CONGESTION", congestionValue);
        congestionBox.getChildren().add(congestionBar);

        VBox stressBox = createStatBox("STRESS LEVEL", stressValue);
        stressBox.getChildren().add(stressBar);

        VBox fireBox = createStatBox("FIRE INTENSITY", fireValue);
        fireBox.getChildren().add(fireBar);

        VBox agentsBox = createStatBox("AGENTS ON SITE", agentsValue);

        widthBox = createStatBox("WIDTH", widthValue);
        lengthBox = createStatBox("LENGTH", lengthValue);

        // Nouvelles cartes : info nœud, stats agents, historique
        nodeInfoValue = new Label("--");
        nodeInfoValue.getStyleClass().add("stat-value");
        nodeInfoBox = createStatBox("NODE INFO", nodeInfoValue);

        avgStressValue = new Label("--");
        avgStressValue.getStyleClass().add("stat-value");
        dominantStateValue = new Label("--");
        dominantStateValue.getStyleClass().add("stat-value");
        agentStatsBox = new VBox(8);
        agentStatsBox.getStyleClass().add("stat-box");
        Label agentStatsTitle = new Label("AGENT STATS");
        agentStatsTitle.getStyleClass().add("stat-title");
        agentStatsBox.getChildren().addAll(agentStatsTitle, avgStressValue, dominantStateValue);

        histTotalValue    = new Label("--");
        histMaxCongValue  = new Label("--");
        histAvgCongValue  = new Label("--");
        for (Label l : new Label[]{histTotalValue, histMaxCongValue, histAvgCongValue})
            l.getStyleClass().add("stat-value");
        historyBox = new VBox(8);
        historyBox.getStyleClass().add("stat-box");
        Label histTitle = new Label("HISTORY");
        histTitle.getStyleClass().add("stat-title");
        historyBox.getChildren().addAll(histTitle, histTotalValue, histMaxCongValue, histAvgCongValue);

        // Masquer toutes les nouvelles cartes par défaut
        for (VBox box : new VBox[]{widthBox, lengthBox, nodeInfoBox, agentStatsBox, historyBox}) {
            box.setVisible(false);
            box.setManaged(false);
        }

        // Spacer pour pousser les panels
        Pane spacer = new Pane();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        detailsPanel.getChildren().addAll(
                panelTitle, new Separator(),
                nodeInfoBox,
                capacityBox, congestionBox, stressBox, fireBox, agentsBox,
                agentStatsBox,
                widthBox, lengthBox,
                historyBox,
                spacer);
        ScrollPane scrollPane = new ScrollPane(detailsPanel);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        root.setRight(scrollPane);
    }

    private VBox createStatBox(String titleText, javafx.scene.Node valueNode) {
        VBox box = new VBox(8);
        box.getStyleClass().add("stat-box");
        Label title = new Label(titleText);
        title.getStyleClass().add("stat-title");
        box.getChildren().addAll(title, valueNode);
        return box;
    }

    /**
     * Permet de trouver l'entité la plus proche des coordonnées cliquées
     */
    private void handleCanvasClick(MouseEvent event) {
        if (event.isStillSincePress()) {
            double mx = (event.getX() - graphCanvas.getPanX()) / graphCanvas.getZoom();
            double my = (event.getY() - graphCanvas.getPanY()) / graphCanvas.getZoom();
            updateDetailsPanel(findClosestElement(mx, my));
        }
    }



    /** Retourne le nœud ou l'arête le plus proche des coordonnées monde (mx, my). */
    private GraphElement findClosestElement(double mx, double my) {
        GraphElement closest = null;
        double minDistance = Double.MAX_VALUE;

        // 1. Nœuds en priorité
        for (Node node : simController.getSimulation().getGraph().getNodes()) {
            double distance = Math.hypot(node.getX() - mx, node.getY() - my);
            if (distance < 30 && distance < minDistance) {
                minDistance = distance;
                closest = node;
            }
        }

        // 2. Arêtes si aucun nœud trouvé
        if (closest == null) {
            for (Edge edge : simController.getSimulation().getGraph().getEdges()) {
                double x1 = edge.getStart().getX(), y1 = edge.getStart().getY();
                double x2 = edge.getEnd().getX(),   y2 = edge.getEnd().getY();

                double C = x2 - x1, D = y2 - y1;
                double len_sq = C * C + D * D;
                double param = len_sq != 0 ? ((mx - x1) * C + (my - y1) * D) / len_sq : -1;

                double xx = param < 0 ? x1 : (param > 1 ? x2 : x1 + param * C);
                double yy = param < 0 ? y1 : (param > 1 ? y2 : y1 + param * D);

                double distance = Math.hypot(mx - xx, my - yy);
                double tolerance = Math.max(10, edge.getWidth() / 2 + 5);
                if (distance < tolerance && distance < minDistance) {
                    minDistance = distance;
                    closest = edge;
                }
            }
        }
        return closest;
    }

    // --- VARIABLES ET METHODE POUR LA CAMÉRA & LE DRAG & DROP ---
    private double lastMouseX;
    private double lastMouseY;
    private Node draggedNode = null; // Élément en cours de déplacement

    private void setupCameraControls() {
        // Clic initial pour retenir la position de la souris
        graphCanvas.setOnMousePressed(event -> {
            lastMouseX = event.getX();
            lastMouseY = event.getY();

            // On vérifie si on clique sur un Noeud existant (pour le glisser)
            double mx = (event.getX() - graphCanvas.getPanX()) / graphCanvas.getZoom();
            double my = (event.getY() - graphCanvas.getPanY()) / graphCanvas.getZoom();

            draggedNode = null;
            double minDistance = Double.MAX_VALUE;

            for (Node node : simController.getSimulation().getGraph().getNodes()) {
                double distance = Math.hypot(node.getX() - mx, node.getY() - my);
                if (distance < 30 && distance < minDistance) {
                    minDistance = distance;
                    draggedNode = node;
                }
            }
        });

        // Déplacement de la souris tout en maintenant le clic (Panning ou Drag d'un
        // noeud)
        graphCanvas.setOnMouseDragged(event -> {
            if (draggedNode != null) {
                // Modification des coordonnées du noeud (Visuel)
                double mx = (event.getX() - graphCanvas.getPanX()) / graphCanvas.getZoom();
                double my = (event.getY() - graphCanvas.getPanY()) / graphCanvas.getZoom();

                draggedNode.setX(mx);
                draggedNode.setY(my);

                // Mise à jour de la distance logique de toutes les arêtes connectées !!
                for (Edge edge : draggedNode.getEdges()) {
                    Node start = edge.getStart();
                    Node end = edge.getEnd();
                    double newLength = Math.hypot(start.getX() - end.getX(), start.getY() - end.getY());
                    edge.setLength(newLength);
                }

                // (Optionnel) Mettre à jour le panneau de droite si on glisse l'élément
                // sélectionné
                updateDetailsPanel(draggedNode);
            } else {
                // Panning classique de la Caméra
                double dx = event.getX() - lastMouseX;
                double dy = event.getY() - lastMouseY;

                graphCanvas.setPanX(graphCanvas.getPanX() + dx);
                graphCanvas.setPanY(graphCanvas.getPanY() + dy);

                lastMouseX = event.getX();
                lastMouseY = event.getY();
            }
        });

        // Relâchement du clic
        graphCanvas.setOnMouseReleased(event -> {
            draggedNode = null;
        });

        // Utilisation de la molette pour le Zoom
        graphCanvas.setOnScroll(event -> {
            double zoomFactor = 1.05;
            if (event.getDeltaY() < 0) {
                zoomFactor = 1 / zoomFactor; // Dézoom
            }

            double oldZoom = graphCanvas.getZoom();
            double newZoom = oldZoom * zoomFactor;

            // On limite le zoom pour éviter des valeurs extrêmes (trop près ou trop loin)
            newZoom = Math.max(0.2, Math.min(newZoom, 5.0));

            // On ajuste le PanX et PanY pour que le zoom s'effectue "sous la souris" et non
            // pas en haut à gauche
            double f = (newZoom / oldZoom) - 1;
            double dx = (event.getX() - graphCanvas.getPanX()) * f;
            double dy = (event.getY() - graphCanvas.getPanY()) * f;

            graphCanvas.setPanX(graphCanvas.getPanX() - dx);
            graphCanvas.setPanY(graphCanvas.getPanY() - dy);
            graphCanvas.setZoom(newZoom);
        });
    }

    /**
     * Met à jour dynamiquement le panneau de détails de droite.
     */
    private void updateDetailsPanel(GraphElement element) {
        // Masquer toutes les boites optionnelles par défaut
        for (VBox box : new VBox[]{widthBox, lengthBox, nodeInfoBox, agentStatsBox, historyBox}) {
            box.setVisible(false);
            box.setManaged(false);
        }

        if (element == null) {
            panelTitle.setText("ELEMENT DETAILS");
            capacityValue.setText("--");
            congestionValue.setText("--");
            congestionBar.setProgress(0);
            stressValue.setText("--");
            stressBar.setProgress(0);
            fireValue.setText("--");
            fireBar.setProgress(0);
            agentsValue.setText("--");
            return;
        }

        // Titre
        String typeName = element instanceof Node ? "NODE" : "EDGE";
        panelTitle.setText(typeName + " #" + element.getId());

        // Capacité et congestion (formule corrigée)
        capacityValue.setText(String.format("%.1f", element.getCapacity()));
        double congestion = element.getCongestion();
        congestionValue.setText(String.format("%.1f%%", congestion * 100));
        congestionBar.setProgress(Math.min(1.0, congestion));

        // Stress
        double stress = element.getStressInducingImpact();
        stressValue.setText(String.format("%.0f%%", stress * 100));
        stressBar.setProgress(Math.min(1.0, stress));

        // Feu
        if (element.isOnFire()) {
            fireValue.setText(String.format("%.2f (fumée %.2f)",
                    element.getFire().getIntensity(), element.getFire().getSmokeLevel()));
            fireBar.setProgress(Math.min(1.0, element.getFire().getIntensity()));
        } else {
            fireValue.setText("None");
            fireBar.setProgress(0);
        }

        // Agents présents
        java.util.List<Agent> agents = element.getAgents();
        agentsValue.setText(String.valueOf(agents.size()));

        // Infos spécifiques nœud
        if (element instanceof Node node) {
            nodeInfoValue.setText(String.format("(%.0f, %.0f)  %s",
                    node.getX(), node.getY(), node.isExit() ? "★ SORTIE" : "nœud"));
            setBoxVisible(nodeInfoBox, true);

            widthBox.setVisible(false);
            widthBox.setManaged(false);
            lengthBox.setVisible(false);
            lengthBox.setManaged(false);
        }

        // Infos spécifiques arête
        if (element instanceof Edge edge) {
            widthValue.setText(String.format("%.2f", edge.getWidth()));
            lengthValue.setText(String.format("%.2f", edge.getLength()));
            setBoxVisible(widthBox, true);
            setBoxVisible(lengthBox, true);
            nodeInfoBox.setVisible(false);
            nodeInfoBox.setManaged(false);
        }

        // Stats agents (stress moyen + état dominant)
        if (!agents.isEmpty()) {
            double avgStress = agents.stream().mapToDouble(Agent::getStressLevel).average().orElse(0);
            avgStressValue.setText("Stress moy. : " + String.format("%.0f%%", avgStress * 100));
            EmotionalState dominant = dominantState(agents);
            dominantStateValue.setText("État : " + dominant.name());
            setBoxVisible(agentStatsBox, true);
        }

        // Historique cumulé
        histTotalValue.setText("Total passés : " + element.getTotalAgentsCount());
        histMaxCongValue.setText("Congestion max : " + String.format("%.0f%%", element.getMaxCongestion() * 100));
        histAvgCongValue.setText("Congestion moy. : " + String.format("%.0f%%", element.getAverageCongestion() * 100));
        setBoxVisible(historyBox, true);
    }

    private void setBoxVisible(VBox box, boolean visible) {
        box.setVisible(visible);
        box.setManaged(visible);
    }

    private EmotionalState dominantState(java.util.List<Agent> agents) {
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

    /**
     * Calcule les statistiques globales et met à jour le panneau gauche.
     * Appelé par le game loop à chaque frame.
     */
    private void refreshStatsPanel() {
        Graph graph = simController.getSimulation().getGraph();
        int totalAgents = simController.getSimulation().getAgentManager().getAgents().size();
        int onNodes  = graph.getNodes().stream().mapToInt(n -> n.getAgents().size()).sum();
        int onEdges  = graph.getEdges().stream().mapToInt(e -> e.getAgents().size()).sum();
        int fireNodes = (int) graph.getNodes().stream().filter(Node::isOnFire).count();
        int fireEdges = (int) graph.getEdges().stream().filter(Edge::isOnFire).count();
        double avgCong = graph.getNodes().stream()
                .mapToDouble(n -> n.getCongestion()).average().orElse(0);

        statsPanel.update(
            simController.getSimulation().getCurrentTick(),
            simController.isRunning(),
            totalAgents, onNodes, onEdges,
            fireNodes, fireEdges,
            graph.getNodes().size(), graph.getEdges().size(), graph.getExits().size(),
            avgCong
        );
    }

    public BorderPane getRoot() {
        return root;
    }
}