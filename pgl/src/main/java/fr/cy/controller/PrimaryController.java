package fr.cy.controller;

import fr.cy.model.graph.Graph;
import fr.cy.model.graph.element.Node;
import fr.cy.view.GraphView;
import fr.cy.view.NodeInfoPanel;
import javafx.fxml.FXML;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;

/**
 * Contrôleur de la vue principale.
 *
 * Crée un graphe de test, l'affiche dans le Pane central,
 * et branche le clic sur un nœud pour afficher ses infos à droite.
 */
public class PrimaryController {

    /** Le Pane central injecté depuis primary.fxml */
    @FXML
    private Pane graphContainer;

    /** Le BorderPane racine — permet d'ajouter le panneau d'infos à droite */
    @FXML
    private BorderPane root;

    @FXML
    public void initialize() {
        // --- 1. Créer le graphe (modèle) ---
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

        // --- 2. Créer le panneau d'infos et l'ajouter à droite ---
        NodeInfoPanel infoPanel = new NodeInfoPanel();
        root.setRight(infoPanel);

        // --- 3. Créer la vue du graphe ---
        GraphView graphView = new GraphView(graph);

        // Quand on clique sur un nœud → mettre à jour le panneau d'infos
        graphView.setOnNodeClicked(node -> infoPanel.display(node));

        graphContainer.getChildren().add(graphView);
    }
}
