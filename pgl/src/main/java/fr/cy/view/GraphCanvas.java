package fr.cy.view;

import javafx.scene.canvas.Canvas;

/**
 * The {@code GraphCanvas} class is a custom JavaFX {@link Canvas} component
 * that adapts dynamically to its parent container's size.
 * <p>
 * It also manages the "Camera" viewport properties, specifically pan (offset)
 * and zoom levels, as well as maintaining the current UI selection state.
 * </p>
 */
public class GraphCanvas extends Canvas {

    /** The horizontal translation (pan) of the viewport. */
    private double panX = 0.0;

    /** The vertical translation (pan) of the viewport. */
    private double panY = 0.0;

    /** The current zoom scale factor. */
    private double zoom = 1.0;

    /** The currently selected entity (e.g., Node, Edge, or Agent). */
    private Object selectedEntity = null;

    /**
     * Indicates that this component is resizable.
     * 
     * @return {@code true} as the canvas adapts to its container.
     */
    @Override
    public boolean isResizable() {
        return true;
    }

    /**
     * Returns the preferred width, which is the current canvas width.
     * 
     * @param height The suggested height.
     * @return The canvas width.
     */
    @Override
    public double prefWidth(double height) {
        return getWidth();
    }

    /**
     * Returns the preferred height, which is the current canvas height.
     * 
     * @param width The suggested width.
     * @return The canvas height.
     */
    @Override
    public double prefHeight(double width) {
        return getHeight();
    }

    /**
     * Returns the current horizontal pan offset.
     * 
     * @return The X-axis pan value.
     */
    public double getPanX() {
        return panX;
    }

    /**
     * Sets the horizontal pan offset.
     * 
     * @param panX The new X-axis pan value.
     */
    public void setPanX(double panX) {
        this.panX = panX;
    }

    /**
     * Returns the current vertical pan offset.
     * 
     * @return The Y-axis pan value.
     */
    public double getPanY() {
        return panY;
    }

    /**
     * Sets the vertical pan offset.
     * 
     * @param panY The new Y-axis pan value.
     */
    public void setPanY(double panY) {
        this.panY = panY;
    }

    /**
     * Returns the current zoom level.
     * 
     * @return The zoom factor.
     */
    public double getZoom() {
        return zoom;
    }

    /**
     * Sets the zoom level.
     * 
     * @param zoom The new zoom factor.
     */
    public void setZoom(double zoom) {
        this.zoom = zoom;
    }

    /**
     * Returns the currently selected entity.
     * 
     * @return The selected {@link Object} (could be Node, Edge, or Agent).
     */
    public Object getSelectedEntity() {
        return this.selectedEntity;
    }

    /**
     * Updates the currently selected entity in the UI.
     * 
     * @param selectedEntity The entity to select, or {@code null} to clear the
     *                       selection.
     */
    public void setSelectedEntity(Object selectedEntity) {
        this.selectedEntity = selectedEntity;
    }
}