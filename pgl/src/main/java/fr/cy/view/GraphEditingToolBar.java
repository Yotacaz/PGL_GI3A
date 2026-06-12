package fr.cy.view;

import java.util.function.Consumer;

import fr.cy.controller.CanvasInteractionController;
import fr.cy.controller.SimulationController;
import javafx.scene.control.Separator;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Button;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

/**
 * Toolbar d'édition du graphe : modes d'interaction à gauche,
 * {@link FileManagementBar} poussée à droite via un spacer flexible.
 */
public class GraphEditingToolBar extends ToolBar {

    private final SimulationController simController;

    /** Callback invoked when the user changes the interaction mode. */
    private Consumer<CanvasInteractionController.InteractionMode> onModeChange;

    /** Callback invoked when the user requests random graph generation. */
    private Runnable onGenerateRandom;

    /** Callback invoked when the user requests random agent population. */
    private Runnable onGenerateRandomAgents;

    public GraphEditingToolBar(SimulationController simController) {
        this.simController = simController;
        this.getStyleClass().add("custom-toolbar");
        initToolBar();
    }

    private void initToolBar() {
        // ── Editing mode buttons (left side) ────────────────────────────────────
        ToggleButton selectBtn   = new ToggleButton("🖱️ Sélection");
        ToggleButton addNodeBtn  = new ToggleButton("🔵 + Nœud");
        ToggleButton addEdgeBtn  = new ToggleButton("➖ + Arête");
        ToggleButton addAgentBtn = new ToggleButton("🚶 + Agents");
        ToggleButton deleteBtn   = new ToggleButton("🗑️ Suppression");

        selectBtn.getStyleClass().add("action-btn");
        addNodeBtn.getStyleClass().add("action-btn");
        addEdgeBtn.getStyleClass().add("action-btn");
        addAgentBtn.getStyleClass().add("action-btn");
        deleteBtn.getStyleClass().add("action-btn");

        ToggleGroup modeGroup = new ToggleGroup();
        selectBtn.setToggleGroup(modeGroup);
        addNodeBtn.setToggleGroup(modeGroup);
        addEdgeBtn.setToggleGroup(modeGroup);
        addAgentBtn.setToggleGroup(modeGroup);
        deleteBtn.setToggleGroup(modeGroup);
        selectBtn.setSelected(true);

        modeGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null) { oldVal.setSelected(true); return; }
            if (onModeChange != null) {
                if (newVal == selectBtn)
                    onModeChange.accept(CanvasInteractionController.InteractionMode.SELECT_AND_DRAG);
                else if (newVal == addNodeBtn)
                    onModeChange.accept(CanvasInteractionController.InteractionMode.ADD_NODE);
                else if (newVal == addEdgeBtn)
                    onModeChange.accept(CanvasInteractionController.InteractionMode.ADD_EDGE_START);
                else if (newVal == addAgentBtn)
                    onModeChange.accept(CanvasInteractionController.InteractionMode.ADD_AGENT);
                else if (newVal == deleteBtn)
                    onModeChange.accept(CanvasInteractionController.InteractionMode.DELETE);
            }
        });

        Button randomGenBtn = new Button("🎲 Génération Aléatoire");
        randomGenBtn.getStyleClass().add("action-btn");
        randomGenBtn.setOnAction(e -> { if (onGenerateRandom != null) onGenerateRandom.run(); });

        Button randomAgentsBtn = new Button("🎲 Agents Auto");
        randomAgentsBtn.getStyleClass().add("action-btn");
        randomAgentsBtn.setOnAction(e -> { if (onGenerateRandomAgents != null) onGenerateRandomAgents.run(); });

        // ── Flexible spacer — pushes the file buttons to the far right ──────────
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // ── File management (right side) ─────────────────────────────────────────
        FileManagementBar fileBar = new FileManagementBar(simController);

        this.getItems().addAll(
                selectBtn, deleteBtn, addNodeBtn, addEdgeBtn, addAgentBtn,
                new Separator(),
                randomGenBtn, randomAgentsBtn,
                spacer,
                new Separator(),
                fileBar);
    }

    // ── Setters for callbacks ─────────────────────────────────────────────────

    public void setOnModeChange(Consumer<CanvasInteractionController.InteractionMode> listener) {
        this.onModeChange = listener;
    }

    public void setOnGenerateRandom(Runnable listener) {
        this.onGenerateRandom = listener;
    }

    public void setOnGenerateRandomAgents(Runnable listener) {
        this.onGenerateRandomAgents = listener;
    }
}
