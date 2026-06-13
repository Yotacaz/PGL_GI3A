package fr.cy.controller;

import fr.cy.model.agent.Agent;
import fr.cy.model.graph.element.Edge;
import fr.cy.model.graph.element.GraphElement;
import fr.cy.model.graph.element.Node;
import fr.cy.view.DialogHelper;
import fr.cy.view.GraphCanvas;
import javafx.scene.Cursor;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Rectangle;

import java.util.ArrayList;
import java.util.List;
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

    // Rectangle selection state (DELETE mode)
    private double selectionStartScreenX, selectionStartScreenY;
    private boolean isDraggingSelection = false;
    private Rectangle selectionOverlay;
    private Consumer<List<Object>> onDeleteInRegionRequested;

    /**
     * Defines the supported interaction modes for the canvas.
     */
    public enum InteractionMode {
        /** Mode for selecting and dragging existing graph elements. */
        SELECT_AND_DRAG,
        /** Mode for adding a new node by clicking on the canvas. */
        ADD_NODE,
        /** Mode for starting to draw an edge from a source node. */
        ADD_EDGE_START,
        /** Mode for placing a new agent on a node. */
        ADD_AGENT,
        /** Mode for deleting graph elements by clicking or drawing a selection region. */
        DELETE
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
        this.edgeStartNode = null;
        this.isDraggingSelection = false;
        if (selectionOverlay != null)
            selectionOverlay.setVisible(false);

        switch (mode) {
            case ADD_NODE, ADD_EDGE_START, ADD_AGENT -> canvas.setCursor(Cursor.CROSSHAIR);
            case DELETE -> canvas.setCursor(Cursor.CROSSHAIR);
            case SELECT_AND_DRAG -> canvas.setCursor(Cursor.DEFAULT);
        }

        canvas.setSelectedEntity(null);
        notifySelection(null);
    }

    /**
     * Provides the overlay Rectangle used to visualise the selection zone in DELETE mode.
     *
     * @param overlay the Rectangle to use as the selection overlay
     */
    public void setSelectionOverlay(Rectangle overlay) {
        this.selectionOverlay = overlay;
    }

    /**
     * Sets the callback fired with every object inside the drawn selection rectangle.
     *
     * @param callback consumer that receives the list of objects within the selection region
     */
    public void setOnDeleteInRegionRequested(Consumer<List<Object>> callback) {
        this.onDeleteInRegionRequested = callback;
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

            if (currentMode == InteractionMode.SELECT_AND_DRAG) {
                canvas.setCursor(draggedNode != null ? Cursor.CLOSED_HAND : Cursor.MOVE);
            } else if (currentMode == InteractionMode.DELETE) {
                // Record the drag-start position for rectangle selection
                selectionStartScreenX = event.getX();
                selectionStartScreenY = event.getY();
                isDraggingSelection = false;
                canvas.setCursor(Cursor.CROSSHAIR);
            }
        });

        canvas.setOnMouseReleased(event -> {
            draggedNode = null;
            if (currentMode == InteractionMode.DELETE && isDraggingSelection) {
                // Hide overlay and process zone deletion
                if (selectionOverlay != null)
                    selectionOverlay.setVisible(false);
                isDraggingSelection = false;

                double zoom = canvas.getZoom();
                double panX = canvas.getPanX();
                double panY = canvas.getPanY();
                double worldMinX = (Math.min(selectionStartScreenX, event.getX()) - panX) / zoom;
                double worldMinY = (Math.min(selectionStartScreenY, event.getY()) - panY) / zoom;
                double worldMaxX = (Math.max(selectionStartScreenX, event.getX()) - panX) / zoom;
                double worldMaxY = (Math.max(selectionStartScreenY, event.getY()) - panY) / zoom;

                List<Object> toDelete = collectElementsInRect(worldMinX, worldMinY, worldMaxX, worldMaxY);
                if (!toDelete.isEmpty() && onDeleteInRegionRequested != null)
                    onDeleteInRegionRequested.accept(toDelete);

            } else if (currentMode == InteractionMode.SELECT_AND_DRAG) {
                canvas.setCursor(Cursor.DEFAULT);
            }
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
            if (currentMode == InteractionMode.SELECT_AND_DRAG && draggedNode != null) {
                // Drag a node to reposition it
                draggedNode.setX((event.getX() - canvas.getPanX()) / canvas.getZoom());
                draggedNode.setY((event.getY() - canvas.getPanY()) / canvas.getZoom());
                notifySelection(draggedNode);
            } else if (currentMode == InteractionMode.DELETE) {
                // Draw the selection rectangle
                isDraggingSelection = true;
                if (selectionOverlay != null) {
                    double x = Math.min(selectionStartScreenX, event.getX());
                    double y = Math.min(selectionStartScreenY, event.getY());
                    selectionOverlay.setX(x);
                    selectionOverlay.setY(y);
                    selectionOverlay.setWidth(Math.abs(event.getX() - selectionStartScreenX));
                    selectionOverlay.setHeight(Math.abs(event.getY() - selectionStartScreenY));
                    selectionOverlay.setVisible(true);
                }
            } else if (currentMode == InteractionMode.SELECT_AND_DRAG) {
                // Pan the canvas
                canvas.setPanX(canvas.getPanX() + (event.getX() - lastMouseX));
                canvas.setPanY(canvas.getPanY() + (event.getY() - lastMouseY));
                lastMouseX = event.getX();
                lastMouseY = event.getY();
            }
        });

        canvas.setOnScroll(event -> {
            double zoomFactor = event.getDeltaY() < 0 ? 0.9 : 1.05;
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
        // Ignore drag-end events in modes that handle drags themselves
        if (!event.isStillSincePress() &&
                (currentMode == InteractionMode.SELECT_AND_DRAG || currentMode == InteractionMode.DELETE))
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
     * Collects every simulation object whose key position falls inside the given
     * world-coordinate rectangle. Nodes are tested by centre; edges by their
     * midpoint (only when neither endpoint is already selected, to avoid
     * double-deletion when a node removal already cascades to its edges);
     * agents by their interpolated position on the graph.
     */
    private List<Object> collectElementsInRect(double minX, double minY, double maxX, double maxY) {
        List<Object> result = new ArrayList<>();
        var sim = simController.getSimulation();
        if (sim == null) return result;

        var graph = sim.getGraph();
        if (graph != null) {
            for (Node node : graph.getNodes()) {
                if (node.getX() >= minX && node.getX() <= maxX
                        && node.getY() >= minY && node.getY() <= maxY)
                    result.add(node);
            }
            for (Edge edge : graph.getEdges()) {
                // Skip edges whose removal is already implied by a selected endpoint node
                if (result.contains(edge.getStart()) || result.contains(edge.getEnd()))
                    continue;
                double midX = (edge.getStart().getX() + edge.getEnd().getX()) / 2.0;
                double midY = (edge.getStart().getY() + edge.getEnd().getY()) / 2.0;
                if (midX >= minX && midX <= maxX && midY >= minY && midY <= maxY)
                    result.add(edge);
            }
        }

        if (sim.getAgentManager() != null) {
            for (Agent agent : sim.getAgentManager().getAgentsToEvacuate()) {
                double ax, ay;
                if (agent.isOnNode() && agent.getCurrentNode() != null) {
                    ax = agent.getCurrentNode().getX();
                    ay = agent.getCurrentNode().getY();
                } else if (agent.getPreviousOrCurrentEdge() != null) {
                    Edge e = agent.getPreviousOrCurrentEdge();
                    double p = agent.getCurrentEdgeProgress();
                    ax = e.getStart().getX() + p * (e.getEnd().getX() - e.getStart().getX());
                    ay = e.getStart().getY() + p * (e.getEnd().getY() - e.getStart().getY());
                } else continue;
                if (ax >= minX && ax <= maxX && ay >= minY && ay <= maxY)
                    result.add(agent);
            }
        }
        return result;
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