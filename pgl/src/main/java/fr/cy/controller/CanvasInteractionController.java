package fr.cy.controller;

import fr.cy.model.agent.Agent;
import fr.cy.model.graph.element.Edge;
import fr.cy.model.graph.element.GraphElement;
import fr.cy.model.graph.element.Node;
import fr.cy.view.GraphCanvas;
import javafx.scene.input.MouseEvent;

import java.util.function.Consumer;

public class CanvasInteractionController {

    private final GraphCanvas canvas;
    private final SimulationController simController;
    private Consumer<Object> onEntitySelected;

    private double lastMouseX;
    private double lastMouseY;
    private Node draggedNode = null;

    public CanvasInteractionController(GraphCanvas canvas, SimulationController simController) {
        this.canvas = canvas;
        this.simController = simController;
        setupInteractions();
    }

    public void setOnEntitySelected(Consumer<Object> callback) {
        this.onEntitySelected = callback;
    }

    private void setupInteractions() {
        canvas.setOnMouseClicked(this::handleCanvasClick);

        canvas.setOnMousePressed(event -> {
            lastMouseX = event.getX();
            lastMouseY = event.getY();
            double mx = (event.getX() - canvas.getPanX()) / canvas.getZoom();
            double my = (event.getY() - canvas.getPanY()) / canvas.getZoom();
            draggedNode = null;
            double minDistance = Double.MAX_VALUE;

            if (simController.getSimulation() != null && simController.getSimulation().getGraph() != null) {
                for (Node node : simController.getSimulation().getGraph().getNodes()) {
                    double distance = Math.hypot(node.getX() - mx, node.getY() - my);
                    if (distance < 30 && distance < minDistance) {
                        minDistance = distance;
                        draggedNode = node;
                    }
                }
            }
        });

        canvas.setOnMouseDragged(event -> {
            if (draggedNode != null) {
                double mx = (event.getX() - canvas.getPanX()) / canvas.getZoom();
                double my = (event.getY() - canvas.getPanY()) / canvas.getZoom();
                draggedNode.setX(mx);
                draggedNode.setY(my);
                /*
                 * for (Edge edge : draggedNode.getEdges()) {
                 * Node start = edge.getStart();
                 * Node end = edge.getEnd();
                 * double newLength = Math.hypot(start.getX() - end.getX(), start.getY() -
                 * end.getY());
                 * edge.setLength(newLength);
                 * }
                 */
                notifySelection(draggedNode);
            } else {
                double dx = event.getX() - lastMouseX;
                double dy = event.getY() - lastMouseY;
                canvas.setPanX(canvas.getPanX() + dx);
                canvas.setPanY(canvas.getPanY() + dy);
                lastMouseX = event.getX();
                lastMouseY = event.getY();
            }
        });

        canvas.setOnMouseReleased(event -> draggedNode = null);

        canvas.setOnScroll(event -> {
            double zoomFactor = 1.05;
            if (event.getDeltaY() < 0)
                zoomFactor = 1 / zoomFactor;
            double oldZoom = canvas.getZoom();
            double newZoom = Math.max(0.2, Math.min(oldZoom * zoomFactor, 5.0));
            double f = (newZoom / oldZoom) - 1;
            double dx = (event.getX() - canvas.getPanX()) * f;
            double dy = (event.getY() - canvas.getPanY()) * f;
            canvas.setPanX(canvas.getPanX() - dx);
            canvas.setPanY(canvas.getPanY() - dy);
            canvas.setZoom(newZoom);
        });
    }

    private void handleCanvasClick(MouseEvent event) {
        if (event.isStillSincePress()) {
            double mx = (event.getX() - canvas.getPanX()) / canvas.getZoom();
            double my = (event.getY() - canvas.getPanY()) / canvas.getZoom();

            Agent clickedAgent = findClosestAgent(mx, my);
            if (clickedAgent != null) {
                canvas.setSelectedAgent(clickedAgent);
                notifySelection(clickedAgent);
                return;
            }

            GraphElement clickedElement = findClosestElement(mx, my);
            canvas.setSelectedAgent(null);
            notifySelection(clickedElement);
        }
    }

    private void notifySelection(Object entity) {
        if (onEntitySelected != null) {
            onEntitySelected.accept(entity);
        }
    }

    private Agent findClosestAgent(double mx, double my) {
        if (simController.getSimulation() == null || simController.getSimulation().getAgentManager() == null)
            return null;

        Agent closest = null;
        double minDistance = Double.MAX_VALUE;

        for (Agent agent : simController.getSimulation().getAgentManager().getAgents()) {
            double ax = 0, ay = 0;
            boolean isVisible = false;

            if (agent.isOnNode() && agent.getCurrentNode() != null) {
                Node node = agent.getCurrentNode();
                double maxOffset = 22.0 * 0.4;
                double angle = (agent.getId() * 137.508) % 360;
                double dist = (agent.getId() * 11.3) % maxOffset;
                ax = node.getX() + Math.cos(Math.toRadians(angle)) * dist;
                ay = node.getY() + Math.sin(Math.toRadians(angle)) * dist;
                isVisible = true;
            } else if (!agent.isOnNode() && agent.getCurrentEdge() != null) {
                Edge edge = agent.getCurrentEdge();
                Node previous = agent.getPreviousOrCurrentNode();
                Node target = edge.getOppositeNode(previous);
                double ratio = Math.max(0, agent.getTravelProgressPercentageOnEdge());
                double baseX = previous.getX() + (target.getX() - previous.getX()) * ratio;
                double baseY = previous.getY() + (target.getY() - previous.getY()) * ratio;
                double angle = (agent.getId() * 137.508) % 360;
                double maxEdgeOffset = Math.max(2, edge.getWidth() / 2 - 2);
                double dist = (agent.getId() * 7.1) % maxEdgeOffset;
                ax = baseX + Math.cos(Math.toRadians(angle)) * dist;
                ay = baseY + Math.sin(Math.toRadians(angle)) * dist;
                isVisible = true;
            }

            if (isVisible) {
                double distance = Math.hypot(ax - mx, ay - my);
                if (distance <= 8.0 && distance < minDistance) {
                    minDistance = distance;
                    closest = agent;
                }
            }
        }
        return closest;
    }

    private GraphElement findClosestElement(double mx, double my) {
        if (simController.getSimulation() == null || simController.getSimulation().getGraph() == null)
            return null;

        GraphElement closest = null;
        double minDistance = Double.MAX_VALUE;

        for (Node node : simController.getSimulation().getGraph().getNodes()) {
            double distance = Math.hypot(node.getX() - mx, node.getY() - my);
            if (distance < 30 && distance < minDistance) {
                minDistance = distance;
                closest = node;
            }
        }

        if (closest == null) {
            for (Edge edge : simController.getSimulation().getGraph().getEdges()) {
                double x1 = edge.getStart().getX(), y1 = edge.getStart().getY();
                double x2 = edge.getEnd().getX(), y2 = edge.getEnd().getY();

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
}