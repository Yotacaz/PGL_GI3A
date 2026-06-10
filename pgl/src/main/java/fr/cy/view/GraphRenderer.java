package fr.cy.view;

import java.util.Objects;

import fr.cy.model.agent.Agent;
import fr.cy.model.agent.behaviour.properties.EmotionalState;
import fr.cy.model.graph.Graph;
import fr.cy.model.graph.element.Edge;
import fr.cy.model.graph.element.Node;
import fr.cy.model.simulation.Simulation;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;

/**
 * The {@code GraphRenderer} class handles the real-time drawing operations
 * of the simulation engine onto a JavaFX Canvas context.
 * <p>
 * It renders background grids, graph components (nodes, edges), dynamic hazards
 * (fires), crowd populations (agents), and interactive selection target
 * highlights.
 * </p>
 */
public class GraphRenderer {

    private final GraphicsContext gc;

    // --- Centralized Theme Integration ---
    private static final Color BG_COLOR = Color.web(ThemeConstants.CANVAS_BG);
    private static final Color EDGE_COLOR = Color.web(ThemeConstants.GRAPH_EDGE);
    private static final Color EDGE_CONGESTED_COLOR = Color.web(ThemeConstants.GRAPH_EDGE_CONGESTED);
    private static final Color CALM_NODE_COLOR = Color.web(ThemeConstants.GRAPH_NODE_CALM);

    // Hazards & States mapped to global UI rules
    private static final Color FIRE_COLOR = Color.web(ThemeConstants.WARNING_ORANGE);
    private static final Color EXIT_NODE_COLOR = Color.web(ThemeConstants.SUCCESS_GREEN);
    private static final Color STRESS_COLOR = Color.web(ThemeConstants.DANGER_RED);

    // Agent population tracking
    private static final Color AGENT_CALM = Color.web(ThemeConstants.SUCCESS_GREEN);
    private static final Color AGENT_SELFISH = Color.web(ThemeConstants.AGENT_SELFISH);
    private static final Color AGENT_PANICKING = Color.web(ThemeConstants.DANGER_RED);
    private static final Color AGENT_DEAD = Color.web(ThemeConstants.AGENT_DEAD);

    // Interactive selections
    private static final Color SELECTED_COLOR = Color.web(ThemeConstants.SELECTION_CYAN);
    private static final Color TARGET_HIGHLIGHT_COLOR = Color.web(ThemeConstants.TARGET_HIGHLIGHT);

    // Scaling constants
    private static final double NODE_RADIUS = 22.0;
    private static final double PIXELS_PER_UNIT = 5.0;
    private static final double EDGE_MIN_WIDTH = 4.0;

    /**
     * Constructs the renderer with the specified {@link GraphicsContext}.
     *
     * @param gc The JavaFX GraphicsContext used for all drawing operations.
     */
    public GraphRenderer(GraphicsContext gc) {
        this.gc = gc;
    }

    /**
     * Renders the entire simulation state onto the provided canvas view scope.
     */
    public void render(Simulation simulation, GraphCanvas canvas) {
        clearAndResetCanvas(canvas);

        if (simulation == null || simulation.getGraph() == null)
            return;

        gc.save();
        gc.translate(canvas.getPanX(), canvas.getPanY());
        gc.scale(canvas.getZoom(), canvas.getZoom());

        Object selectedEntity = canvas.getSelectedEntity();
        Graph graph = simulation.getGraph();

        // 1. Render structural layers
        for (Edge edge : graph.getEdges()) {
            drawEdge(edge, selectedEntity);
        }
        for (Node node : graph.getNodes()) {
            drawNode(node, selectedEntity);
        }

        // 2. Render population layers
        if (simulation.getAgentManager() != null) {
            Agent selectedAgent = selectedEntity instanceof Agent ? (Agent) selectedEntity : null;

            for (Agent agent : simulation.getAgentManager().getAgentsToEvacuate()) {
                drawAgent(agent, selectedAgent, false);
            }
            for (Agent agent : simulation.getAgentManager().getDeadAgents()) {
                drawAgent(agent, selectedAgent, true);
            }
        }

        gc.restore();
    }

    /** Helper: Clears previous frame and resets transforms. */
    private void clearAndResetCanvas(GraphCanvas canvas) {
        gc.save();
        gc.setTransform(1, 0, 0, 1, 0, 0);
        gc.setFill(BG_COLOR);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        gc.restore();
    }

    // ========================================================================
    // STRUCTURAL RENDERING (NODES & EDGES)
    // ========================================================================

