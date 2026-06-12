package fr.cy.view;

/**
 * Centralized utility class defining application-wide color hex codes.
 * Prevents magic strings and ensures theme consistency across dynamic JavaFX
 * updates.
 */
public final class ThemeConstants {

    /** Prevents instantiation of this utility class. */
    private ThemeConstants() {
    }

    // --- UI Dynamic States ---
    public static final String SUCCESS_GREEN = "#4ADE80";
    public static final String WARNING_ORANGE = "#FF5722";
    public static final String DANGER_RED = "#FF5252";
    public static final String NEUTRAL_TEXT = "#E0E0E0";

    // --- Graph Canvas Palette ---
    public static final String CANVAS_BG = "#121212";
    public static final String GRAPH_EDGE = "#5A5A8A";
    public static final String GRAPH_EDGE_CONGESTED = "#E91E63";
    public static final String GRAPH_NODE_CALM = "#007ACC";

    public static final String AGENT_SELFISH = "#FF8C00";
    public static final String AGENT_DEAD = "#7F8C8D";
    public static final String SELECTION_CYAN = "#00FFFF";
    public static final String TARGET_HIGHLIGHT = "#FFA500"; // Orange

}