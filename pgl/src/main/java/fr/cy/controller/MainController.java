package fr.cy.controller;

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

/**
 * Contrôleur principal de l'application JavaFX.
 * Crée un graphe de démonstration et affiche la vue interactive.
 */
public class MainController {

    @FXML
    private Pane graphContainer;

    @FXML
    private BorderPane root;

    @FXML
    public void initialize() {
        Graph graph = buildDemoGraph();

        NodeInfoPanel infoPanel = new NodeInfoPanel();
        root.setRight(infoPanel);

        GraphView graphView = new GraphView(graph);
        graphView.setOnNodeClicked(infoPanel::display);
        graphContainer.getChildren().add(graphView);
    }

    private Graph buildDemoGraph() {
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

        n4.setFire(new Fire(0.5, 0.5, 0.2));

        graph.createEdge(n1, n2);
        graph.createEdge(n2, n3);
        graph.createEdge(n3, n4);
        graph.createEdge(n4, n5);
        graph.createEdge(n5, n1);
        graph.createEdge(n2, n4);
        graph.createEdge(n3, sortie1);
        graph.createEdge(n4, sortie2);

        return graph;
    }
}