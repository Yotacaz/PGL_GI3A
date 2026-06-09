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

public class CanvasInteractionController {

    // Error margin in units to facilitate clicking (e.g., 15 units around the element)
    
    private static final double CLICK_TOLERANCE = 15.0;

    private final GraphCanvas canvas;
    private final SimulationController simController;
    private Consumer<Object> onEntitySelected;

    private double lastMouseX;
    private double lastMouseY;
    private Node draggedNode = null;

    public enum InteractionMode {
        SELECT_AND_DRAG,
        ADD_NODE,
        ADD_EDGE_START,
        ADD_AGENT
    }

    private InteractionMode currentMode = InteractionMode.SELECT_AND_DRAG;
    private Node edgeStartNode = null;

    public void setMode(InteractionMode mode) {

        System.out.println("--- LE MODE ACTUEL EST MAINTENANT : " + mode + " ---");
        this.currentMode = mode;
        this.edgeStartNode = null; // Annule l'action en cours si on change d'outil

        switch (mode) {
            case ADD_NODE:
                // A precise crosshair cursor for placing a node
                canvas.setCursor(Cursor.CROSSHAIR);
                break;

            case ADD_EDGE_START:
                // A different crosshair or maybe a custom cursor indicating "select start node"
                canvas.setCursor(Cursor.CROSSHAIR);
                break;
            case ADD_AGENT:
                canvas.setCursor(Cursor.CROSSHAIR);
                break;

            case SELECT_AND_DRAG:
            default:
                // Classic arrow cursor for selection and dragging
                canvas.setCursor(Cursor.DEFAULT);
                break;
        }

        canvas.setSelectedEntity(null);
        notifySelection(null);
    }

    public CanvasInteractionController(GraphCanvas canvas, SimulationController simController) {
        this.canvas = canvas;
        this.simController = simController;
        setupInteractions();
    }

    public void setOnEntitySelected(Consumer<Object> callback) {
        this.onEntitySelected = callback;
    }

    private Consumer<Node> onAddAgentRequested;

