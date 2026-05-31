package fr.cy.view;

import fr.cy.model.agent.Agent;
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
    private static final Color BG_COLOR        = Color.web("#121212");
    private static final Color EDGE_COLOR      = Color.web("#5A5A8A");
    private static final Color FIRE_COLOR      = Color.web("#FF5722");
    private static final Color CALM_NODE_COLOR = Color.web("#007ACC");
    private static final Color STRESS_COLOR    = Color.web("#D32F2F");
    private static final Color AGENT_COLOR     = Color.web("#F5F5F5");

    // Taille fixe des nœuds (en pixels dans les coordonnées du monde)
    private static final double NODE_RADIUS    = 22.0;
    // Épaisseur minimale des arêtes
    private static final double EDGE_MIN_WIDTH = 4.0;

    public GraphRenderer(GraphicsContext gc) {
        this.gc = gc;
    }

    /**
     * Point d'entrée du dessin appelé chaque tick par le SimulationController.
     */
    public void render(Simulation simulation, GraphCanvas canvas) {
        double width = canvas.getWidth();
        double height = canvas.getHeight();

        // 1. Nettoyage TOTAL de l'écran en effaçant les transformations précédentes
        gc.save();
        gc.setTransform(1, 0, 0, 1, 0, 0); // Réinitialisation de la matrice (Identité)
        gc.setFill(BG_COLOR);
        gc.fillRect(0, 0, width, height);
        gc.restore(); // Retour au contexte initial

        if (simulation == null)
            return;

        Graph graph = simulation.getGraph();
        if (graph == null)
            return;

        // 2. Application de la transformation Caméra (Pan & Zoom)
        gc.save();
        gc.translate(canvas.getPanX(), canvas.getPanY());
        gc.scale(canvas.getZoom(), canvas.getZoom());

        // 3. Dessiner les arêtes (Edges)
        for (Edge edge : graph.getEdges()) {
            drawEdge(edge);
        }

        // 4. Dessiner les noeuds (Nodes)
        for (Node node : graph.getNodes()) {
            drawNode(node);
        }

        // 5. Dessiner les agents
        if (simulation.getAgentManager() != null) {
            for (Agent agent : simulation.getAgentManager().getAgents()) {
                drawAgent(agent);
            }
        }

        gc.restore(); // Nettoie la matrice de caméra pour la frame suivante !
    }

    private void drawEdge(Edge edge) {
        Node start = edge.getStart();
        Node end = edge.getEnd();

        double sx = start.getX();
        double sy = start.getY();
        double ex = end.getX();
        double ey = end.getY();

        // Arête de base — épaisseur basée sur la largeur du couloir, minimum EDGE_MIN_WIDTH
        gc.setStroke(EDGE_COLOR);
        gc.setLineWidth(Math.max(EDGE_MIN_WIDTH, edge.getWidth()));
        gc.strokeLine(sx, sy, ex, ey);

        // Overlay de feu en fonction du pourcentage/distance brulée
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

        // Calcul de la couleur selon le Stress Actuel
        double stressLvl = node.getStressInducingImpact();
        Color nodeColor = CALM_NODE_COLOR.interpolate(STRESS_COLOR, stressLvl);

        // Si le noeud est en feu, l'animation et l'aura rouge dominent
        if (node.isOnFire()) {
            double intensity = node.getFire().getIntensity();
            nodeColor = FIRE_COLOR.interpolate(Color.web("#B71C1C"), intensity);

            // Effet visuel lueur (Glow)
            DropShadow glow = new DropShadow(15 * intensity, FIRE_COLOR);
            gc.setEffect(glow);
        } else {
            gc.setEffect(null);
        }

        gc.setFill(nodeColor);
        gc.fillOval(x, y, radius, radius);

        // Bordure épurée
        gc.setStroke(Color.web("#2A2A35"));
        gc.setLineWidth(2);
        gc.strokeOval(x, y, radius, radius);

        // Reset effet
        gc.setEffect(null);
    }

    private void drawAgent(Agent agent) {
        double ax, ay;

        // L'agent est-il sur un Noeud ou une Arête ?
        if (agent.isOnNode() && agent.getCurrentNode() != null) {
            Node node = agent.getCurrentNode();

            // Jitter déterministe pour éviter le chevauchement parfait visuel
            double maxOffset = NODE_RADIUS * 0.4;
            double angle = (agent.getId() * 137.508) % 360;
            // On calcule une distance pour qu'ils remplissent un peu le nœud
            double dist = (agent.getId() * 11.3) % maxOffset;

            ax = node.getX() + Math.cos(Math.toRadians(angle)) * dist;
            ay = node.getY() + Math.sin(Math.toRadians(angle)) * dist;
        } else if (!agent.isOnNode() && agent.getCurrentEdge() != null) {
            Edge edge = agent.getCurrentEdge();
            Node previous = agent.getPreviousOrCurrentNode();

            // On détermine le chemin en fonction du Noeud de provencance (direction)
            Node target = edge.getOppositeNode(previous);
            double ratio = Math.max(0, agent.getTravelProgressPercentageOnEdge());

            // Interpolation linéaire entre les deux noeuds
            double baseX = previous.getX() + (target.getX() - previous.getX()) * ratio;
            double baseY = previous.getY() + (target.getY() - previous.getY()) * ratio;

            // Optionnel : ajouter un léger jitter sur l'arête aussi pour voir la densité
            double angle = (agent.getId() * 137.508) % 360;
            double maxEdgeOffset = Math.max(2, edge.getWidth() / 2 - 2);
            double dist = (agent.getId() * 7.1) % maxEdgeOffset;

            ax = baseX + Math.cos(Math.toRadians(angle)) * dist;
            ay = baseY + Math.sin(Math.toRadians(angle)) * dist;
        } else {
            return;
        }

        // Dessin final de l'Agent
        double agentRadius = 6;
        gc.setFill(AGENT_COLOR);
        gc.fillOval(ax - agentRadius / 2, ay - agentRadius / 2, agentRadius, agentRadius);

        // Bordure sombre pour bien les discerner
        gc.setStroke(Color.web("#121212"));
        gc.setLineWidth(1);
        gc.strokeOval(ax - agentRadius / 2, ay - agentRadius / 2, agentRadius, agentRadius);
    }
}
