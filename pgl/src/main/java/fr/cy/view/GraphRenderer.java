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

    // Modern Dark Theme color palette
    private static final Color BG_COLOR = Color.web("#121212");
    private static final Color EDGE_COLOR = Color.web("#5A5A8A");
    private static final Color FIRE_COLOR = Color.web("#FF5722");
    private static final Color EDGE_CONGESTED_COLOR = Color.web("#E91E63");

    private static final Color CALM_NODE_COLOR = Color.web("#007ACC");
    private static final Color EXIT_NODE_COLOR = Color.web("#2ECC71");
    private static final Color STRESS_COLOR = Color.web("#D32F2F");

    private static final Color AGENT_CALM = Color.web("#4ADE80");
    private static final Color AGENT_SELFISH = Color.web("#FF8C00");
    private static final Color AGENT_PANICKING = Color.web("#FF2020");
    private static final Color AGENT_DEAD = Color.web("#7F8C8D");

    private static final Color SELECTED_COLOR = Color.web("#00FFFF");
    private static final Color TARGET_HIGHLIGHT_COLOR = Color.ORANGE;

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
     * <p>
     * Updates background frames, graph components, and runs individual rendering
     * iterations for both living evacuation crowds and recorded casualties.
     * </p>
     *
     * @param simulation The active simulation instance.
     * @param canvas     The canvas component managing camera view parameters.
     */
    public void render(Simulation simulation, GraphCanvas canvas) {
        double width = canvas.getWidth();
        double height = canvas.getHeight();

        // 1. Reset base view bounds
        gc.save();
        gc.setTransform(1, 0, 0, 1, 0, 0);
        gc.setFill(BG_COLOR);
        gc.fillRect(0, 0, width, height);
        gc.restore();

        if (simulation == null)
            return;
        Graph graph = simulation.getGraph();
        if (graph == null)
            return;

        // 2. Apply camera transformations
        gc.save();
        gc.translate(canvas.getPanX(), canvas.getPanY());
        gc.scale(canvas.getZoom(), canvas.getZoom());

        Object selectedEntity = canvas.getSelectedEntity();

        // 3. Render graph structural topology layers
        for (Edge edge : graph.getEdges()) {
            drawEdge(edge, selectedEntity);
        }

        for (Node node : graph.getNodes()) {
            drawNode(node, selectedEntity);
        }

        // 4. Render active agent crowds (both alive and dead populations)
        if (simulation.getAgentManager() != null) {
            // Isolate selection entity casting once to keep loop calls clean
            Agent selectedAgent = selectedEntity instanceof Agent ? (Agent) selectedEntity : null;

            // Render live evacuation population vectors
            for (Agent agent : simulation.getAgentManager().getAgentsToEvacuate()) {
                drawAgent(agent, selectedAgent, false);
            }

            // Render static casualty populations in place
            for (Agent agent : simulation.getAgentManager().getDeadAgents()) {
                drawAgent(agent, selectedAgent, true);
            }
        }

        gc.restore();
    }

    /**
     * Calculates the dynamic visual scaling radius of a given node.
     *
     * @param node The node target.
     * @return The derived layout radius dimensions in pixels.
     */
    private double getNodeVisualRadius(Node node) {
        double maxEdgeWidth = 0.0;
        for (Edge edge : node.getEdges()) {
            double edgeVisualWidth = edge.getWidth() * PIXELS_PER_UNIT;
            if (edgeVisualWidth > maxEdgeWidth) {
                maxEdgeWidth = edgeVisualWidth;
            }
        }
        double diameter = Math.max(Math.sqrt(node.getCapacity() / Math.PI) * PIXELS_PER_UNIT * 2, maxEdgeWidth);
        diameter = Math.max(diameter, NODE_RADIUS * 1.5);
        return diameter / 2.0;
    }

    /**
     * Extracts and bounds standard normalized edge width metrics.
     *
     * @param edge The edge segment layout to scale.
     * @return The scaled layout width bounds in pixels.
     */
    private double getEdgeVisualWidth(Edge edge) {
        return Math.max(EDGE_MIN_WIDTH, edge.getWidth() * PIXELS_PER_UNIT);
    }

    /**
     * Draws an edge layout segment including direction markers, heatmaps, and
     * hazards.
     */
    private void drawEdge(Edge edge, Object selectedEntity) {
        boolean isSelected = edge.equals(selectedEntity);
        Node start = edge.getStart();
        Node end = edge.getEnd();

        double sx = start.getX(), sy = start.getY();
        double ex = end.getX(), ey = end.getY();

        double dx = ex - sx;
        double dy = ey - sy;
        double centerDistance = Math.hypot(dx, dy);

        double startRadius = Math.max(0, getNodeVisualRadius(start) - 3.0);
        double endRadius = Math.max(0, getNodeVisualRadius(end) - 3.0);

        double borderStartX = sx, borderStartY = sy;
        double borderEndX = ex, borderEndY = ey;

        if (centerDistance > 0) {
            borderStartX = sx + (dx / centerDistance) * startRadius;
            borderStartY = sy + (dy / centerDistance) * startRadius;
            borderEndX = ex - (dx / centerDistance) * endRadius;
            borderEndY = ey - (dy / centerDistance) * endRadius;
        }

        double visDx = borderEndX - borderStartX;
        double visDy = borderEndY - borderStartY;
        double visualLength = Math.hypot(visDx, visDy);
        double visualWidth = getEdgeVisualWidth(edge);

        // Render backing selection glow if highlighted
        if (isSelected) {
            drawLineHalo(borderStartX, borderStartY, borderEndX, borderEndY, visualWidth, 4.0, SELECTED_COLOR);
        }

        // Render core structural network lane
        double congestionRatio = edge.getCapacity() > 0
                ? Math.min(1.0, (double) edge.getAgents().size() / edge.getCapacity())
                : 0.0;
        Color currentEdgeColor = EDGE_COLOR.interpolate(EDGE_CONGESTED_COLOR, congestionRatio);

        gc.setStroke(currentEdgeColor);
        gc.setLineWidth(visualWidth);
        gc.setLineCap(javafx.scene.shape.StrokeLineCap.BUTT);
        gc.strokeLine(borderStartX, borderStartY, borderEndX, borderEndY);

        // Direction Chevrons
        if (edge.isDirected() && visualLength > 0) {
            double angle = Math.atan2(visDy, visDx);
            double chevronSize = 8.0 + (visualWidth * 0.4);
            double spacing = 200.0;
            int numChevrons = Math.max(1, (int) (visualLength / spacing));

            gc.setStroke(currentEdgeColor.brighter());
            gc.setLineWidth(Math.max(2.0, visualWidth * 0.15));
            for (int i = 1; i <= numChevrons; i++) {
                double ratio = (double) i / (numChevrons + 1);
                double cx = borderStartX + visDx * ratio;
                double cy = borderStartY + visDy * ratio;

                double tipX = cx + (chevronSize * 0.5) * Math.cos(angle);
                double tipY = cy + (chevronSize * 0.5) * Math.sin(angle);
                double leftX = tipX - chevronSize * Math.cos(angle - Math.PI / 4);
                double leftY = tipY - chevronSize * Math.sin(angle - Math.PI / 4);
                double rightX = tipX - chevronSize * Math.cos(angle + Math.PI / 4);
                double rightY = tipY - chevronSize * Math.sin(angle + Math.PI / 4);

                gc.strokePolyline(new double[] { leftX, tipX, rightX }, new double[] { leftY, tipY, rightY }, 3);
            }
        }

        // Fire propagation path tracing
        if (edge.isOnFire() && (edge.isBurningFromStart() || edge.isBurningFromEnd()) && visualLength > 0) {
            double ratio = Math.min(1.0, edge.getBurnedDistance() / edge.getLength());
            gc.setLineWidth(visualWidth);
            gc.setStroke(FIRE_COLOR.deriveColor(0, 1, 1, 0.8));
            if (edge.isBurningFromStart()) {
                gc.strokeLine(borderStartX, borderStartY, borderStartX + visDx * ratio, borderStartY + visDy * ratio);
            }
            if (edge.isBurningFromEnd()) {
                gc.strokeLine(borderEndX, borderEndY, borderEndX - visDx * ratio, borderEndY - visDy * ratio);
            }
        }
    }

    /**
     * Draws a node infrastructure item handling color blending states and radial
     * glows.
     */
    private void drawNode(Node node, Object selectedEntity) {
        boolean isSelected = node.equals(selectedEntity);
        double radius = getNodeVisualRadius(node);
        double x = node.getX(), y = node.getY();

        if (isSelected) {
            drawCircularHalo(x, y, radius, 6.0);
        }

        Color nodeColor = CALM_NODE_COLOR.interpolate(STRESS_COLOR, node.getStressInducingImpact());
        if (node.isExit())
            nodeColor = EXIT_NODE_COLOR;
        if (node.isOnFire()) {
            nodeColor = FIRE_COLOR.interpolate(Color.web("#B71C1C"), node.getFire().getIntensity());
        }

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

        gc.setFill(nodeColor);
        gc.fillOval(x - radius, y - radius, radius * 2, radius * 2);
        gc.setStroke(Color.web("#2A2A35"));
        gc.setLineWidth(2);
        gc.strokeOval(x - radius, y - radius, radius * 2, radius * 2);
    }

    /**
     * Draws an individual agent calculation model mapped either inside nodes or
     * traversing tracking lines.
     */
    /**
     * Draws an individual agent model mapped either inside nodes or traversing
     * tracking lines.
     * <p>
     * It handles contextual selections by overlaying route guidance vectors if the
     * agent is alive.
     * Dead agents automatically bypass path decorations and drop down to a static
     * gray color palette.
     * </p>
     *
     * @param agent          The agent instance to render.
     * @param selectedEntity The currently selected tracking entity context.
     * @param isDead         True if the entity belongs to the static dead tracking
     *                       database layer.
     */
    private void drawAgent(Agent agent, Object selectedEntity, boolean isDead) {
        double ax, ay;
        double visualRadius = Math.sqrt(agent.getSurfaceAreaTakenByAgent() / Math.PI) * PIXELS_PER_UNIT;
        double visualDiameter = visualRadius * 2.0;

        // 1. Calculate structural rendering coordinates
        if (agent.isOnNode()) {
            Node node = agent.getCurrentNode();
            double maxOffset = Math.max(0, getNodeVisualRadius(node) - visualRadius);
            double angle = agent.getId() * 137.508;
            double dist = Math.sqrt((agent.getId() * 11.3) % 100 / 100.0) * maxOffset;
            ax = node.getX() + Math.cos(Math.toRadians(angle)) * dist;
            ay = node.getY() + Math.sin(Math.toRadians(angle)) * dist;
        } else {
            Edge edge = agent.getCurrentOrPreviousEdge();
            Node target = Objects.requireNonNull(agent.getCurrentNodeOrNextNodeIfOnEdge());
            Node previous = Objects.requireNonNull(edge.getOppositeNode(target));

            double ratio = Math.max(0, agent.getCurrentEdgeProgress());
            ax = previous.getX() + (target.getX() - previous.getX()) * ratio;
            ay = previous.getY() + (target.getY() - previous.getY()) * ratio;
        }

        // 2. Process interactive selection overlays
        if (agent.equals(selectedEntity)) {
            drawCircularHalo(ax, ay, visualRadius, 3.0);

            // Only display route path bounding boxes if the agent is still alive and moving
            if (!isDead) {
                Node targetNode = agent.getCurrentNodeOrNextNodeIfOnEdge();
                double progress = agent.getCurrentEdgeProgress();

                if (progress >= 0.0 && progress <= 1.0) {
                    Edge currentEdge = agent.getCurrentOrPreviousEdge();
                    if (currentEdge != null) {
                        drawEdgeTargetHighlight(gc, currentEdge, agent, progress);
                    }
                }

                if (targetNode != null) {
                    drawTargetNodeHighlight(gc, targetNode);
                }
            }
        }

        // 3. Resolve profile paint mapping configs
        Color agentColor;
        if (isDead) {
            agentColor = AGENT_DEAD;
        } else {
            EmotionalState state = agent.getEmotionalState();
            agentColor = switch (state) {
                case CALM -> AGENT_CALM;
                case SELFISH -> AGENT_SELFISH;
                case PANICKING -> AGENT_PANICKING;
            };
        }

        // 4. Execute final canvas stroke commands
        gc.setFill(agentColor);
        gc.fillOval(ax - visualRadius, ay - visualRadius, visualDiameter, visualDiameter);
        gc.setStroke(Color.web("#121212"));
        gc.setLineWidth(1);
        gc.strokeOval(ax - visualRadius, ay - visualRadius, visualDiameter, visualDiameter);
    }

    /**
     * Helper layout component to generate backing selections behind circular
     * profiles.
     */
    private void drawCircularHalo(double centerX, double centerY, double baseRadius, double padding) {
        double totalRadius = baseRadius + padding;
        gc.setFill(SELECTED_COLOR.deriveColor(0, 1, 1, 0.4));
        gc.fillOval(centerX - totalRadius, centerY - totalRadius, totalRadius * 2, totalRadius * 2);
        gc.setStroke(SELECTED_COLOR);
        gc.setLineWidth(Math.max(2.0, padding / 2.0));
        gc.strokeOval(centerX - totalRadius, centerY - totalRadius, totalRadius * 2, totalRadius * 2);
    }

    /**
     * Helper layout component to generate selection backings behind linear vectors.
     */
    private void drawLineHalo(double startX, double startY, double endX, double endY, double baseWidth,
            double padding, Color color) {
        gc.setStroke(color.deriveColor(0, 1, 1, 0.6));
        gc.setLineWidth(baseWidth + (padding * 2));
        gc.setLineCap(javafx.scene.shape.StrokeLineCap.BUTT);
        gc.strokeLine(startX, startY, endX, endY);
    }

    /**
     * Draws a dynamically scaled visual highlight ring around an agent's current
     * destination node.
     */
    private void drawTargetNodeHighlight(GraphicsContext gc, Node node) {
        gc.save();
        gc.setStroke(TARGET_HIGHLIGHT_COLOR);
        gc.setLineWidth(3.0);

        // Dynamically adapts scale relative to the targets actual layout parameters
        double highlightRadius = getNodeVisualRadius(node) + 5.0;

        gc.strokeOval(
                node.getX() - highlightRadius,
                node.getY() - highlightRadius,
                highlightRadius * 2,
                highlightRadius * 2);
        gc.restore();
    }

    /**
     * Renders a hollow frame profiling only the exterior boundaries of the
     * remaining path segment.
     */
    private void drawEdgeTargetHighlight(GraphicsContext gc, Edge edge, Agent agent, double progress) {
        Node targetNode = agent.getCurrentNodeOrNextNodeIfOnEdge();
        if (targetNode == null)
            return;

        Node departureNode = edge.getOppositeNode(targetNode);
        if (departureNode == null)
            return;

        double depX = departureNode.getX(), depY = departureNode.getY();
        double destX = targetNode.getX(), destY = targetNode.getY();

        // Trace position via linear interpolations
        double currentX = depX + (destX - depX) * progress;
        double currentY = depY + (destY - depY) * progress;

        double dx = destX - currentX;
        double dy = destY - currentY;
        double distanceRemaining = Math.hypot(dx, dy);
        double targetRadius = getNodeVisualRadius(targetNode);

        // Only draw if agent is positioned clear of the perimeter boundaries
        if (distanceRemaining > targetRadius) {
            double nx = -dy / distanceRemaining;
            double ny = dx / distanceRemaining;

            // Adjust target vectors to dock perfectly flush on the perimeter border
            double adjDestX = destX - (dx / distanceRemaining) * targetRadius;
            double adjDestY = destY - (dy / distanceRemaining) * targetRadius;
            double visualWidth = getEdgeVisualWidth(edge);

            gc.save();
            gc.setStroke(TARGET_HIGHLIGHT_COLOR);
            gc.setLineWidth(2.0);
            gc.setLineCap(javafx.scene.shape.StrokeLineCap.BUTT);

            double outerOffset = visualWidth / 2.0;

            // Generate twin left/right tracking segment boundaries
            double startLeftX = currentX + nx * outerOffset, startLeftY = currentY + ny * outerOffset;
            double endLeftX = adjDestX + nx * outerOffset, endLeftY = adjDestY + ny * outerOffset;

            double startRightX = currentX - nx * outerOffset, startRightY = currentY - ny * outerOffset;
            double endRightX = adjDestX - nx * outerOffset, endRightY = adjDestY - ny * outerOffset;

            // Render hollow wireframe boundaries
            gc.strokeLine(startLeftX, startLeftY, endLeftX, endLeftY);
            gc.strokeLine(startRightX, startRightY, endRightX, endRightY);
            gc.strokeLine(startLeftX, startLeftY, startRightX, startRightY); // Closing cap behind agent

            gc.restore();
        }
    }
}