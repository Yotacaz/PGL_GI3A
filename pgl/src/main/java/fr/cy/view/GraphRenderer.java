package fr.cy.view;

import java.util.Objects;

import fr.cy.model.agent.Agent;
import fr.cy.model.agent.behaviour.properties.EmotionalState;
import fr.cy.model.graph.Graph;
import fr.cy.model.graph.element.Edge;
import fr.cy.model.graph.element.Node;
import fr.cy.model.simulation.Simulation;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;

/**
 * Moteur de rendu graphique. Dessine le réseau de graphe et les agents.
 */
public class GraphRenderer {

    private final GraphicsContext gc;

    // Palette de couleurs Modern Dark Theme
    private static final Color BG_COLOR = Color.web("#121212");

    private static final Color EDGE_COLOR = Color.web("#5A5A8A");
    private static final Color FIRE_COLOR = Color.web("#FF5722");
    private static final Color EDGE_CONGESTED_COLOR = Color.web("#E91E63");

    private static final Color CALM_NODE_COLOR = Color.web("#007ACC");
    private static final Color EXIT_NODE_COLOR = Color.web("#2ECC71"); // vert = sortie
    private static final Color STRESS_COLOR = Color.web("#D32F2F");

    // Couleurs agents selon EmotionalState (système du groupe)
    private static final Color AGENT_CALM = Color.web("#4ADE80"); // vert = calme
    private static final Color AGENT_SELFISH = Color.web("#FF8C00"); // orange = égoïste
    private static final Color AGENT_PANICKING = Color.web("#FF2020"); // rouge = panique

    // Couleur de sélection
    private static final Color SELECTED_COLOR = Color.web("#00FFFF"); // Cyan fluo

    // Taille fixe des nœuds (en pixels dans les coordonnées du monde)
    private static final double NODE_RADIUS = 22.0;

    // 1 unité mathématique = 10 pixels à l'écran (Échelle)
    private static final double PIXELS_PER_UNIT = 5.0;

    // Épaisseur visuelle minimale pour qu'une arête très fine reste visible
    private static final double EDGE_MIN_WIDTH = 4.0;

    public GraphRenderer(GraphicsContext gc) {
        this.gc = gc;
    }

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

        // Les arêtes en premier : parfait pour cacher les coupes sous les nœuds !
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
     * Méthode utilitaire pour calculer le rayon visuel dynamique d'un nœud.
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

        double borderStartX = sx;
        double borderStartY = sy;
        double borderEndX = ex;
        double borderEndY = ey;

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
            // Padding de 4 pixels autour de l'arête
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

