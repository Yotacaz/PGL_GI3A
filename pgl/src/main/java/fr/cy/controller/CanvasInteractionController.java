package fr.cy.controller;

import fr.cy.model.agent.Agent;
import fr.cy.model.graph.element.Edge;
import fr.cy.model.graph.element.GraphElement;
import fr.cy.model.graph.element.Node;
import fr.cy.view.DialogHelper;
import fr.cy.view.GraphCanvas;
import javafx.scene.Cursor;
import javafx.scene.input.MouseEvent;

import java.util.function.Consumer;

/**
 * The {@code CanvasInteractionController} manages user interactions with the
 * {@link GraphCanvas}.
 * <p>
 * It handles mouse inputs for selection, dragging entities, panning/zooming,
 * and various creation/manipulation modes (nodes, edges, agents, deletion).
 * </p>
 */
public class CanvasInteractionController {

    /** Tolerance in pixels to detect clicks near elements. */
    private static final double CLICK_TOLERANCE = 15.0;

    private final GraphCanvas canvas;
    private final SimulationController simController;
    private Consumer<Object> onEntitySelected;
    private Consumer<Node> onAddAgentRequested;
    private Consumer<GraphElement> onDeleteElementRequested;

    private double lastMouseX;
    private double lastMouseY;
    private Node draggedNode = null;
    private Node edgeStartNode = null;
    private InteractionMode currentMode = InteractionMode.SELECT_AND_DRAG;

    /**
     * Defines the supported interaction modes for the canvas.
     */
    public enum InteractionMode {
        SELECT_AND_DRAG, ADD_NODE, ADD_EDGE_START, ADD_AGENT, DELETE
    }

    /**
     * Constructs the controller and initializes interaction listeners.
     *
     * @param canvas        The canvas to attach listeners to.
     * @param simController The simulation controller instance.
     */
    public CanvasInteractionController(GraphCanvas canvas, SimulationController simController) {
        this.canvas = canvas;
        this.simController = simController;
        setupInteractions();
    }

    /**
     * Switches the interaction mode and updates the cursor accordingly.
     *
     * @param mode The new interaction mode.
     */
    public void setMode(InteractionMode mode) {
        this.currentMode = mode;
        this.edgeStartNode = null; // Clear creation state

        switch (mode) {
            case ADD_NODE, ADD_EDGE_START, ADD_AGENT -> canvas.setCursor(Cursor.CROSSHAIR);
            case DELETE -> canvas.setCursor(Cursor.DEFAULT); // Cursor handled dynamically on move
            case SELECT_AND_DRAG -> canvas.setCursor(Cursor.DEFAULT);
        }

        canvas.setSelectedEntity(null);
        notifySelection(null);
    }

    /**
     * Sets the callback for entity selection.
     *
     * @param callback Consumer to receive the selected object.
     */
    public void setOnEntitySelected(Consumer<Object> callback) {
        this.onEntitySelected = callback;
    }

    /**
     * Sets the callback for agent addition requests.
     *
     * @param callback Consumer to receive the target {@link Node}.
     */
    public void setOnAddAgentRequested(Consumer<Node> callback) {
        this.onAddAgentRequested = callback;
    }

    /**
     * Sets the callback executed when an element is clicked in deletion mode.
     *
     * @param callback Consumer receiving the structural element to remove.
     */
    public void setOnDeleteElementRequested(Consumer<GraphElement> callback) {
        this.onDeleteElementRequested = callback;
    }

