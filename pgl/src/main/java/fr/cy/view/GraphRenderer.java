package fr.cy.view;

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

        // Les arêtes en premier : parfait pour cacher les coupes sous les nœuds !
        for (Edge edge : graph.getEdges()) {
            drawEdge(edge);
        }

        for (Node node : graph.getNodes()) {
            drawNode(node);
        }

        if (simulation.getAgentManager() != null) {
            Agent selectedAgent = canvas.getSelectedAgent();
            for (Agent agent : simulation.getAgentManager().getAgentsToEvacuate()) {
                drawAgent(agent, selectedAgent);
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

    private void drawEdge(Edge edge) {
        Node start = edge.getStart();
        Node end = edge.getEnd();

        // Coordonnées des centres mathématiques
        double sx = start.getX();
        double sy = start.getY();
        double ex = end.getX();
        double ey = end.getY();

        double dx = ex - sx;
        double dy = ey - sy;
        double centerDistance = Math.sqrt(dx * dx + dy * dy);

        // =========================================================
        // 1. CALCUL BORD-À-BORD AVEC CHEVAUCHEMENT (Overlap)
        // =========================================================
        double startRadius = getNodeVisualRadius(start);
        double endRadius = getNodeVisualRadius(end);

        // L'ASTUCE : On réduit virtuellement le rayon du nœud de 3 pixels.
        // Cela force l'arête à s'enfoncer légèrement sous le nœud, bouchant tout espace
        // visible !
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

        // =========================================================
        // 2. GESTION DE LA CONGESTION ET DE L'ÉCHELLE
        // =========================================================
        double congestionRatio = 0.0;
        if (edge.getCapacity() > 0) {
            congestionRatio = Math.min(1.0, (double) edge.getAgents().size() / edge.getCapacity());
        }

        Color currentEdgeColor = EDGE_COLOR.interpolate(EDGE_CONGESTED_COLOR, congestionRatio);
        double visualWidth = Math.max(EDGE_MIN_WIDTH, edge.getWidth() * PIXELS_PER_UNIT);

        gc.setStroke(currentEdgeColor);
        gc.setLineWidth(visualWidth);

        // Coupe nette (BUTT) car le débordement est géré par notre marge "overlap"
        gc.setLineCap(javafx.scene.shape.StrokeLineCap.BUTT);
        gc.strokeLine(borderStartX, borderStartY, borderEndX, borderEndY);

        // =========================================================
        // 3. DESSIN DE LA FLÈCHE / CHEVRONS
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

        // =========================================================
        // 4. GESTION DU FEU
        // =========================================================
        if (edge.isOnFire() && (edge.isBurningFromStart() || edge.isBurningFromEnd()) && visualLength > 0) {
            double ratio = Math.min(1.0, edge.getBurnedDistance() / edge.getLength());
            gc.setStroke(FIRE_COLOR.deriveColor(0, 1, 1, 0.8));
            gc.setLineCap(javafx.scene.shape.StrokeLineCap.BUTT);

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

    private void drawNode(Node node) {
        double radius = getNodeVisualRadius(node);
        double diameter = radius * 2.0;

        double x = node.getX() - radius;
        double y = node.getY() - radius;

        Color nodeColor = CALM_NODE_COLOR.interpolate(STRESS_COLOR, node.getStressInducingImpact());

        if (node.isExit()) {
            nodeColor = EXIT_NODE_COLOR;
            DropShadow glow = new DropShadow(1, FIRE_COLOR);
            gc.setEffect(glow);
        }

        if (node.isOnFire()) {
            double intensity = node.getFire().getIntensity();
            nodeColor = FIRE_COLOR.interpolate(Color.web("#B71C1C"), intensity);
            DropShadow glow = new DropShadow(15 * intensity, FIRE_COLOR);
            gc.setEffect(glow);
        } else {
            gc.setEffect(null);
        }

        gc.setFill(nodeColor);
        gc.fillOval(x, y, diameter, diameter);
        gc.setStroke(Color.web("#2A2A35"));
        gc.setLineWidth(2);
        gc.strokeOval(x, y, diameter, diameter);
        gc.setEffect(null);
    }

    private void drawAgent(Agent agent, Agent selectedAgent) {
        double ax, ay;

        // 1. On calcule la taille visuelle de l'agent EN PREMIER
        double visualRadius = Math.sqrt(agent.getSurfaceAreaTakenByAgent() / Math.PI) * PIXELS_PER_UNIT;
        double visualDiameter = visualRadius * 2.0;

        // 2. Calcul de la position
        if (agent.isOnNode() && agent.getCurrentNode() != null) {
            Node node = agent.getCurrentNode();

            // Le décalage max : le bord du nœud, moins la taille de l'agent pour ne pas
            // déborder
            double maxOffset = Math.max(0, getNodeVisualRadius(node) - visualRadius);

            // L'angle (Spiral de Vogel / Nombre d'or pour une répartition organique)
            double angle = agent.getId() * 137.508;

            // Pseudo-aléatoire entre 0.0 et 1.0
            double randomRatio = (agent.getId() * 11.3) % 100 / 100.0;

            // L'utilisation de Math.sqrt() répartit uniformément la foule sur toute la
            // surface !
            double dist = Math.sqrt(randomRatio) * maxOffset;

            ax = node.getX() + Math.cos(Math.toRadians(angle)) * dist;
            ay = node.getY() + Math.sin(Math.toRadians(angle)) * dist;

        } else if (!agent.isOnNode() && agent.getCurrentOrPreviousEdge() != null) {
            Edge edge = agent.getCurrentOrPreviousEdge();
            Node previous = agent.getPreviousOrCurrentNode();
            Node target = edge.getOppositeNode(previous);

            double ratio = Math.max(0, agent.getTravelProgressPercentageOnEdge());
            double baseX = previous.getX() + (target.getX() - previous.getX()) * ratio;
            double baseY = previous.getY() + (target.getY() - previous.getY()) * ratio;

            double angle = agent.getId() * 137.508;

            // Pareil pour le couloir : largeur du couloir moins le rayon de l'agent
            double maxEdgeOffset = Math.max(0, (edge.getWidth() * PIXELS_PER_UNIT) / 2.0 - visualRadius);

            // Pseudo-aléatoire entre -1.0 et 1.0 pour aller à gauche ou à droite dans le
            // couloir
            double randomEdgeRatio = ((agent.getId() * 7.1) % 100 / 50.0) - 1.0;
            double dist = randomEdgeRatio * maxEdgeOffset;

            ax = baseX + Math.cos(Math.toRadians(angle)) * dist;
            ay = baseY + Math.sin(Math.toRadians(angle)) * dist;
        } else {
            return;
        }

        // 3. Dessin de l'agent
        if (agent.equals(selectedAgent)) {
            gc.setStroke(SELECTED_COLOR);
            gc.setLineWidth(2.5);
            gc.strokeOval(ax - visualRadius - 2, ay - visualRadius - 2, visualDiameter + 4, visualDiameter + 4);
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
}