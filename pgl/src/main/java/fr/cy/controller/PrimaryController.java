package fr.cy.controller;

import fr.cy.model.agent.Agent;
import fr.cy.model.fire.Fire;
import fr.cy.model.graph.Graph;
import fr.cy.model.graph.element.Edge;
import fr.cy.model.graph.element.Node;
import fr.cy.model.pathfinding.PathFinder;
import fr.cy.model.simulation.Simulation;
import fr.cy.view.AgentTransit;
import fr.cy.view.EdgeInfoPanel;
import fr.cy.view.GraphView;
import fr.cy.view.NodeInfoPanel;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PrimaryController {

    @FXML private Pane       graphContainer;
    @FXML private BorderPane root;
    @FXML private Button     playPauseButton;
    @FXML private Button     stepButton;
    @FXML private Label      tickLabel;
    @FXML private Slider     speedSlider;
    @FXML private Label      speedLabel;

    private Simulation    simulation;
    private GraphView     graphView;
    private Timeline      timeline;
    private int           localTick = 0;

    private final List<AgentTransit> transits = new ArrayList<>();

    /** Panneaux d'info (gardés pour les rafraîchir à chaque tick) */
    private NodeInfoPanel nodeInfoPanel;
    private EdgeInfoPanel edgeInfoPanel;

    /** Sélection courante : le nœud ou l'arête sur lequel on a cliqué */
    private Node selectedNode = null;
    private Edge selectedEdge = null;

    /** Overlay "simulation terminée" */
    private Label completionLabel;
    private int   initialAgentCount = 0;
    private boolean simulationCompleted = false;

    @FXML
    public void initialize() {
        setupSimulation();

        speedSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            timeline.setRate(newVal.doubleValue());
            speedLabel.setText(String.format("x%.2g", newVal.doubleValue()));
        });
    }

    // -----------------------------------------------------------------------
    // Setup / Reset
    // -----------------------------------------------------------------------

    private void setupSimulation() {
        if (timeline != null) timeline.stop();
        transits.clear();
        selectedNode = null;
        selectedEdge = null;
        simulationCompleted = false;
        localTick = 0;
        if (tickLabel       != null) tickLabel.setText("0");
        if (playPauseButton != null) playPauseButton.setText("▶  Play");

        // --- 1. Graphe ---
        Graph graph = new Graph();

        Node n1 = graph.createNode(150, 150);
        Node n2 = graph.createNode(400, 100);
        Node n3 = graph.createNode(600, 250);
        Node n4 = graph.createNode(400, 400);
        Node n5 = graph.createNode(150, 450);

        Node sortie1 = graph.createNode(700, 100);
        Node sortie2 = graph.createNode(700, 450);
        sortie1.setExit(true);
        sortie2.setExit(true);

        graph.createEdge(n1, n2);
        graph.createEdge(n2, n3);
        graph.createEdge(n3, n4);
        graph.createEdge(n4, n5);
        graph.createEdge(n5, n1);
        graph.createEdge(n2, n4);
        graph.createEdge(n3, sortie1);
        graph.createEdge(n4, sortie2);

        // --- 2. Agents ---
        n1.addAgent(new Agent("Alice", 2, 0.7, 0.6));
        n1.addAgent(new Agent("Bob",   1, 0.3, 0.4));
        n2.addAgent(new Agent("Carol", 3, 0.8, 0.7));
        n3.addAgent(new Agent("Dave",  2, 0.1, 0.2));
        n3.addAgent(new Agent("Eve",   1, 0.5, 0.5));
        n3.addAgent(new Agent("Frank", 2, 0.6, 0.3));

        // Mémoriser le nombre initial d'agents pour détecter la fin
        initialAgentCount = graph.getNodes().stream()
                .mapToInt(n -> n.getAgents().size()).sum();

        // --- 3. Feu sur n1 ---
        n1.setFire(new Fire(1.0, 1.0, 0.3));

        // --- 4. Simulation ---
        simulation = new Simulation(graph);

        // --- 5. Panneaux d'infos ---
        nodeInfoPanel = new NodeInfoPanel();
        edgeInfoPanel = new EdgeInfoPanel();
        root.setRight(nodeInfoPanel);

        // --- 6. Vue du graphe ---
        graphView = new GraphView(graph);
        graphView.setOnNodeClicked(node -> {
            selectedNode = node;
            selectedEdge = null;
            root.setRight(nodeInfoPanel);
            nodeInfoPanel.display(node);
        });
        graphView.setOnEdgeClicked(edge -> {
            selectedEdge = edge;
            selectedNode = null;
            root.setRight(edgeInfoPanel);
            edgeInfoPanel.display(edge);
        });
        graphContainer.getChildren().clear();
        graphContainer.getChildren().add(graphView);

        // GraphView remplit tout le conteneur
        graphView.prefWidthProperty().bind(graphContainer.widthProperty());
        graphView.prefHeightProperty().bind(graphContainer.heightProperty());

        // Label de fin de simulation (invisible par défaut)
        completionLabel = new Label("✓   TOUS LES AGENTS ÉVACUÉS !");
        completionLabel.setMaxWidth(Double.MAX_VALUE);
        completionLabel.setAlignment(Pos.CENTER);
        completionLabel.setStyle(
            "-fx-background-color: #5B7FFF; " +
            "-fx-text-fill: white; " +
            "-fx-font-size: 22; " +
            "-fx-font-weight: bold; " +
            "-fx-padding: 18 0;"
        );
        completionLabel.layoutYProperty().bind(
            graphContainer.heightProperty().divide(2).subtract(35)
        );
        completionLabel.setVisible(false);
        graphContainer.getChildren().add(completionLabel);

        graphView.refresh();

        // --- 7. Timeline ---
        setupTimeline();
    }

    private void setupTimeline() {
        timeline = new Timeline(new KeyFrame(Duration.millis(500), e -> {
            // Propagation du feu
            try {
                simulation.tick();
            } catch (Exception ex) {
                simulation.getFireService().updateFires(simulation.getGraph());
            }

            // Avancer les agents en transit
            advanceTransits();

            // Mettre à jour le stress
            updateStress(simulation.getGraph());

            // Rafraîchir le panneau ici : les transits complétés ont ajouté
            // les agents sur les nœuds, et les arêtes ont encore leurs agents en transit
            refreshInfoPanel();

            // Vérifier si tous les agents ont évacué
            checkCompletion(simulation.getGraph());

            // Lancer de nouveaux mouvements
            moveAgents(simulation.getGraph(), simulation.getPathFinder());

            localTick++;
            tickLabel.setText(String.valueOf(localTick));
            graphView.refresh();
            graphView.redrawAgents();
            graphView.drawTransitAgents(transits);
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
    }

    // -----------------------------------------------------------------------
    // Rafraîchissement du panneau d'info
    // -----------------------------------------------------------------------

    /**
     * Vérifie si tous les agents ont quitté le graphe.
     * Condition : aucun agent sur les nœuds ET aucun transit en cours.
     * Quand c'est le cas : arrête la simulation et affiche le message de fin.
     */
    private void checkCompletion(Graph graph) {
        if (simulationCompleted || initialAgentCount == 0) return;

        boolean nodesEmpty    = graph.getNodes().stream().allMatch(n -> n.getAgents().isEmpty());
        boolean transitsEmpty = transits.isEmpty();

        if (nodesEmpty && transitsEmpty) {
            simulationCompleted = true;
            simulation.stop();
            timeline.stop();
            playPauseButton.setText("▶  Play");
            completionLabel.setVisible(true);
        }
    }

    /**
     * Si l'utilisateur a cliqué sur un nœud ou une arête,
     * le panneau d'info se met à jour automatiquement à chaque tick.
     */
    private void refreshInfoPanel() {
        if (selectedNode != null) {
            nodeInfoPanel.display(selectedNode);
        } else if (selectedEdge != null) {
            edgeInfoPanel.display(selectedEdge);
        }
    }

    // -----------------------------------------------------------------------
    // Transit
    // -----------------------------------------------------------------------

    /**
     * Avance chaque transit d'un STEP.
     * Quand progress >= 1.0 :
     *   - l'agent est retiré de l'arête du modèle
     *   - il est ajouté au nœud de destination (ou retiré si c'est une sortie)
     */
    private void advanceTransits() {
        List<AgentTransit> completed = new ArrayList<>();
        for (AgentTransit t : transits) {
            t.progress += AgentTransit.STEP;
            if (t.isCompleted()) completed.add(t);
        }
        transits.removeAll(completed);
        for (AgentTransit t : completed) {
            // Retirer l'agent de l'arête dans le modèle
            Edge edge = t.from.getEdgeTo(t.to);
            if (edge != null) edge.removeAgent(t.agent);

            if (!t.to.isExit()) {
                t.to.addAgent(t.agent);
            }
            // si sortie → agent retiré définitivement
        }
    }

    // -----------------------------------------------------------------------
    // Stress
    // -----------------------------------------------------------------------

    private void updateStress(Graph graph) {
        for (Node node : graph.getNodes()) {
            double delta = 0;
            if (node.isOnFire())      delta += 0.12;
            delta += node.getCongestion() * 0.06;
            delta = Math.min(delta, 0.15);

            for (Agent agent : node.getAgents()) {
                double newStress = Math.min(1.0, agent.getStressLevel() + delta);
                agent.setStressLevel(newStress);
                agent.updateState();
            }
        }
        for (AgentTransit transit : transits) {
            double delta = 0;
            if (transit.from.isOnFire()) delta += 0.06;
            delta += transit.from.getCongestion() * 0.03;
            double newStress = Math.min(1.0, transit.agent.getStressLevel() + delta);
            transit.agent.setStressLevel(newStress);
            transit.agent.updateState();
        }
    }

    // -----------------------------------------------------------------------
    // Mouvement des agents
    // -----------------------------------------------------------------------

    /**
     * Pour chaque agent sur un nœud, crée un AgentTransit vers le prochain nœud.
     * L'agent est immédiatement ajouté à l'arête du modèle (edge.addAgent)
     * afin que le panneau d'info montre le vrai nombre d'agents sur l'arête.
     */
    private void moveAgents(Graph graph, PathFinder pathFinder) {
        Map<Agent, Node> origins      = new LinkedHashMap<>();
        Map<Agent, Node> destinations = new LinkedHashMap<>();

        for (Node node : graph.getNodes()) {
            for (Agent agent : new ArrayList<>(node.getAgents())) {
                origins.put(agent, node);
                if (node.isExit()) {
                    destinations.put(agent, null);
                } else {
                    Node next = getNextNodeTowardExit(node, graph, pathFinder);
                    destinations.put(agent, next);
                }
            }
        }

        for (Agent agent : origins.keySet()) {
            Node origin = origins.get(agent);
            Node dest   = destinations.get(agent);
            origin.removeAgent(agent);
            if (dest != null) {
                // Enregistrer l'agent sur l'arête dans le modèle
                Edge edge = origin.getEdgeTo(dest);
                if (edge != null) edge.addAgent(agent);
                transits.add(new AgentTransit(agent, origin, dest));
            }
        }
    }

    private Node getNextNodeTowardExit(Node node, Graph graph, PathFinder pathFinder) {
        Node   bestNext = null;
        double bestDist = Double.MAX_VALUE;

        for (Node exit : graph.getExits()) {
            List<Node> path = pathFinder.shortestPath(node, exit);
            if (path.size() >= 2) {
                double dist = computePathLength(path);
                if (dist < bestDist) {
                    bestDist = dist;
                    bestNext = path.get(1);
                }
            }
        }
        return bestNext;
    }

    private double computePathLength(List<Node> path) {
        double dist = 0;
        for (int i = 0; i < path.size() - 1; i++) {
            Edge edge = path.get(i).getEdgeTo(path.get(i + 1));
            if (edge != null) dist += edge.getLength();
        }
        return dist;
    }

    // -----------------------------------------------------------------------
    // Boutons FXML
    // -----------------------------------------------------------------------

    @FXML
    private void onPlayPause() {
        if (simulation.isRunning()) {
            simulation.stop();
            timeline.stop();
            playPauseButton.setText("▶ Play");
        } else {
            simulation.start();
            timeline.play();
            playPauseButton.setText("⏸ Pause");
        }
    }

    @FXML
    private void onStep() {
        simulation.getFireService().updateFires(simulation.getGraph());
        advanceTransits();
        updateStress(simulation.getGraph());
        refreshInfoPanel();
        moveAgents(simulation.getGraph(), simulation.getPathFinder());
        localTick++;
        tickLabel.setText(String.valueOf(localTick));
        graphView.refresh();
        graphView.redrawAgents();
        graphView.drawTransitAgents(transits);
    }

    @FXML
    private void onReset() {
        setupSimulation();
    }
}