        // =========================================================
        // ÉTAPE 3 : CHEVRONS ET FEU (PAR-DESSUS LE CORPS)
        // =========================================================
        if (edge.isDirected() && visualLength > 0) {
            double angle = Math.atan2(visDy, visDx);
            double chevronSize = 8.0 + (visualWidth * 0.4);
            double spacing = 200.0;
            int numChevrons = (int) (visualLength / spacing);
            if (numChevrons < 1) {
                numChevrons = 1;
            }

            gc.setStroke(currentEdgeColor.brighter());
            gc.setLineWidth(Math.max(2.0, visualWidth * 0.15));
            gc.setLineJoin(javafx.scene.shape.StrokeLineJoin.ROUND);
            gc.setLineCap(javafx.scene.shape.StrokeLineCap.ROUND);

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

                gc.strokePolyline(
                        new double[] { leftX, tipX, rightX },
                        new double[] { leftY, tipY, rightY },
                        3);
            }
        }

        if (edge.isOnFire() && (edge.isBurningFromStart() || edge.isBurningFromEnd()) && visualLength > 0) {
            double ratio = Math.min(1.0, edge.getBurnedDistance() / edge.getLength());

            gc.setLineWidth(visualWidth); // On applique la largeur de l'arête au feu
            gc.setLineCap(javafx.scene.shape.StrokeLineCap.BUTT);

            gc.setStroke(FIRE_COLOR.deriveColor(0, 1, 1, 0.8));

            if (edge.isBurningFromStart()) {
                double burnX = borderStartX + visDx * ratio;
                double burnY = borderStartY + visDy * ratio;
                gc.strokeLine(borderStartX, borderStartY, burnX, burnY);
            }
            if (edge.isBurningFromEnd()) {
                double burnX = borderEndX - visDx * ratio;
                double burnY = borderEndY - visDy * ratio;
                gc.strokeLine(borderEndX, borderEndY, burnX, burnY);
            }
        }
    }

    private void drawNode(Node node, Object selectedEntity) {
        boolean isSelected = node.equals(selectedEntity);
        double radius = getNodeVisualRadius(node);
        double x = node.getX();
        double y = node.getY();

        // 1. Dessiner le halo de sélection si nécessaire
        if (isSelected) {
            drawCircularHalo(x, y, radius, 6.0);
        }

        // 2. Déterminer la couleur de base
        Color nodeColor = CALM_NODE_COLOR.interpolate(STRESS_COLOR, node.getStressInducingImpact());
        if (node.isExit())
            nodeColor = EXIT_NODE_COLOR;
        if (node.isOnFire()) {
            double intensity = node.getFire().getIntensity();
            nodeColor = FIRE_COLOR.interpolate(Color.web("#B71C1C"), intensity);
        }

        // 3. Dessiner l'effet de Lueur (Glow) manuel
        // On dessine 3 cercles de plus en plus grands et transparents
        if (node.isExit() || node.isOnFire()) {
            double glowRadius = node.isOnFire() ? 15 * node.getFire().getIntensity() : 10;
            Color glowColor = node.isExit() ? EXIT_NODE_COLOR : FIRE_COLOR;

            for (int i = 1; i <= 3; i++) {
                gc.setFill(glowColor.deriveColor(0, 1, 1, 0.2 / i)); // Opacité qui diminue
                double pulse = 1.0 + 0.1 * Math.sin(System.currentTimeMillis() * 0.008);
                double r = (radius + (glowRadius * i / 3)) * pulse;
                gc.fillOval(x - r, y - r, r * 2, r * 2);
            }
        }

        // 4. Dessiner le nœud lui-même
        gc.setFill(nodeColor);
        gc.fillOval(x - radius, y - radius, radius * 2, radius * 2);

        // 5. Bordure
        gc.setStroke(Color.web("#2A2A35"));
        gc.setLineWidth(2);
        gc.strokeOval(x - radius, y - radius, radius * 2, radius * 2);
    }

    private void drawAgent(Agent agent, Object selectedEntity) {
        double ax, ay;

        // 1. Calcul de la taille de l'agent
        double visualRadius = Math.sqrt(agent.getSurfaceAreaTakenByAgent() / Math.PI) * PIXELS_PER_UNIT;
        double visualDiameter = visualRadius * 2.0;

        // 2. Calcul de la position (ax, ay)
        if (agent.isOnNode() && agent.getCurrentNode() != null) {
            Node node = agent.getCurrentNode();
            double maxOffset = Math.max(0, getNodeVisualRadius(node) - visualRadius);
            double angle = agent.getId() * 137.508;
            double randomRatio = (agent.getId() * 11.3) % 100 / 100.0;
            double dist = Math.sqrt(randomRatio) * maxOffset;

            ax = node.getX() + Math.cos(Math.toRadians(angle)) * dist;
            ay = node.getY() + Math.sin(Math.toRadians(angle)) * dist;

        } else if (agent.isOnGraph() && agent.getCurrentOrPreviousEdge() != null) {
            Edge edge = agent.getCurrentOrPreviousEdge();
            // Node previous = agent.getPreviousOrCurrentNode();
            Node target = Objects.requireNonNull(agent.getCurrentNodeOrNextNodeIfOnEdge());
            Node previous = Objects.requireNonNull(edge.getOppositeNode(target));

            // Logique de raccourcissement bord-à-bord (déjà implémentée précédemment)
            double startRadius = getNodeVisualRadius(previous);
            double endRadius = getNodeVisualRadius(target);
            double sx = previous.getX(), sy = previous.getY();
            double ex = target.getX(), ey = target.getY();
            double dx = ex - sx, dy = ey - sy;
            double centerDist = Math.sqrt(dx * dx + dy * dy);

            double borderStartX = sx, borderStartY = sy, borderEndX = ex, borderEndY = ey;
            if (centerDist > 0) {
                borderStartX = sx + (dx / centerDist) * startRadius;
                borderStartY = sy + (dy / centerDist) * startRadius;
                borderEndX = ex - (dx / centerDist) * endRadius;
                borderEndY = ey - (dy / centerDist) * endRadius;
            }

            double ratio = Math.max(0, agent.getCurrentEdgeProgress());
            double baseX = borderStartX + (borderEndX - borderStartX) * ratio;
            double baseY = borderStartY + (borderEndY - borderStartY) * ratio;

            double angle = agent.getId() * 137.508;
            double maxEdgeOffset = Math.max(0, (edge.getWidth() * PIXELS_PER_UNIT) / 2.0 - visualRadius);
            double randomEdgeRatio = ((agent.getId() * 7.1) % 100 / 50.0) - 1.0;
            double dist = randomEdgeRatio * maxEdgeOffset;

            ax = baseX + Math.cos(Math.toRadians(angle)) * dist;
            ay = baseY + Math.sin(Math.toRadians(angle)) * dist;
        } else {
            return;
        }

        if (agent.equals(selectedEntity)) {
            // Un petit padding de 3 pixels autour de l'agent
            drawCircularHalo(ax, ay, visualRadius, 3.0);
        }

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
     * Méthode générique pour le halo de sélection circulaire (Nœuds et Agents)
     */
    private void drawCircularHalo(double centerX, double centerY, double baseRadius, double padding) {
        double totalRadius = baseRadius + padding;
        double diameter = totalRadius * 2.0;
        double x = centerX - totalRadius;
        double y = centerY - totalRadius;

        gc.setFill(SELECTED_COLOR.deriveColor(0, 1, 1, 0.4)); // Fond transparent
        gc.fillOval(x, y, diameter, diameter);

        gc.setStroke(SELECTED_COLOR); // Bordure pure
        gc.setLineWidth(Math.max(2.0, padding / 2.0)); // Ajuste la bordure à la taille du halo
        gc.strokeOval(x, y, diameter, diameter);
    }

    /**
     * Méthode générique pour le halo de sélection linéaire (Arêtes)
     */
    private void drawLineHalo(double startX, double startY, double endX, double endY, double baseWidth,
            double padding) {
        gc.setStroke(SELECTED_COLOR.deriveColor(0, 1, 1, 0.6));
        gc.setLineWidth(baseWidth + (padding * 2)); // On ajoute le padding de chaque côté
        gc.setLineCap(javafx.scene.shape.StrokeLineCap.BUTT);
        gc.strokeLine(startX, startY, endX, endY);
    }
}