    public void setOnAddAgentRequested(Consumer<Node> callback) {
        this.onAddAgentRequested = callback;
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

            if (currentMode == InteractionMode.SELECT_AND_DRAG) {
                if (draggedNode != null) {
                    canvas.setCursor(Cursor.CLOSED_HAND); // Main that catch the node
                } else {
                    canvas.setCursor(Cursor.MOVE); // Directional arrows for panning the view
                }
            }
        });

        canvas.setOnMouseReleased(event -> {
            draggedNode = null;
            if (currentMode == InteractionMode.SELECT_AND_DRAG) {
                canvas.setCursor(Cursor.DEFAULT);
            }
        });

        canvas.setOnMouseMoved(event -> {
            if (currentMode == InteractionMode.SELECT_AND_DRAG) {
                // Commpute the mouse position in graph coordinates
                double mx = (event.getX() - canvas.getPanX()) / canvas.getZoom();
                double my = (event.getY() - canvas.getPanY()) / canvas.getZoom();

                // if the mouse is hovering over an agent, node or edge, change the cursor to indicate interactivity
                if (findClosestAgent(mx, my) != null || findClosestElement(mx, my) != null) {
                    canvas.setCursor(Cursor.HAND); // Main ouverte (Prêt à cliquer)
                } else {
                    canvas.setCursor(Cursor.DEFAULT); // Flèche normale dans le vide
                }
            } else {
                // If we are in "Creation" mode (Blue or gray buttons pressed), show the crosshair cursor to indicate precision placement
                canvas.setCursor(Cursor.CROSSHAIR); // Precision crosshair for adding nodes/edges/agents
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
        if (event.isStillSincePress() || currentMode != InteractionMode.SELECT_AND_DRAG) {
            double mx = (event.getX() - canvas.getPanX()) / canvas.getZoom();
            double my = (event.getY() - canvas.getPanY()) / canvas.getZoom();

            // Getting the graph from the simulation to find nodes and edges
            fr.cy.model.graph.Graph graph = simController.getSimulation().getGraph();

            // =========================================================
            // MODE 1 : ADD NODE
            // =========================================================
            if (currentMode == InteractionMode.ADD_NODE) {
                try {
                    DialogHelper.showNodeCreationDialog(canvas)
                            .ifPresent(params -> {
                                Node newNode = graph.createNode(mx, my, params.capacity);
                                newNode.setExit(params.isExit);
                                canvas.setSelectedEntity(newNode);
                                notifySelection(newNode);
                            });
                } catch (Exception e) {
                    System.err.println("❌ ERREUR CRITIQUE LORS DE LA CRÉATION DE LA FENÊTRE :");
                    e.printStackTrace();
                }

                return;
            }

            // =========================================================
            // MODE 2 : ADD EDGE
            // =========================================================
            if (currentMode == InteractionMode.ADD_EDGE_START) {
                GraphElement clickedElement = findClosestElement(mx, my);

                if (clickedElement instanceof Node clickedNode) {
                    if (edgeStartNode == null) {
                        // First click
                        edgeStartNode = clickedNode;
                        canvas.setSelectedEntity(edgeStartNode);
                    } else if (edgeStartNode != clickedNode) {

                        // 1. Compute distance
                        double pixelDistance = Math.hypot(edgeStartNode.getX() - clickedNode.getX(),
                                edgeStartNode.getY() - clickedNode.getY());
                        double logicalLength = pixelDistance / 10.0; // Ton échelle de distance

                        
                        // 2. Memorize the nodes in temporary variables before opening the dialog (to avoid bugs if we cancel and to have access to them in the lambda)
                        Node startNode = edgeStartNode;
                        Node endNode = clickedNode;

                        // 3. RESET THE TOOL IMMEDIATELY (to avoid bugs if we cancel)
                        edgeStartNode = null;
                        canvas.setSelectedEntity(null);

                        DialogHelper.showEdgeCreationDialog(startNode, endNode, canvas)
                                .ifPresent(params -> {
                                    // 5. Create the edge with the actual parameters chosen by the user
                                    Edge newEdge = graph.createEdge(startNode, endNode, logicalLength, params.width,
                                            params.directed);

                                    // Select the new edge to show its properties in the right panel
                                    canvas.setSelectedEntity(newEdge);
                                    notifySelection(newEdge);
                                });
                    }
                }
                return;
            }

            // =========================================================
            // MODE 3 : ADD AGENTS TO A NODE
            // =========================================================
            if (currentMode == InteractionMode.ADD_AGENT) {
                GraphElement clickedElement = findClosestElement(mx, my);
                if (clickedElement instanceof Node clickedNode) {

                    // --- Correction is here ---
                    // 1. Select visually the node on which we want to add agents and show its properties in the right panel (to avoid confusion about which node we are adding agents to, especially if we cancel the action)
                    canvas.setSelectedEntity(clickedNode);
                    notifySelection(clickedNode);

                    // 2. Open the dialog to choose how many agents we want to add
                    if (onAddAgentRequested != null) {
                        onAddAgentRequested.accept(clickedNode);
                    }
                }
                return;
            }

            // =========================================================
            // NORMAL MODE : SELECT AND MODIFY (Double-Click)
            // =========================================================
            if (currentMode == InteractionMode.SELECT_AND_DRAG || currentMode == null) {
                // 1. Search for the cliked element
                Object selectedEntity = null;
                Agent clickedAgent = findClosestAgent(mx, my);
                GraphElement clickedElement = (clickedAgent == null) ? findClosestElement(mx, my) : null;

                selectedEntity = (clickedAgent != null) ? clickedAgent : clickedElement;

                // 2. Double-click case
                if (event.getClickCount() == 2 && selectedEntity != null) {
                    handleModification(selectedEntity);
                }
                // 3. Simple Click Handling (Selection)
                else {
                    canvas.setSelectedEntity(selectedEntity);
                    notifySelection(selectedEntity);
                }
            }
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

        for (Agent agent : simController.getSimulation().getAgentManager().getAgentsToEvacuate()) {
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
            } else if (!agent.isOnNode() && agent.getCurrentOrPreviousEdge() != null) {
                Edge edge = agent.getCurrentOrPreviousEdge();
                Node previous = agent.getPreviousOrCurrentNode();
                Node target = edge.getOppositeNode(previous);
                double ratio = Math.max(0, agent.getCurrentEdgeProgress());
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


    /**
     * Calculate the shortest distance from a point (px, py) to a line segment defined by endpoints (x1, y1) and (x2, y2).
     * @param px 
     * @param py
     * @param x1
     * @param y1
     * @param x2
     * @param y2 
     * @return the shortest distance from the point to the line segment
     */
    private double distancePointToSegment(double px, double py, double x1, double y1, double x2, double y2) {
        double l2 = Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2);
        if (l2 == 0)
            return Math.hypot(px - x1, py - y1); // Le segment est un point

        //Compute the projection of point p onto the line defined by points (x1, y1) and (x2, y2)
        double t = Math.max(0, Math.min(1, ((px - x1) * (x2 - x1) + (py - y1) * (y2 - y1)) / l2));

        double projX = x1 + t * (x2 - x1);
        double projY = y1 + t * (y2 - y1);

        return Math.hypot(px - projX, py - projY);
    }

    private void handleModification(Object entity) {
        javafx.application.Platform.runLater(() -> {
            if (entity instanceof Node node) {
                fr.cy.view.DialogHelper.showNodeUpdateDialog(node, canvas).ifPresent(params -> {
                    node.setCapacity(params.capacity);
                    node.setExit(params.isExit);
                });
            } else if (entity instanceof Edge edge) {
                // Pass the parentNode (e.g., the canvas itself) to center the dialog
                fr.cy.view.DialogHelper.showEdgeUpdateDialog(edge, canvas).ifPresent(params -> {
                    edge.setWidth(params.width);
                    edge.setLength(params.length);
                    edge.setDirected(params.directed);
                });
            }
        });
    }

    private GraphElement findClosestElement(double mx, double my) {
        fr.cy.model.graph.Graph graph = simController.getSimulation().getGraph();

        // 1. Search for the closest Node
        fr.cy.model.graph.element.Node closestNode = null;
        double minNodeDist = Double.MAX_VALUE;

        for (fr.cy.model.graph.element.Node node : graph.getNodes()) {
            double dist = Math.hypot(node.getX() - mx, node.getY() - my);
            // We suppose that the node has a visual radius (e.g., 10) + our tolerance to make clicking easier
            double hitRadius = 10.0 + CLICK_TOLERANCE;

            if (dist <= hitRadius && dist < minNodeDist) {
                minNodeDist = dist;
                closestNode = node;
            }
        }

        // if we hit a node, we stop here (Nodes have priority over edges!)
        if (closestNode != null) {
            return closestNode;
        }

        // 2. If no node is hit, we look for Edges
        fr.cy.model.graph.element.Edge closestEdge = null;
        double minEdgeDist = Double.MAX_VALUE;

        for (fr.cy.model.graph.element.Edge edge : graph.getEdges()) {
            // Mathématic formula to calculate distance from point to segment
            double dist = distancePointToSegment(mx, my,
                    edge.getStart().getX(), edge.getStart().getY(),
                    edge.getEnd().getX(), edge.getEnd().getY());

            double hitWidth = (edge.getWidth() / 2.0) + CLICK_TOLERANCE;

            if (dist <= hitWidth && dist < minEdgeDist) {
                minEdgeDist = dist;
                closestEdge = edge;
            }
        }

        return closestEdge;
    }

}