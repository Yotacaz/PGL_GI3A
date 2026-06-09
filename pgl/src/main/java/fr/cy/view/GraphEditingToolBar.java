package fr.cy.view;

import java.util.function.Consumer;
import fr.cy.controller.CanvasInteractionController;
import javafx.scene.control.Button;
import javafx.scene.control.Separator;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.ToolBar;

/**
 * The {@code GraphEditingToolBar} class provides a toolbar for manipulating the
 * graph
 * and simulation entities. It allows users to toggle between different
 * interaction modes
 * (selecting, adding nodes/edges/agents) and triggers generation processes.
 */
public class GraphEditingToolBar extends ToolBar {

    /** Callback invoked when the user changes the interaction mode. */
    private Consumer<CanvasInteractionController.InteractionMode> onModeChange;

    /** Callback invoked when the user requests random graph generation. */
    private Runnable onGenerateRandom;

    /** Callback invoked when the user requests random agent population. */
    private Runnable onGenerateRandomAgents;

    /**
     * Sets the listener for random agent generation.
     * * @param listener The runnable to execute when generating agents.
     */
    public void setOnGenerateRandomAgents(Runnable listener) {
        this.onGenerateRandomAgents = listener;
    }

    /**
     * Constructs the {@code GraphEditingToolBar} and initializes the UI components.
     */
    public GraphEditingToolBar() {
        this.getStyleClass().add("custom-toolbar");
        initToolBar();
    }

    /**
     * Initializes the toolbar UI, groups the toggle buttons, and sets up
     * the event handlers for interaction modes and generation triggers.
     */
    private void initToolBar() {
        ToggleButton selectBtn = new ToggleButton("🖱️ Sélection");
        ToggleButton addNodeBtn = new ToggleButton("🔵 + Nœud");
        ToggleButton addEdgeBtn = new ToggleButton("➖ + Arête");

        selectBtn.getStyleClass().add("action-btn");
        addNodeBtn.getStyleClass().add("action-btn");
        addEdgeBtn.getStyleClass().add("action-btn");

        ToggleGroup modeGroup = new ToggleGroup();
        selectBtn.setToggleGroup(modeGroup);
        addNodeBtn.setToggleGroup(modeGroup);
        addEdgeBtn.setToggleGroup(modeGroup);

        selectBtn.setSelected(true);

        ToggleButton addAgentBtn = new ToggleButton("🚶+ Agents");
        addAgentBtn.getStyleClass().add("action-btn");
        addAgentBtn.setToggleGroup(modeGroup);

        modeGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null) {
                oldVal.setSelected(true);
                return;
            }
            if (onModeChange != null) {
                if (newVal == selectBtn) {
                    onModeChange.accept(CanvasInteractionController.InteractionMode.SELECT_AND_DRAG);
                } else if (newVal == addNodeBtn) {
                    onModeChange.accept(CanvasInteractionController.InteractionMode.ADD_NODE);
                } else if (newVal == addEdgeBtn) {
                    onModeChange.accept(CanvasInteractionController.InteractionMode.ADD_EDGE_START);
                } else if (newVal == addAgentBtn) {
                    onModeChange.accept(CanvasInteractionController.InteractionMode.ADD_AGENT);
                }
            }
        });

        Button randomGenBtn = new Button("🎲 Génération Aléatoire");
        randomGenBtn.getStyleClass().addAll("action-btn");

        randomGenBtn.setOnAction(event -> {
            if (onGenerateRandom != null) {
                onGenerateRandom.run();
            }
        });

        Button randomAgentsBtn = new Button("🎲 Agents Auto");
        randomAgentsBtn.getStyleClass().addAll("action-btn");
        randomAgentsBtn.setOnAction(event -> {
            if (onGenerateRandomAgents != null)
                onGenerateRandomAgents.run();
        });

        this.getItems().addAll(
                selectBtn, addNodeBtn, addEdgeBtn, addAgentBtn,
                new Separator(),
                randomGenBtn, randomAgentsBtn);
    }

    /**
     * Sets the listener for interaction mode changes.
     * * @param listener A consumer function that receives the new
     * {@link CanvasInteractionController.InteractionMode}.
     */
    public void setOnModeChange(Consumer<CanvasInteractionController.InteractionMode> listener) {
        this.onModeChange = listener;
    }

    /**
     * Sets the listener for random graph generation.
     * * @param listener The runnable to execute when generating a random graph.
     */
    public void setOnGenerateRandom(Runnable listener) {
        this.onGenerateRandom = listener;
    }
}