    /**
     * Draws an edge layout segment including direction markers, heatmaps, and
     * hazards.
     */
    private void drawEdge(Edge edge, Object selectedEntity) {
        Node start = edge.getStart();
        Node end = edge.getEnd();

        double dx = end.getX() - start.getX();
        double dy = end.getY() - start.getY();
        double centerDistance = Math.hypot(dx, dy);

        double startRadius = Math.max(0, getNodeVisualRadius(start) - 3.0);
        double endRadius = Math.max(0, getNodeVisualRadius(end) - 3.0);

        // Calculate visual perimeter offsets
        double borderStartX = start.getX(), borderStartY = start.getY();
        double borderEndX = end.getX(), borderEndY = end.getY();

        if (centerDistance > 0) {
            borderStartX += (dx / centerDistance) * startRadius;
            borderStartY += (dy / centerDistance) * startRadius;
            borderEndX -= (dx / centerDistance) * endRadius;
            borderEndY -= (dy / centerDistance) * endRadius;
        }

        double visualWidth = getEdgeVisualWidth(edge);
        double visDx = borderEndX - borderStartX;
        double visDy = borderEndY - borderStartY;

        // Render selection glow
        if (edge.equals(selectedEntity)) {
            drawLineHalo(borderStartX, borderStartY, borderEndX, borderEndY, visualWidth, 4.0, SELECTED_COLOR);
        }

        // Render base structural edge with congestion gradient
        double congestionRatio = edge.getCapacity() > 0
                ? Math.min(1.0, (double) edge.getAgents().size() / edge.getCapacity())
                : 0.0;
        Color currentEdgeColor = EDGE_COLOR.interpolate(EDGE_CONGESTED_COLOR, congestionRatio);

        gc.setStroke(currentEdgeColor);
        gc.setLineWidth(visualWidth);
        gc.setLineCap(StrokeLineCap.BUTT);
        gc.strokeLine(borderStartX, borderStartY, borderEndX, borderEndY);

        // Decorators
        if (edge.isDirected()) {
            drawDirectionChevrons(borderStartX, borderStartY, visDx, visDy, visualWidth, currentEdgeColor);
        }
        if (edge.isOnFire() && (edge.isBurningFromStart() || edge.isBurningFromEnd())) {
            drawFirePropagation(edge, borderStartX, borderStartY, borderEndX, borderEndY, visDx, visDy, visualWidth);
        }
    }

    /** Helper: Draws direction indicators along the edge path. */
    private void drawDirectionChevrons(double startX, double startY, double dx, double dy, double width,
            Color baseColor) {
        double length = Math.hypot(dx, dy);
        if (length <= 0)
            return;

        double angle = Math.atan2(dy, dx);
        double chevronSize = 8.0 + (width * 0.4);
        int numChevrons = Math.max(1, (int) (length / 200.0)); // Spacing = 200px

        gc.setStroke(baseColor.brighter());
        gc.setLineWidth(Math.max(2.0, width * 0.15));

        for (int i = 1; i <= numChevrons; i++) {
            double ratio = (double) i / (numChevrons + 1);
            double tipX = startX + dx * ratio + (chevronSize * 0.5) * Math.cos(angle);
            double tipY = startY + dy * ratio + (chevronSize * 0.5) * Math.sin(angle);

            double leftX = tipX - chevronSize * Math.cos(angle - Math.PI / 4);
            double leftY = tipY - chevronSize * Math.sin(angle - Math.PI / 4);
            double rightX = tipX - chevronSize * Math.cos(angle + Math.PI / 4);
            double rightY = tipY - chevronSize * Math.sin(angle + Math.PI / 4);

            gc.strokePolyline(new double[] { leftX, tipX, rightX }, new double[] { leftY, tipY, rightY }, 3);
        }
    }

    /** Helper: Renders fire damage tracking along the edge infrastructure. */
    private void drawFirePropagation(Edge edge, double startX, double startY, double endX, double endY, double dx,
            double dy, double width) {
        double ratio = Math.min(1.0, edge.getBurnedDistance() / edge.getLength());
        gc.setLineWidth(width);
        gc.setStroke(FIRE_COLOR.deriveColor(0, 1, 1, 0.8));

        if (edge.isBurningFromStart())
            gc.strokeLine(startX, startY, startX + dx * ratio, startY + dy * ratio);
        if (edge.isBurningFromEnd())
            gc.strokeLine(endX, endY, endX - dx * ratio, endY - dy * ratio);
    }

