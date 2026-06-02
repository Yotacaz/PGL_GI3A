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

    // Marge d'erreur en unités pour faciliter le clic (ex: 15 unités autour de
    // l'objet)
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
                // Un viseur précis pour poser un nœud
                canvas.setCursor(Cursor.CROSSHAIR);
                break;

            case ADD_EDGE_START:
                // Un viseur différent (ou le même) pour tracer une ligne
                canvas.setCursor(Cursor.CROSSHAIR);
                break;
            case ADD_AGENT:
                canvas.setCursor(Cursor.CROSSHAIR);
                break;

            case SELECT_AND_DRAG:
            default:
                // La flèche classique
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
                    canvas.setCursor(Cursor.CLOSED_HAND); // Main qui attrape le nœud
                } else {
                    canvas.setCursor(Cursor.MOVE); // Flèches directionnelles pour déplacer la caméra
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
                // On calcule la position de la souris dans le graphe
                double mx = (event.getX() - canvas.getPanX()) / canvas.getZoom();
                double my = (event.getY() - canvas.getPanY()) / canvas.getZoom();

                // Si la souris passe au-dessus d'un agent, d'un noeud ou d'une arête
                if (findClosestAgent(mx, my) != null || findClosestElement(mx, my) != null) {
                    canvas.setCursor(Cursor.HAND); // Main ouverte (Prêt à cliquer)
                } else {
                    canvas.setCursor(Cursor.DEFAULT); // Flèche normale dans le vide
                }
            } else {
                // Si on est en mode "Création" (Boutons bleu ou gris appuyés)
                canvas.setCursor(Cursor.CROSSHAIR); // Viseur de précision
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

            // On récupère le graphe depuis le contrôleur
            fr.cy.model.graph.Graph graph = simController.getSimulation().getGraph();

            // =========================================================
            // MODE 1 : AJOUTER UN NŒUD
            // =========================================================
            if (currentMode == InteractionMode.ADD_NODE) {
                try {
                    DialogHelper.showNodeCreationDialog(canvas)
                            .ifPresent(capacity -> {
                                Node newNode = graph.createNode(mx, my, capacity);
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
            // MODE 2 : AJOUTER UNE ARÊTE
            // =========================================================
            if (currentMode == InteractionMode.ADD_EDGE_START) {
                GraphElement clickedElement = findClosestElement(mx, my);

                if (clickedElement instanceof Node clickedNode) {
                    if (edgeStartNode == null) {
                        // Premier clic
                        edgeStartNode = clickedNode;
                        canvas.setSelectedEntity(edgeStartNode);
                    } else if (edgeStartNode != clickedNode) {

                        // 1. Calcul de la distance
                        double pixelDistance = Math.hypot(edgeStartNode.getX() - clickedNode.getX(),
                                edgeStartNode.getY() - clickedNode.getY());
                        double logicalLength = pixelDistance / 10.0; // Ton échelle de distance

                        // 2. On mémorise les nœuds dans des variables temporaires avant d'ouvrir la
                        // fenêtre
                        Node startNode = edgeStartNode;
                        Node endNode = clickedNode;

                        // 3. ON RÉINITIALISE L'OUTIL IMMÉDIATEMENT (pour éviter les bugs si on annule)
                        edgeStartNode = null;
                        canvas.setSelectedEntity(null);

                        DialogHelper.showEdgeCreationDialog(startNode, endNode, canvas)
                                .ifPresent(params -> {
                                    // 5. On crée l'arête avec les vrais paramètres choisis par l'utilisateur
                                    Edge newEdge = graph.createEdge(startNode, endNode, logicalLength, params.width,
                                            params.directed);

                                    // On sélectionne la nouvelle arête pour afficher ses infos à droite
                                    canvas.setSelectedEntity(newEdge);
                                    notifySelection(newEdge);
                                });
                    }
                }
                return;
            }

            // =========================================================
            // MODE 3 : AJOUTER DES AGENTS SUR UN NŒUD
            // =========================================================
            if (currentMode == InteractionMode.ADD_AGENT) {
                GraphElement clickedElement = findClosestElement(mx, my);
                if (clickedElement instanceof Node clickedNode) {

                    // --- LA CORRECTION EST ICI ---
                    // 1. On sélectionne visuellement le nœud pour ouvrir le panneau de droite
                    canvas.setSelectedEntity(clickedNode);
                    notifySelection(clickedNode);

                    // 2. On ouvre la fenêtre pour demander combien d'agents
                    if (onAddAgentRequested != null) {
                        onAddAgentRequested.accept(clickedNode);
                    }
                }
                return;
            }

            // =========================================================
            // MODE NORMAL (SÉLECTION ET INFOS) - TON CODE ORIGINAL
            // =========================================================
            if (currentMode == InteractionMode.SELECT_AND_DRAG || currentMode == null) {
                Object selectedEntity = null;

                Agent clickedAgent = findClosestAgent(mx, my);
                if (clickedAgent != null) {
                    selectedEntity = clickedAgent;
                } else {
                    GraphElement clickedElement = findClosestElement(mx, my);
                    if (clickedElement != null) {
                        selectedEntity = clickedElement;
                    }
                }

                canvas.setSelectedEntity(selectedEntity);
                notifySelection(selectedEntity);
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

    /**
     * Calcule la distance la plus courte entre un point (px, py) et un segment de
     * droite (x1,y1) -> (x2,y2).
     */
    private double distancePointToSegment(double px, double py, double x1, double y1, double x2, double y2) {
        double l2 = Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2);
        if (l2 == 0)
            return Math.hypot(px - x1, py - y1); // Le segment est un point

        // Calcul de la projection du point sur la ligne (clamped entre 0 et 1 pour
        // rester sur le segment)
        double t = Math.max(0, Math.min(1, ((px - x1) * (x2 - x1) + (py - y1) * (y2 - y1)) / l2));

        double projX = x1 + t * (x2 - x1);
        double projY = y1 + t * (y2 - y1);

        return Math.hypot(px - projX, py - projY);
    }

    private GraphElement findClosestElement(double mx, double my) {
        fr.cy.model.graph.Graph graph = simController.getSimulation().getGraph();

        // 1. Chercher le Nœud le plus proche
        fr.cy.model.graph.element.Node closestNode = null;
        double minNodeDist = Double.MAX_VALUE;

        for (fr.cy.model.graph.element.Node node : graph.getNodes()) {
            double dist = Math.hypot(node.getX() - mx, node.getY() - my);
            // On considère que le nœud a un rayon visuel (ex: 10) + notre tolérance
            double hitRadius = 10.0 + CLICK_TOLERANCE;

            if (dist <= hitRadius && dist < minNodeDist) {
                minNodeDist = dist;
                closestNode = node;
            }
        }

        // Si on a touché un nœud, on s'arrête là (Priorité aux nœuds !)
        if (closestNode != null) {
            return closestNode;
        }

        // 2. Si aucun nœud touché, on cherche les Arêtes
        fr.cy.model.graph.element.Edge closestEdge = null;
        double minEdgeDist = Double.MAX_VALUE;

        for (fr.cy.model.graph.element.Edge edge : graph.getEdges()) {
            // Utilisation d'une formule mathématique pour calculer la distance à un segment
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