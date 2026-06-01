package fr.cy.view;

import javafx.scene.canvas.Canvas;
import fr.cy.model.agent.Agent;

/**
 * Surface de dessin personnalisée qui épouse dynamiquement
 * la taille de son conteneur parent (responsive rendering).
 * Gère également les propriétés de la "Caméra" (Pan & Zoom).
 */
public class GraphCanvas extends Canvas {

    // Propriétés de transformation de caméra (Viewport)
    private double panX = 0.0;
    private double panY = 0.0;
    private double zoom = 1.0;

    // --- NOUVEAU : Mémoire de la sélection UI ---
    private Agent selectedAgent = null;

    @Override
    public boolean isResizable() {
        return true;
    }

    @Override
    public double prefWidth(double height) {
        return getWidth();
    }

    @Override
    public double prefHeight(double width) {
        return getHeight();
    }

    // --- Getters & Setters Caméra ---
    public double getPanX() {
        return panX;
    }

    public void setPanX(double panX) {
        this.panX = panX;
    }

    public double getPanY() {
        return panY;
    }

    public void setPanY(double panY) {
        this.panY = panY;
    }

    public double getZoom() {
        return zoom;
    }

    public void setZoom(double zoom) {
        this.zoom = zoom;
    }

    // --- Getters & Setters Sélection ---
    public Agent getSelectedAgent() {
        return selectedAgent;
    }

    public void setSelectedAgent(Agent selectedAgent) {
        this.selectedAgent = selectedAgent;
    }
}