    /**
     * Draws a node infrastructure item handling color blending states and radial
     * glows.
     */
    private void drawNode(Node node, Object selectedEntity) {
        double radius = getNodeVisualRadius(node);
        double x = node.getX(), y = node.getY();

        if (node.equals(selectedEntity)) {
            drawCircularHalo(x, y, radius, 6.0);
        }

        // Determine base node state color
        Color nodeColor = CALM_NODE_COLOR.interpolate(STRESS_COLOR, node.getStressInducingImpact());
        if (node.isExit())
            nodeColor = EXIT_NODE_COLOR;
        if (node.isOnFire())
            nodeColor = FIRE_COLOR.interpolate(Color.web("#B71C1C"), node.getFire().getIntensity());

        // Draw animated hazard/exit pulses
        if (node.isExit() || node.isOnFire()) {
            double glowRadius = node.isOnFire() ? 30 : 10;
            Color glowColor = node.isExit() ? EXIT_NODE_COLOR : FIRE_COLOR;
            for (int i = 1; i <= 3; i++) {
                gc.setFill(glowColor.deriveColor(0, 1, 1, 0.2 / i));
                double pulse = 1.0 + 0.1 * Math.sin(System.currentTimeMillis() * 0.008);
                double r = (radius + (glowRadius * i / 3)) * pulse;
                gc.fillOval(x - r, y - r, r * 2, r * 2);
            }
        }

        // Core structure
        gc.setFill(nodeColor);
        gc.fillOval(x - radius, y - radius, radius * 2, radius * 2);
        gc.setStroke(Color.web("#2A2A35"));
        gc.setLineWidth(2);
        gc.strokeOval(x - radius, y - radius, radius * 2, radius * 2);
    }

    // ========================================================================
    // POPULATION RENDERING (AGENTS)
    // ========================================================================

    /**
     * Draws an individual agent calculation model.
     */
    private void drawAgent(Agent agent, Object selectedEntity, boolean isDead) {
        double[] coords = calculateAgentCoordinates(agent);
        if (coords == null)
            return; // Agent is not placed on the physical graph

        double ax = coords[0], ay = coords[1];
        double visualRadius = Math.sqrt(agent.getSurfaceAreaTakenByAgent() / Math.PI) * PIXELS_PER_UNIT;
        double visualDiameter = visualRadius * 2.0;

        // Render selection and pathfinding targets
        if (agent.equals(selectedEntity)) {
            drawCircularHalo(ax, ay, visualRadius, 3.0);

            if (!isDead) {
                Node targetNode = agent.getCurrentNodeOrNextNodeIfOnEdge();
                if (agent.getCurrentEdgeProgress() >= 0.0 && agent.getCurrentEdgeProgress() <= 1.0
                        && agent.getCurrentOrPreviousEdge() != null) {
                    drawEdgeTargetHighlight(agent.getCurrentOrPreviousEdge(), agent, agent.getCurrentEdgeProgress());
                }
                if (targetNode != null)
                    drawTargetNodeHighlight(targetNode);
            }
        }

        // Render physical body
        gc.setFill(isDead ? AGENT_DEAD : getAgentStateColor(agent.getEmotionalState()));
        gc.fillOval(ax - visualRadius, ay - visualRadius, visualDiameter, visualDiameter);
        gc.setStroke(Color.web("#121212"));
        gc.setLineWidth(1);
        gc.strokeOval(ax - visualRadius, ay - visualRadius, visualDiameter, visualDiameter);
    }

    /** Helper: Maps the emotional enum to the visual palette. */
    private Color getAgentStateColor(EmotionalState state) {
        return switch (state) {
            case CALM -> AGENT_CALM;
            case SELFISH -> AGENT_SELFISH;
            case PANICKING -> AGENT_PANICKING;
        };
    }

    /**
     * Helper: Derives the physical X/Y canvas coordinates based on network
     * position.
     */
    private double[] calculateAgentCoordinates(Agent agent) {
        if (agent.isOnNode()) {
            Node node = agent.getCurrentNode();
            double visualRadius = Math.sqrt(agent.getSurfaceAreaTakenByAgent() / Math.PI) * PIXELS_PER_UNIT;
            double maxOffset = Math.max(0, getNodeVisualRadius(node) - visualRadius);

            // Sunflower seed arrangement pattern
            double angle = agent.getId() * 137.508;
            double dist = Math.sqrt((agent.getId() * 11.3) % 100 / 100.0) * maxOffset;

            return new double[] {
                    node.getX() + Math.cos(Math.toRadians(angle)) * dist,
                    node.getY() + Math.sin(Math.toRadians(angle)) * dist
            };
        } else if (agent.isOnEdge()) {
            Edge edge = agent.getCurrentOrPreviousEdge();
            Node target = Objects.requireNonNull(agent.getCurrentNodeOrNextNodeIfOnEdge());
            Node previous = Objects.requireNonNull(edge.getOppositeNode(target));

            double ratio = Math.max(0, agent.getCurrentEdgeProgress());
            return new double[] {
                    previous.getX() + (target.getX() - previous.getX()) * ratio,
                    previous.getY() + (target.getY() - previous.getY()) * ratio
            };
        }
        return null;
    }

