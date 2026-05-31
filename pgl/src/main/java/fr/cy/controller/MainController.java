package fr.cy.controller;

import fr.cy.model.graph.element.Edge;
import fr.cy.model.graph.element.GraphElement;
import fr.cy.model.graph.element.Node;
import fr.cy.model.simulation.Simulation;
import fr.cy.view.GraphCanvas;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Separator;
import javafx.scene.control.ToolBar;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
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
    private ProgressBar stressBar, fireBar, congestionBar;
    private VBox widthBox, lengthBox;

    public MainController(Simulation simulation) {
        this.root = new BorderPane();
        this.root.getStyleClass().add("main-pane");

        initToolBar();
        initCenterPane();
        initDetailsPanel();

        // Initialisation de la boucle (qui injecte au canvas le rendu)
        this.simController = new SimulationController(simulation, graphCanvas);
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

        // Ajout de la logique Clic & Sélection
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

        // Hide width and length boxes initially
        widthBox.setVisible(false);
        widthBox.setManaged(false);
        lengthBox.setVisible(false);
        lengthBox.setManaged(false);

        // Spacer pour pousser les panels
        Pane spacer = new Pane();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        detailsPanel.getChildren().addAll(
                panelTitle, new Separator(),
                capacityBox, congestionBox, stressBox, fireBox, agentsBox,
                widthBox, lengthBox,
                spacer);
        root.setRight(detailsPanel);
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
        // Optionnel : ne pas déclencher le clic si on était en train de glisser (Drag)
        if (event.isStillSincePress()) {
            // Conversion des coordonnées de l'écran (souris) vers les coordonnées du Monde
            // (Modèle)
            double mx = (event.getX() - graphCanvas.getPanX()) / graphCanvas.getZoom();
            double my = (event.getY() - graphCanvas.getPanY()) / graphCanvas.getZoom();

            GraphElement closestElement = null;
            double minDistance = Double.MAX_VALUE;

            // 1. Cherche dans les Nodes en priorité
            for (Node node : simController.getSimulation().getGraph().getNodes()) {
                double distance = Math.hypot(node.getX() - mx, node.getY() - my);
                if (distance < 30 && distance < minDistance) { // Hitbox tolérance
                    minDistance = distance;
                    closestElement = node;
                }
            }

            // 2. Si pas de nœud trouvé, on cherche dans les arêtes (Edges)
            if (closestElement == null) {
                for (Edge edge : simController.getSimulation().getGraph().getEdges()) {
                    Node start = edge.getStart();
                    Node end = edge.getEnd();

                    double x1 = start.getX();
                    double y1 = start.getY();
                    double x2 = end.getX();
                    double y2 = end.getY();

                    // Calcul de la distance d'un point à un segment de droite.
                    double A = mx - x1;
                    double B = my - y1;
                    double C = x2 - x1;
                    double D = y2 - y1;

                    double dot = A * C + B * D;
                    double len_sq = C * C + D * D;
                    double param = -1;

                    if (len_sq != 0) { // S'assurer que le segment a une longueur
                        param = dot / len_sq;
                    }

                    double xx, yy;

                    if (param < 0) {
                        xx = x1;
                        yy = y1;
                    } else if (param > 1) {
                        xx = x2;
                        yy = y2;
                    } else {
                        xx = x1 + param * C;
                        yy = y1 + param * D;
                    }

                    double dx = mx - xx;
                    double dy = my - yy;
                    double distance = Math.sqrt(dx * dx + dy * dy);

                    // Tolérance de clic basée sur la largeur de la route ou fixe
                    double tolerance = Math.max(10, (edge.getWidth() / 2) + 5);
                    if (distance < tolerance && distance < minDistance) {
                        minDistance = distance;
                        closestElement = edge;
                    }
                }
            }

            updateDetailsPanel(closestElement);
        }
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
     * Met à jour dynamiquement la section de droite.
     */
    private void updateDetailsPanel(GraphElement element) {
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
            widthBox.setVisible(false);
            widthBox.setManaged(false);
            lengthBox.setVisible(false);
            lengthBox.setManaged(false);
            return;
        }

        String typeName = element instanceof Node ? "NODE" : "EDGE";
        panelTitle.setText(typeName + " #" + element.getId());

        capacityValue.setText(String.valueOf(element.getCapacity()));

        // Calculate congestion
        double congestion = element.getCapacity() > 0 ? (double) element.getAgents().size() / element.getCapacity() : 0;
        congestionValue.setText(String.format("%.1f%%", congestion * 100));
        congestionBar.setProgress(Math.min(1.0, congestion));

        if (element instanceof Edge) {
            Edge edge = (Edge) element;
            widthBox.setVisible(true);
            widthBox.setManaged(true);
            widthValue.setText(String.format("%.2f", edge.getWidth()));
            lengthBox.setVisible(true);
            lengthBox.setManaged(true);
            lengthValue.setText(String.format("%.2f", edge.getLength()));
        } else {
            widthBox.setVisible(false);
            widthBox.setManaged(false);
            lengthBox.setVisible(false);
            lengthBox.setManaged(false);
        }

        // Arrondi du stress à deux décimales pour un affichage propre
        double stress = Math.round(element.getStressInducingImpact() * 100.0) / 100.0;
        stressValue.setText(String.valueOf(stress));
        stressBar.setProgress(stress);

        String fireTxt = element.isOnFire() ? String.format("%.2f", element.getFire().getIntensity()) : "None";
        fireValue.setText(fireTxt);
        fireBar.setProgress(element.isOnFire() ? element.getFire().getIntensity() : 0);

        agentsValue.setText(String.valueOf(element.getAgents().size()));
    }

    public BorderPane getRoot() {
        return root;
    }
}