    /**
     * Wires up mouse and scroll events for the graph canvas.
     */
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
                    double dist = Math.hypot(node.getX() - mx, node.getY() - my);
                    if (dist < 30 && dist < minDistance) {
                        minDistance = dist;
                        draggedNode = node;
                    }
                }
            }

            // Panning is allowed in SELECT_AND_DRAG or DELETE modes
            if (currentMode == InteractionMode.SELECT_AND_DRAG) {
                canvas.setCursor(draggedNode != null ? Cursor.CLOSED_HAND : Cursor.MOVE);
            } else if (currentMode == InteractionMode.DELETE) {
                canvas.setCursor(Cursor.MOVE);
            }
        });

        canvas.setOnMouseReleased(event -> {
            draggedNode = null;
            if (currentMode == InteractionMode.SELECT_AND_DRAG || currentMode == InteractionMode.DELETE)
                canvas.setCursor(Cursor.DEFAULT);
        });

        canvas.setOnMouseMoved(event -> {
            double mx = (event.getX() - canvas.getPanX()) / canvas.getZoom();
            double my = (event.getY() - canvas.getPanY()) / canvas.getZoom();

            if (currentMode == InteractionMode.SELECT_AND_DRAG) {
                canvas.setCursor((findClosestAgent(mx, my) != null || findClosestElement(mx, my) != null)
                        ? Cursor.HAND
                        : Cursor.DEFAULT);
            } else if (currentMode == InteractionMode.DELETE) {
                // Highlight structural elements removable in this mode
                canvas.setCursor(Cursor.CROSSHAIR);
            } else {
                canvas.setCursor(Cursor.CROSSHAIR);
            }
        });

        canvas.setOnMouseDragged(event -> {
            // Drag node (only in default mode)
            if (currentMode == InteractionMode.SELECT_AND_DRAG && draggedNode != null) {
                draggedNode.setX((event.getX() - canvas.getPanX()) / canvas.getZoom());
                draggedNode.setY((event.getY() - canvas.getPanY()) / canvas.getZoom());
                notifySelection(draggedNode);
            } else {
                // Pan canvas (allowed in SELECT_AND_DRAG or DELETE)
                if (currentMode == InteractionMode.SELECT_AND_DRAG || currentMode == InteractionMode.DELETE) {
                    canvas.setPanX(canvas.getPanX() + (event.getX() - lastMouseX));
                    canvas.setPanY(canvas.getPanY() + (event.getY() - lastMouseY));
                    lastMouseX = event.getX();
                    lastMouseY = event.getY();
                }
            }
        });

        canvas.setOnScroll(event -> {
            double zoomFactor = event.getDeltaY() < 0 ? 0.95 : 1.05;
            double oldZoom = canvas.getZoom();
            double newZoom = Math.max(0.2, Math.min(oldZoom * zoomFactor, 5.0));
            double f = (newZoom / oldZoom) - 1;
            canvas.setPanX(canvas.getPanX() - (event.getX() - canvas.getPanX()) * f);
            canvas.setPanY(canvas.getPanY() - (event.getY() - canvas.getPanY()) * f);
            canvas.setZoom(newZoom);
        });
    }

    /**
     * Routes clicks to appropriate interaction logic based on the
     * {@code currentMode}.
     */
    private void handleCanvasClick(MouseEvent event) {
        // Ignore clicks if a drag occurred (except selection mode where drag selection
        // is complex)
        if (!event.isStillSincePress() && currentMode == InteractionMode.SELECT_AND_DRAG)
            return;

        double mx = (event.getX() - canvas.getPanX()) / canvas.getZoom();
        double my = (event.getY() - canvas.getPanY()) / canvas.getZoom();
        var graph = simController.getSimulation().getGraph();

        if (currentMode == InteractionMode.ADD_NODE) {
            DialogHelper.showNodeCreationDialog(canvas).ifPresent(p -> {
                Node n = graph.createNode(mx, my, p.capacity);
                n.setExit(p.isExit);
                canvas.setSelectedEntity(n);
                notifySelection(n);
            });
        } else if (currentMode == InteractionMode.ADD_EDGE_START) {
            GraphElement clicked = findClosestElement(mx, my);
            if (clicked instanceof Node clickedNode) {
                if (edgeStartNode == null) {
                    edgeStartNode = clickedNode;
                    canvas.setSelectedEntity(edgeStartNode);
                } else if (edgeStartNode != clickedNode) {
                    double len = Math.hypot(edgeStartNode.getX() - clickedNode.getX(),
                            edgeStartNode.getY() - clickedNode.getY()) / 10.0;
                    Node start = edgeStartNode, end = clickedNode;
                    edgeStartNode = null;
                    canvas.setSelectedEntity(null);
                    DialogHelper.showEdgeCreationDialog(start, end, canvas).ifPresent(p -> {
                        Edge e = graph.createEdge(start, end, len, p.width, p.directed);
                        canvas.setSelectedEntity(e);
                        notifySelection(e);
                    });
                }
            }
        } else if (currentMode == InteractionMode.ADD_AGENT) {
            GraphElement clicked = findClosestElement(mx, my);
            if (clicked instanceof Node clickedNode) {
                canvas.setSelectedEntity(clickedNode);
                notifySelection(clickedNode);
                if (onAddAgentRequested != null)
                    onAddAgentRequested.accept(clickedNode);
            }
        } else if (currentMode == InteractionMode.DELETE) {
            Agent clickedAgent = findClosestAgent(mx, my);

            if (clickedAgent != null) {
                if (onEntitySelected != null) {
                    onEntitySelected.accept(clickedAgent);
                }

            }
            GraphElement clicked = findClosestElement(mx, my);
            if (clicked != null && onDeleteElementRequested != null) {
                onDeleteElementRequested.accept(clicked);
                // Selection logic is handled by MainController via wiring
            }
        } else {
            // Standard selection and interaction logic
            Agent clickedAgent = findClosestAgent(mx, my);
            GraphElement clickedElement = (clickedAgent == null) ? findClosestElement(mx, my) : null;
            Object selected = (clickedAgent != null) ? clickedAgent : clickedElement;

            canvas.setSelectedEntity(selected);
            notifySelection(selected);
        }
    }

    private void notifySelection(Object entity) {
        if (onEntitySelected != null)
            onEntitySelected.accept(entity);
    }

    /**
     * Logic to find the closest agent within a small click radius.
     */
    private Agent findClosestAgent(double mx, double my) {
        if (simController.getSimulation() == null || simController.getSimulation().getAgentManager() == null)
            return null;

        Agent closest = null;
        double minDistance = 8.0;

        for (Agent agent : simController.getSimulation().getAgentManager().getAgentsToEvacuate()) {
            double ax = 0;
            double ay = 0;

            if (agent.isOnNode() && agent.getCurrentNode() != null) {
                ax = agent.getCurrentNode().getX();
                ay = agent.getCurrentNode().getY();
            } else if (agent.getPreviousOrCurrentEdge() != null) {
                Edge edge = agent.getPreviousOrCurrentEdge();
                double progress = agent.getCurrentEdgeProgress();
                double startX = edge.getStart().getX();
                double startY = edge.getStart().getY();
                double endX = edge.getEnd().getX();
                double endY = edge.getEnd().getY();

                ax = startX + progress * (endX - startX);
                ay = startY + progress * (endY - startY);
            } else {
                continue;
            }

            double dist = Math.hypot(ax - mx, ay - my);
            if (dist <= minDistance) {
                minDistance = dist;
                closest = agent;
            }
        }
        return closest;
    }

    /**
     * Calculates the shortest distance from a point to a line segment.
     */
    private double distancePointToSegment(double px, double py, double x1, double y1, double x2, double y2) {
        double l2 = Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2);
        if (l2 == 0)
            return Math.hypot(px - x1, py - y1);
        double t = Math.max(0, Math.min(1, ((px - x1) * (x2 - x1) + (py - y1) * (y2 - y1)) / l2));
        return Math.hypot(px - (x1 + t * (x2 - x1)), py - (y1 + t * (y2 - y1)));
    }

    /**
     * Finds the closest graph element (Node or Edge) within click tolerance.
     */
    private GraphElement findClosestElement(double mx, double my) {
        var graph = simController.getSimulation().getGraph();
        for (Node n : graph.getNodes())
            if (Math.hypot(n.getX() - mx, n.getY() - my) <= 10.0 + CLICK_TOLERANCE)
                return n;
        for (Edge e : graph.getEdges())
            if (distancePointToSegment(mx, my, e.getStart().getX(), e.getStart().getY(), e.getEnd().getX(),
                    e.getEnd().getY()) <= (e.getWidth() / 2.0) + CLICK_TOLERANCE)
                return e;
        return null;
    }
}