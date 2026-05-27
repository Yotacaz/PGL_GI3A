package fr.cy.view;

import fr.cy.model.graph.Graph;
import fr.cy.model.graph.element.Edge;
import fr.cy.model.graph.element.Node;
import javafx.scene.layout.Pane;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Vue du graphe entier.
 *
 * GraphView est un Pane JavaFX qui contient tous les EdgeView et NodeView.
 * Les arêtes sont liées aux nœuds par des bindings JavaFX :
 * quand on déplace un nœud, les arêtes le suivent automatiquement.
 */
public class GraphView extends Pane {

    private final Graph graph;

    /** Callback appelé quand on clique sur un nœud. */
    private Consumer<Node> onNodeClicked = node -> {};

    public GraphView(Graph graph) {
        this.graph = graph;
        draw();
    }

    public void setOnNodeClicked(Consumer<Node> handler) {
        this.onNodeClicked = handler;
    }

    /**
     * Dessine le graphe.
     *
     * Ordre :
     * 1. Créer tous les NodeViews et les stocker dans une Map
     * 2. Créer les EdgeViews et lier leurs extrémités aux NodeViews (binding)
     * 3. Ajouter d'abord les arêtes, puis les nœuds (ordre d'affichage)
     */
    private void draw() {

        // 1. Créer tous les NodeViews et les stocker (Node → NodeView)
        Map<Node, NodeView> nodeViewMap = new HashMap<>();

        for (Node node : graph.getNodes()) {
            NodeView nodeView = new NodeView(node);
            nodeView.setOnMouseClicked(event -> onNodeClicked.accept(node));
            nodeViewMap.put(node, nodeView);
        }

        // 2. Créer les EdgeViews et lier leurs extrémités aux NodeViews
        for (Edge edge : graph.getEdges()) {
            EdgeView edgeView = new EdgeView(edge);

            NodeView startView = nodeViewMap.get(edge.getStart());
            NodeView endView   = nodeViewMap.get(edge.getEnd());

            // Binding : les coords de la ligne sont liées aux coords du cercle
            // → quand le cercle bouge, la ligne se met à jour automatiquement
            edgeView.startXProperty().bind(startView.centerXProperty());
            edgeView.startYProperty().bind(startView.centerYProperty());
            edgeView.endXProperty().bind(endView.centerXProperty());
            edgeView.endYProperty().bind(endView.centerYProperty());

            getChildren().add(edgeView); // arêtes en dessous
        }

        // 3. Ajouter les nœuds au dessus des arêtes
        getChildren().addAll(nodeViewMap.values());
    }
}
