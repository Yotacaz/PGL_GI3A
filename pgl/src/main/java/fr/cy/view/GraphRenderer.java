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
 * The {@code GraphRenderer} class is responsible for the graphical rendering
 * of the simulation world, including the graph structure (nodes/edges) and
 * agents.
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

    private static final Color SELECTED_COLOR = Color.web("#00FFFF");

    private static final double NODE_RADIUS = 22.0;
    private static final double PIXELS_PER_UNIT = 5.0;
    private static final double EDGE_MIN_WIDTH = 4.0;

    /**
     * Constructs the renderer with the specified {@link GraphicsContext}.
     * * @param gc The JavaFX GraphicsContext used for all drawing operations.
     */
    public GraphRenderer(GraphicsContext gc) {
        this.gc = gc;
    }

    /**
     * Renders the entire simulation state onto the provided canvas.
     * * @param simulation The simulation instance containing the graph and agents.
     * 
     * @param canvas The canvas component providing view coordinates (pan/zoom).
     */
    public void render(Simulation simulation, GraphCanvas canvas) {
        double width = canvas.getWidth();
        double height = canvas.getHeight();

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

        gc.save();
        gc.translate(canvas.getPanX(), canvas.getPanY());
        gc.scale(canvas.getZoom(), canvas.getZoom());

        Object selectedEntity = canvas.getSelectedEntity();

        for (Edge edge : graph.getEdges()) {
            drawEdge(edge, selectedEntity);
        }

        for (Node node : graph.getNodes()) {
            drawNode(node, selectedEntity);
        }

        if (simulation.getAgentManager() != null) {
            for (Agent agent : simulation.getAgentManager().getAgentsToEvacuate()) {
                drawAgent(agent, selectedEntity instanceof Agent ? (Agent) selectedEntity : null);
            }
        }

        gc.restore();
    }

    /**
     * Calculates the dynamic visual radius of a node, accounting for its
     * capacity and the width of connected edges.
     * * @param node The node to calculate the radius for.
     * 
     * @return The calculated radius in pixels.
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
     * Draws an edge, including its body, potential congestion colors,
     * direction markers, and fire effects.
     * * @param edge The edge to render.
     * 
     * @param selectedEntity The currently selected entity, for highlighting.
     */
    private void drawEdge(Edge edge, Object selectedEntity) {
        boolean isSelected = edge.equals(selectedEntity);
        Node start = edge.getStart();
        Node end = edge.getEnd();

        double sx = start.getX();
        double sy = start.getY();
        double ex = end.getX();
        double ey = end.getY();

        double dx = ex - sx;
        double dy = ey - sy;
        double centerDistance = Math.sqrt(dx * dx + dy * dy);

        double startRadius = getNodeVisualRadius(start);
        double endRadius = getNodeVisualRadius(end);

        double overlap = 3.0;
        startRadius = Math.max(0, startRadius - overlap);
        endRadius = Math.max(0, endRadius - overlap);

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
        double visualLength = Math.sqrt(visDx * visDx + visDy * visDy);
        double visualWidth = Math.max(EDGE_MIN_WIDTH, edge.getWidth() * PIXELS_PER_UNIT);

        if (isSelected) {
            drawLineHalo(borderStartX, borderStartY, borderEndX, borderEndY, visualWidth, 4.0);
        }

        double congestionRatio = 0.0;
        if (edge.getCapacity() > 0) {
            congestionRatio = Math.min(1.0, (double) edge.getAgents().size() / edge.getCapacity());
        }

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

        // Fire rendering
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
     * Draws a node, handling selection, state-based colors (fire/exit/stress),
     * and visual glow effects.
     * * @param node The node to render.
     * 
     * @param selectedEntity The currently selected entity.
     */
    private void drawNode(Node node, Object selectedEntity) {
        boolean isSelected = node.equals(selectedEntity);
        double radius = getNodeVisualRadius(node);
        double x = node.getX(), y = node.getY();

        if (isSelected)
            drawCircularHalo(x, y, radius, 6.0);

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
     * Draws an agent, calculating its position either on a node or interpolated
     * along an edge, with color reflecting its emotional state.
     * * @param agent The agent to render.
     * 
     * @param selectedEntity The currently selected entity.
     */
    private void drawAgent(Agent agent, Object selectedEntity) {
        double ax, ay;
        double visualRadius = Math.sqrt(agent.getSurfaceAreaTakenByAgent() / Math.PI) * PIXELS_PER_UNIT;
        double visualDiameter = visualRadius * 2.0;

        if (agent.isOnNode()) {
            Node node = agent.getCurrentNode();
            double maxOffset = Math.max(0, getNodeVisualRadius(node) - visualRadius);
            double angle = agent.getId() * 137.508;
            double dist = Math.sqrt((agent.getId() * 11.3) % 100 / 100.0) * maxOffset;
            ax = node.getX() + Math.cos(Math.toRadians(angle)) * dist;
            ay = node.getY() + Math.sin(Math.toRadians(angle)) * dist;
        } else if (agent.isOnEdge()) {
            Edge edge = agent.getCurrentOrPreviousEdge();
            if (agent.getCurrentNodeOrNextNodeIfOnEdge()==null){
                System.err.println("Agent " + agent.getId() + " is on edge " + edge.getId() + " but has no valid current or next node");
            }
            Node target = Objects.requireNonNull(agent.getCurrentNodeOrNextNodeIfOnEdge());
            Node previous = Objects.requireNonNull(edge.getOppositeNode(target));

            double ratio = Math.max(0, agent.getCurrentEdgeProgress());
            ax = previous.getX() + (target.getX() - previous.getX()) * ratio;
            ay = previous.getY() + (target.getY() - previous.getY()) * ratio;
        }else{
            return; //not on graph
        }

        if (agent.equals(selectedEntity))
            drawCircularHalo(ax, ay, visualRadius, 3.0);

        EmotionalState state = agent.getEmotionalState();
        Color agentColor = switch (state) {
            case CALM -> AGENT_CALM;
            case SELFISH -> AGENT_SELFISH;
            case PANICKING -> AGENT_PANICKING;
        };

        gc.setFill(agentColor);
        gc.fillOval(ax - visualRadius, ay - visualRadius, visualDiameter, visualDiameter);
        gc.setStroke(Color.web("#121212"));
        gc.setLineWidth(1);
        gc.strokeOval(ax - visualRadius, ay - visualRadius, visualDiameter, visualDiameter);
    }

    /**
     * Helper to draw a selection halo around circular entities (nodes/agents).
     * * @param centerX The center X coordinate.
     * 
     * @param centerY    The center Y coordinate.
     * @param baseRadius The radius of the object.
     * @param padding    The thickness/padding of the halo.
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
     * Helper to draw a selection halo around linear entities (edges).
     * * @param startX Start X coordinate.
     * 
     * @param startY    Start Y coordinate.
     * @param endX      End X coordinate.
     * @param endY      End Y coordinate.
     * @param baseWidth The original width of the edge.
     * @param padding   The extra width to add for the halo effect.
     */
    private void drawLineHalo(double startX, double startY, double endX, double endY, double baseWidth,
            double padding) {
        gc.setStroke(SELECTED_COLOR.deriveColor(0, 1, 1, 0.6));
        gc.setLineWidth(baseWidth + (padding * 2));
        gc.setLineCap(javafx.scene.shape.StrokeLineCap.BUTT);
        gc.strokeLine(startX, startY, endX, endY);
    }
}