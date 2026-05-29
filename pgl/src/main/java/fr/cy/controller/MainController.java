package fr.cy.controller;

import fr.cy.model.fire.Fire;
import fr.cy.model.graph.Graph;
import fr.cy.model.graph.element.Node;
import fr.cy.view.GraphView;
import fr.cy.view.NodeInfoPanel;
import javafx.fxml.FXML;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;

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
