package fr.cy.controller;

import fr.cy.model.agent.Agent;
import fr.cy.model.fire.Fire;
import fr.cy.model.graph.Graph;
import fr.cy.model.graph.element.Edge;
import fr.cy.model.graph.element.Node;
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
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

public class PrimaryController {

    @FXML private Pane       graphContainer;
    @FXML private BorderPane root;
    @FXML private Button     playPauseButton;
    @FXML private Button     stepButton;
    @FXML private Label      tickLabel;
    @FXML private Slider     speedSlider;
    @FXML private Label      speedLabel;

    private Simulation simulation;
    private GraphView  graphView;
    private Timeline   timeline;
    private int        localTick = 0;

    private final List<AgentTransit> transits = new ArrayList<>();

    private NodeInfoPanel nodeInfoPanel;
    private EdgeInfoPanel edgeInfoPanel;

    private Node    selectedNode = null;
    private Edge    selectedEdge = null;

    private Label   completionLabel;
    private int     initialAgentCount  = 0;
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

    private void setupSimulation() {
        if (timeline != null) timeline.stop();
        transits.clear();
        selectedNode = null;
        selectedEdge = null;
        simulationCompleted = false;
        localTick = 0;
        if (tickLabel       != null) tickLabel.setText("0");
        if (playPauseButton != null) playPauseButton.setText("▶  Play");

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

        // Agents avec des vitesses variees (1=lent, 2=moyen, 3=rapide)
        n1.addAgent(new Agent("Alice", 2, 0.7, 0.6));
        n1.addAgent(new Agent("Bob",   1, 0.3, 0.4));
        n2.addAgent(new Agent("Carol", 3, 0.8, 0.7));
        n3.addAgent(new Agent("Dave",  2, 0.1, 0.2));
        n3.addAgent(new Agent("Eve",   1, 0.5, 0.5));
        n3.addAgent(new Agent("Frank", 3, 0.6, 0.3));

        initialAgentCount = graph.getNodes().stream()
                .mapToInt(n -> n.getAgents().size()).sum();

        // Feu sur n1 : spreadRate modere pour que les agents vitesse 2+ puissent echapper
        n1.setFire(new Fire(1.0, 1.0, 0.2));

        simulation = new Simulation(graph);

        nodeInfoPanel = new NodeInfoPanel();
        edgeInfoPanel = new EdgeInfoPanel();
        root.setRight(nodeInfoPanel);

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

        graphView.prefWidthProperty().bind(graphContainer.widthProperty());
        graphView.prefHeightProperty().bind(graphContainer.heightProperty());

        completionLabel = new Label("TOUS LES AGENTS EVACUES !");
        completionLabel.setMaxWidth(Double.MAX_VALUE);
        completionLabel.setAlignment(Pos.CENTER);
        completionLabel.setStyle(
            "-fx-background-color: #5B7FFF; -fx-text-fill: white; " +
            "-fx-font-size: 22; -fx-font-weight: bold; -fx-padding: 18 0;"
        );
        completionLabel.layoutYProperty().bind(
            graphContainer.heightProperty().divide(2).subtract(35)
        );
        completionLabel.setVisible(false);
        graphContainer.getChildren().add(completionLabel);

        graphView.refresh();
        setupTimeline();
    }

    // -----------------------------------------------------------------------

    private void setupTimeline() {
        timeline = new Timeline(new KeyFrame(Duration.millis(500), e -> doTick()));
        timeline.setCycleCount(Timeline.INDEFINITE);
    }

    private void doTick() {
        // 1. Feu en premier : les agents verront l'etat ACTUEL du feu pour choisir leur chemin
        simulation.getFireService().updateFires(simulation.getGraph());

        // 2. Deplacement : Dijkstra evite les noeuds/aretes EN FEU
        //    Si bloque de tous les cotes, l'agent reste sur place (ne disparait pas)
        moveAgents(simulation.getGraph());

        // 3. Avancer les transits : agents arrives rejoignent leurs noeuds
        advanceTransits();

        // 4. Stress
        updateStress(simulation.getGraph());

        // 5. Panneau d'info
        refreshInfoPanel();

        // 6. Fin de simulation ?
        checkCompletion(simulation.getGraph());

        localTick++;
        tickLabel.setText(String.valueOf(localTick));
        graphView.refresh();
        graphView.redrawAgents();
        graphView.drawTransitAgents(transits);
    }

    // -----------------------------------------------------------------------

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

    private void refreshInfoPanel() {
        if (selectedNode != null)      nodeInfoPanel.display(selectedNode);
        else if (selectedEdge != null) edgeInfoPanel.display(selectedEdge);
    }

    // -----------------------------------------------------------------------