    // ========================================================================
    // VISUAL EFFECT UTILITIES
    // ========================================================================

    private void drawCircularHalo(double centerX, double centerY, double baseRadius, double padding) {
        double totalRadius = baseRadius + padding;
        gc.setFill(SELECTED_COLOR.deriveColor(0, 1, 1, 0.4));
        gc.fillOval(centerX - totalRadius, centerY - totalRadius, totalRadius * 2, totalRadius * 2);
        gc.setStroke(SELECTED_COLOR);
        gc.setLineWidth(Math.max(2.0, padding / 2.0));
        gc.strokeOval(centerX - totalRadius, centerY - totalRadius, totalRadius * 2, totalRadius * 2);
    }

    private void drawLineHalo(double startX, double startY, double endX, double endY, double baseWidth, double padding,
            Color color) {
        gc.setStroke(color.deriveColor(0, 1, 1, 0.6));
        gc.setLineWidth(baseWidth + (padding * 2));
        gc.setLineCap(StrokeLineCap.BUTT);
        gc.strokeLine(startX, startY, endX, endY);
    }

    private void drawTargetNodeHighlight(Node node) {
        gc.save();
        gc.setStroke(TARGET_HIGHLIGHT_COLOR);
        gc.setLineWidth(3.0);
        double highlightRadius = getNodeVisualRadius(node) + 5.0;
        gc.strokeOval(node.getX() - highlightRadius, node.getY() - highlightRadius, highlightRadius * 2,
                highlightRadius * 2);
        gc.restore();
    }

    private void drawEdgeTargetHighlight(Edge edge, Agent agent, double progress) {
        Node targetNode = agent.getCurrentNodeOrNextNodeIfOnEdge();
        Node departureNode = edge.getOppositeNode(targetNode);
        if (targetNode == null || departureNode == null)
            return;

        double currentX = departureNode.getX() + (targetNode.getX() - departureNode.getX()) * progress;
        double currentY = departureNode.getY() + (targetNode.getY() - departureNode.getY()) * progress;

        double dx = targetNode.getX() - currentX;
        double dy = targetNode.getY() - currentY;
        double distanceRemaining = Math.hypot(dx, dy);
        double targetRadius = getNodeVisualRadius(targetNode);

        if (distanceRemaining > targetRadius) {
            double nx = -dy / distanceRemaining;
            double ny = dx / distanceRemaining;
            double outerOffset = getEdgeVisualWidth(edge) / 2.0;

            double adjDestX = targetNode.getX() - (dx / distanceRemaining) * targetRadius;
            double adjDestY = targetNode.getY() - (dy / distanceRemaining) * targetRadius;

            gc.save();
            gc.setStroke(TARGET_HIGHLIGHT_COLOR);
            gc.setLineWidth(2.0);
            gc.setLineCap(StrokeLineCap.BUTT);

            gc.strokeLine(currentX + nx * outerOffset, currentY + ny * outerOffset, adjDestX + nx * outerOffset,
                    adjDestY + ny * outerOffset);
            gc.strokeLine(currentX - nx * outerOffset, currentY - ny * outerOffset, adjDestX - nx * outerOffset,
                    adjDestY - ny * outerOffset);
            gc.strokeLine(currentX + nx * outerOffset, currentY + ny * outerOffset, currentX - nx * outerOffset,
                    currentY - ny * outerOffset);
            gc.restore();
        }
    }

    // Mathematical sizing utilities
    private double getNodeVisualRadius(Node node) {
        double maxEdgeWidth = node.getEdges().stream().mapToDouble(e -> e.getWidth() * PIXELS_PER_UNIT).max().orElse(0);
        double diameter = Math.max(Math.sqrt(node.getCapacity() / Math.PI) * PIXELS_PER_UNIT * 2, maxEdgeWidth);
        return Math.max(diameter, NODE_RADIUS * 1.5) / 2.0;
    }

    private double getEdgeVisualWidth(Edge edge) {
        return Math.max(EDGE_MIN_WIDTH, edge.getWidth() * PIXELS_PER_UNIT);
    }
}