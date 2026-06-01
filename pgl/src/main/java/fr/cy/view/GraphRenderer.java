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
    // Épaisseur minimale des arêtes
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

        for (Edge edge : graph.getEdges()) {
            drawEdge(edge);
        }

        for (Node node : graph.getNodes()) {
            drawNode(node);
        }

        if (simulation.getAgentManager() != null) {
            // NOUVEAU : On récupère l'agent sélectionné pour le passer à la fonction de
            // dessin
            Agent selectedAgent = canvas.getSelectedAgent();
            for (Agent agent : simulation.getAgentManager().getAgentsOnGraph()) {
                drawAgent(agent, selectedAgent);
            }
        }

        gc.restore();
    }

    private void drawEdge(Edge edge) {
        Node start = edge.getStart();
        Node end = edge.getEnd();
        double sx = start.getX();
        double sy = start.getY();
        double ex = end.getX();
        double ey = end.getY();

        gc.setStroke(EDGE_COLOR);
        gc.setLineWidth(Math.max(EDGE_MIN_WIDTH, edge.getWidth()));
        gc.strokeLine(sx, sy, ex, ey);

        if (edge.isOnFire() && (edge.isBurningFromStart() || edge.isBurningFromEnd())) {
            double ratio = Math.min(1.0, edge.getBurnedDistance() / edge.getLength());
            gc.setStroke(FIRE_COLOR.deriveColor(0, 1, 1, 0.8));

            if (edge.isBurningFromStart()) {
                double burnX = sx + (ex - sx) * ratio;
                double burnY = sy + (ey - sy) * ratio;
                gc.strokeLine(sx, sy, burnX, burnY);
            }
            if (edge.isBurningFromEnd()) {
                double burnX = ex + (sx - ex) * ratio;
                double burnY = ey + (sy - ey) * ratio;
                gc.strokeLine(ex, ey, burnX, burnY);
            }
        }
    }

    private void drawNode(Node node) {
        double radius = NODE_RADIUS;
        double x = node.getX() - radius / 2;
        double y = node.getY() - radius / 2;

        Color nodeColor = node.isExit()
                ? EXIT_NODE_COLOR
                : CALM_NODE_COLOR.interpolate(STRESS_COLOR, node.getStressInducingImpact());

        if (node.isOnFire()) {
            double intensity = node.getFire().getIntensity();
            nodeColor = FIRE_COLOR.interpolate(Color.web("#B71C1C"), intensity);
            DropShadow glow = new DropShadow(15 * intensity, FIRE_COLOR);
            gc.setEffect(glow);
        } else {
            gc.setEffect(null);
        }

        gc.setFill(nodeColor);
        gc.fillOval(x, y, radius, radius);
        gc.setStroke(Color.web("#2A2A35"));
        gc.setLineWidth(2);
        gc.strokeOval(x, y, radius, radius);
        gc.setEffect(null);
    }

    private void drawAgent(Agent agent, Agent selectedAgent) {
        double ax, ay;

        if (agent.isOnNode() && agent.getCurrentNode() != null) {
            Node node = agent.getCurrentNode();
            double maxOffset = NODE_RADIUS * 0.4;
            double angle = (agent.getId() * 137.508) % 360;
            double dist = (agent.getId() * 11.3) % maxOffset;
            ax = node.getX() + Math.cos(Math.toRadians(angle)) * dist;
            ay = node.getY() + Math.sin(Math.toRadians(angle)) * dist;
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
        } else {
            return;
        }

        double agentRadius = 6;

        // --- NOUVEAU : Effet visuel si l'agent est sélectionné ---
        if (agent.equals(selectedAgent)) {
            gc.setStroke(SELECTED_COLOR);
            gc.setLineWidth(2.5);
            // On dessine un anneau un peu plus grand autour de l'agent
            gc.strokeOval(ax - agentRadius / 2 - 3, ay - agentRadius / 2 - 3, agentRadius + 6, agentRadius + 6);
        }

        EmotionalState state = agent.getEmotionalState();
        Color agentColor = switch (state) {
            case CALM -> AGENT_CALM;
            case SELFISH -> AGENT_SELFISH;
            case PANICKING -> AGENT_PANICKING;
        };

        gc.setFill(agentColor);
        gc.fillOval(ax - agentRadius / 2, ay - agentRadius / 2, agentRadius, agentRadius);
        gc.setStroke(Color.web("#121212"));
        gc.setLineWidth(1);
        gc.strokeOval(ax - agentRadius / 2, ay - agentRadius / 2, agentRadius, agentRadius);
    }
}