    private void advanceTransits() {
        List<AgentTransit> completed = new ArrayList<>();
        for (AgentTransit t : transits) {
            t.progress += t.step;
            if (t.isCompleted()) completed.add(t);
        }
        transits.removeAll(completed);
        for (AgentTransit t : completed) {
            Edge edge = t.from.getEdgeTo(t.to);
            if (edge != null) edge.removeAgent(t.agent);
            if (!t.to.isExit()) t.to.addAgent(t.agent);
        }
    }

    private void updateStress(Graph graph) {
        for (Node node : graph.getNodes()) {
            double delta = node.isOnFire() ? 0.12 : 0;
            delta += node.getCongestion() * 0.06;
            delta  = Math.min(delta, 0.15);
            for (Agent agent : node.getAgents()) {
                agent.setStressLevel(Math.min(1.0, agent.getStressLevel() + delta));
            }
        }
        for (AgentTransit t : transits) {
            double delta = t.from.isOnFire() ? 0.06 : 0;
            delta += t.from.getCongestion() * 0.03;
            t.agent.setStressLevel(Math.min(1.0, t.agent.getStressLevel() + delta));
        }
    }

    private void moveAgents(Graph graph) {
        Map<Agent, Node> origins      = new LinkedHashMap<>();
        Map<Agent, Node> destinations = new LinkedHashMap<>();

        for (Node node : graph.getNodes()) {
            for (Agent agent : new ArrayList<>(node.getAgents())) {
                origins.put(agent, node);
                if (node.isExit()) {
                    destinations.put(agent, null); // sortie → agent quitte le graphe
                } else {
                    // null si tous les chemins sont bloques par le feu → agent reste sur place
                    destinations.put(agent, getNextNodeTowardExit(node, graph));
                }
            }
        }

        for (Agent agent : origins.keySet()) {
            Node origin = origins.get(agent);
            Node dest   = destinations.get(agent);

            if (origin.isExit()) {
                // L'agent a atteint une sortie : il quitte la simulation
                origin.removeAgent(agent);
            } else if (dest != null) {
                // L'agent se deplace vers le prochain noeud accessible
                origin.removeAgent(agent);
                Edge edge = origin.getEdgeTo(dest);
                if (edge != null) edge.addAgent(agent);
                transits.add(new AgentTransit(agent, origin, dest));
            }
            // dest == null ET pas une sortie → feu bloque tout → l'agent reste sur son noeud
        }
    }

    /**
     * Dijkstra qui évite strictement les arêtes et noeuds en feu.
     * Les agents ne peuvent PAS entrer dans un noeud ou arête en feu.
     */
    private Node getNextNodeTowardExit(Node node, Graph graph) {
        if (node.isExit()) return null;

        Map<Node, Double> dist    = new HashMap<>();
        Map<Node, Node>   parent  = new HashMap<>();
        Set<Node>         visited = new HashSet<>();
        PriorityQueue<Node> pq    = new PriorityQueue<>(
            Comparator.comparingDouble(n -> dist.getOrDefault(n, Double.MAX_VALUE))
        );

        dist.put(node, 0.0);
        pq.add(node);

        while (!pq.isEmpty()) {
            Node current = pq.poll();
            if (visited.contains(current)) continue;
            visited.add(current);

            if (current.isExit() && !current.equals(node)) {
                // Sortie trouvee : remonter au premier pas depuis le noeud de depart
                Node step = current;
                while (parent.containsKey(step) && !parent.get(step).equals(node)) {
                    step = parent.get(step);
                }
                return step;
            }

            for (Edge edge : current.getEdges()) {
                // Regle stricte : JAMAIS dans une arete en feu
                if (edge.isOnFire()) continue;

                Node neighbor = edge.getOppositeNode(current);
                if (neighbor == null || visited.contains(neighbor)) continue;

                // Regle stricte : JAMAIS dans un noeud en feu (sauf sortie, mais sorties ne brulent pas)
                if (neighbor.isOnFire()) continue;

                // Cout = longueur + penalite de congestion (prefere les chemins moins encombres)
                double cost = edge.getLength() + neighbor.getCongestion() * 5.0;
                double newDist = dist.getOrDefault(current, Double.MAX_VALUE) + cost;

                if (newDist < dist.getOrDefault(neighbor, Double.MAX_VALUE)) {
                    dist.put(neighbor, newDist);
                    parent.put(neighbor, current);
                    pq.add(neighbor);
                }
            }
        }

        return null; // Aucun chemin sans feu → agent reste bloque sur place
    }

    // -----------------------------------------------------------------------

    @FXML
    private void onPlayPause() {
        if (simulation.isRunning()) {
            simulation.stop();
            timeline.stop();
            playPauseButton.setText("▶  Play");
        } else {
            simulation.start();
            timeline.play();
            playPauseButton.setText("⏸  Pause");
        }
    }

    @FXML
    private void onStep() {
        doTick();
    }

    @FXML
    private void onReset() {
        setupSimulation();
    }
}
