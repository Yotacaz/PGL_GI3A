package fr.cy.view;

import fr.cy.model.agent.Agent;
import fr.cy.model.graph.Graph;
import fr.cy.model.graph.element.Edge;
import fr.cy.model.graph.element.Node;
import javafx.scene.Cursor;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Vue du graphe entier.
 *
 * Couches d'affichage (bas → haut) :
 * 1. EdgeViews  — les arêtes
 * 2. NodeViews  — les nœuds
 * 3. Labels ID  — numéros des nœuds
 * 4. AgentViews — les agents
 */
public class GraphView extends Pane {

    private final Graph graph;

    private Consumer<Node> onNodeClicked = node -> {};
    private Consumer<Edge> onEdgeClicked = edge -> {};

    private final Map<Node, NodeView> nodeViewMap       = new HashMap<>();
    private final List<EdgeView>      edgeViews         = new ArrayList<>();
    private final List<AgentView>     agentViews        = new ArrayList<>();
    private final List<AgentView>     transitAgentViews = new ArrayList<>();

    public GraphView(Graph graph) {
        this.graph = graph;
        setStyle("-fx-background-color: #1E1E2E;");
        draw();
    }

    public void setOnNodeClicked(Consumer<Node> handler) { this.onNodeClicked = handler; }
    public void setOnEdgeClicked(Consumer<Edge> handler) { this.onEdgeClicked = handler; }

    private void draw() {

        // 1. NodeViews
        for (Node node : graph.getNodes()) {
            NodeView nodeView = new NodeView(node);
            nodeView.setOnMouseClicked(event -> onNodeClicked.accept(node));
            nodeViewMap.put(node, nodeView);
        }

        // 2. EdgeViews + hitAreas
        for (Edge edge : graph.getEdges()) {
            NodeView startView = nodeViewMap.get(edge.getStart());
            NodeView endView   = nodeViewMap.get(edge.getEnd());

            EdgeView edgeView = new EdgeView(edge);
            edgeView.startXProperty().bind(startView.centerXProperty());
            edgeView.startYProperty().bind(startView.centerYProperty());
            edgeView.endXProperty().bind(endView.centerXProperty());
            edgeView.endYProperty().bind(endView.centerYProperty());
            edgeViews.add(edgeView);
            getChildren().add(edgeView);

            Line hitArea = new Line();
            hitArea.setStrokeWidth(14);
            hitArea.setStroke(Color.TRANSPARENT);
            hitArea.setCursor(Cursor.HAND);
            hitArea.startXProperty().bind(startView.centerXProperty());
            hitArea.startYProperty().bind(startView.centerYProperty());
            hitArea.endXProperty().bind(endView.centerXProperty());
            hitArea.endYProperty().bind(endView.centerYProperty());
            hitArea.setOnMouseClicked(event -> onEdgeClicked.accept(edge));
            getChildren().add(hitArea);
        }

        // 3. NodeViews au-dessus des arêtes
        getChildren().addAll(nodeViewMap.values());

        // 4. Labels ID sur chaque nœud
        for (Node node : graph.getNodes()) {
            NodeView nodeView = nodeViewMap.get(node);

            Text idLabel = new Text(String.valueOf(node.getId()));
            idLabel.setFont(Font.font("System", FontWeight.BOLD, 11));
            idLabel.setFill(Color.WHITE);
            idLabel.setMouseTransparent(true);
            idLabel.xProperty().bind(nodeView.centerXProperty().subtract(5));
            idLabel.yProperty().bind(nodeView.centerYProperty().add(4));
            getChildren().add(idLabel);
        }

        // 5. AgentViews initiaux
        for (Node node : graph.getNodes()) {
            NodeView nodeView = nodeViewMap.get(node);
            List<Agent> agents = node.getAgents();

            for (int i = 0; i < agents.size(); i++) {
                AgentView agentView = new AgentView(agents.get(i));

                double offsetX = (i % 3) * 13 - 13;
                double offsetY = 20 + (i / 3) * 13;

                agentView.centerXProperty().bind(nodeView.centerXProperty().add(offsetX));
                agentView.centerYProperty().bind(nodeView.centerYProperty().add(offsetY));

                agentViews.add(agentView);
                getChildren().add(agentView);
            }
        }
    }

    /** Met à jour les couleurs de tous les nœuds et arêtes. */
    public void refresh() {
        for (NodeView nodeView : nodeViewMap.values()) {
            nodeView.refresh();
        }
        for (EdgeView edgeView : edgeViews) {
            edgeView.refresh();
        }
    }

    /**
     * Redessine tous les agents sur les nœuds depuis zéro.
     * Appelé après chaque tick.
     */
    public void redrawAgents() {
        getChildren().removeAll(agentViews);
        agentViews.clear();

        for (Node node : graph.getNodes()) {
            NodeView nodeView = nodeViewMap.get(node);
            List<Agent> agents = node.getAgents();

            for (int i = 0; i < agents.size(); i++) {
                AgentView agentView = new AgentView(agents.get(i));

                double offsetX = (i % 3) * 13 - 13;
                double offsetY = 20 + (i / 3) * 13;

                agentView.centerXProperty().bind(nodeView.centerXProperty().add(offsetX));
                agentView.centerYProperty().bind(nodeView.centerYProperty().add(offsetY));

                agentViews.add(agentView);
                getChildren().add(agentView);
            }
        }
    }

    /**
     * Dessine les agents en transit sur les arêtes.
     * Position interpolée entre le nœud d'origine et le nœud de destination
     * selon la progression (0.0 → 1.0).
     */
    public void drawTransitAgents(List<AgentTransit> transits) {
        getChildren().removeAll(transitAgentViews);
        transitAgentViews.clear();

        // Grouper les transits par arete pour calculer l'offset perpendiculaire
        Map<String, List<AgentTransit>> byEdge = new LinkedHashMap<>();
        for (AgentTransit t : transits) {
            int a = Math.min(t.from.getId(), t.to.getId());
            int b = Math.max(t.from.getId(), t.to.getId());
            byEdge.computeIfAbsent(a + "_" + b, k -> new ArrayList<>()).add(t);
        }

        for (List<AgentTransit> group : byEdge.values()) {
            int n = group.size();
            for (int i = 0; i < n; i++) {
                AgentTransit transit = group.get(i);
                NodeView fromView = nodeViewMap.get(transit.from);
                NodeView toView   = nodeViewMap.get(transit.to);
                if (fromView == null || toView == null) continue;

                double fx = fromView.getCenterX(), fy = fromView.getCenterY();
                double tx = toView.getCenterX(),   ty = toView.getCenterY();

                // Position interpolee sur l'arete
                double x = fx + (tx - fx) * transit.progress;
                double y = fy + (ty - fy) * transit.progress;

                // Offset perpendiculaire pour eviter la superposition
                if (n > 1) {
                    double edgeLen = Math.sqrt((tx - fx) * (tx - fx) + (ty - fy) * (ty - fy));
                    if (edgeLen > 0) {
                        double perpX = -(ty - fy) / edgeLen;
                        double perpY =  (tx - fx) / edgeLen;
                        double offset = (i - (n - 1) / 2.0) * 11;
                        x += perpX * offset;
                        y += perpY * offset;
                    }
                }

                AgentView agentView = new AgentView(transit.agent);
                agentView.setCenterX(x);
                agentView.setCenterY(y);
                transitAgentViews.add(agentView);
                getChildren().add(agentView);
            }
        }
    }
}
