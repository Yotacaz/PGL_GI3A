package fr.cy.view;

import java.util.function.Consumer;

import fr.cy.controller.CanvasInteractionController;
import fr.cy.controller.SimulationController;
import javafx.css.PseudoClass;
import javafx.scene.control.Button;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Separator;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
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

    private Consumer<CanvasInteractionController.InteractionMode> onModeChange;
    private Runnable onGenerateRandom;
    private Runnable onGenerateRandomAgents;

    // PseudoClass partagé avec ToggleButton pour un rendu CSS identique
    private static final PseudoClass SELECTED = PseudoClass.getPseudoClass("selected");

    public GraphEditingToolBar(SimulationController simController) {
        this.simController = simController;
        this.getStyleClass().add("custom-toolbar");
        initToolBar();
    }

    private void initToolBar() {
        // ── Boutons de mode (gauche) ─────────────────────────────────────────────
        ToggleButton selectBtn = new ToggleButton("🖱️ Sélection");
        ToggleButton deleteBtn = new ToggleButton("🗑️ Suppression");

        selectBtn.getStyleClass().add("action-btn");
        deleteBtn.getStyleClass().add("action-btn");

        // Fantôme invisible : représente "un mode de création est actif" dans le groupe
        ToggleButton createPhantom = new ToggleButton();
        createPhantom.setVisible(false);
        createPhantom.setManaged(false);

        ToggleGroup modeGroup = new ToggleGroup();
        selectBtn.setToggleGroup(modeGroup);
        deleteBtn.setToggleGroup(modeGroup);
        createPhantom.setToggleGroup(modeGroup);
        selectBtn.setSelected(true);

        // ── Bouton dépliant "Créer" ──────────────────────────────────────────────
        MenuButton createBtn = new MenuButton("➕ Créer");
        createBtn.getStyleClass().add("action-btn");

        MenuItem addNodeItem  = new MenuItem("🔵 + Nœud");
        MenuItem addEdgeItem  = new MenuItem("➖ + Arête");
        MenuItem addAgentItem = new MenuItem("🚶 + Agents");
        createBtn.getItems().addAll(addNodeItem, addEdgeItem, addAgentItem);

        addNodeItem.setOnAction(e -> activateCreation(createBtn, createPhantom, "🔵 + Nœud",
                CanvasInteractionController.InteractionMode.ADD_NODE));
        addEdgeItem.setOnAction(e -> activateCreation(createBtn, createPhantom, "➖ + Arête",
                CanvasInteractionController.InteractionMode.ADD_EDGE_START));
        addAgentItem.setOnAction(e -> activateCreation(createBtn, createPhantom, "🚶 + Agents",
                CanvasInteractionController.InteractionMode.ADD_AGENT));

        // Quand on quitte le mode création, réinitialise le MenuButton
        modeGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null) { oldVal.setSelected(true); return; }

            boolean creationActive = newVal == createPhantom;
            createBtn.pseudoClassStateChanged(SELECTED, creationActive);
            if (!creationActive) {
                createBtn.setText("➕ Créer");
            }

            if (onModeChange != null) {
                if (newVal == selectBtn)
                    onModeChange.accept(CanvasInteractionController.InteractionMode.SELECT_AND_DRAG);
                else if (newVal == deleteBtn)
                    onModeChange.accept(CanvasInteractionController.InteractionMode.DELETE);
                // Les modes de création sont déjà transmis par activateCreation()
            }
        });

        // ── Boutons de génération ────────────────────────────────────────────────
        Button randomGenBtn = new Button("🎲 Génération Aléatoire");
        randomGenBtn.getStyleClass().add("action-btn");
        randomGenBtn.setOnAction(e -> { if (onGenerateRandom != null) onGenerateRandom.run(); });

        Button randomAgentsBtn = new Button("🎲 Agents Auto");
        randomAgentsBtn.getStyleClass().add("action-btn");
        randomAgentsBtn.setOnAction(e -> { if (onGenerateRandomAgents != null) onGenerateRandomAgents.run(); });

        // ── Spacer + barre de fichiers (droite) ──────────────────────────────────
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        FileManagementBar fileBar = new FileManagementBar(simController);

        this.getItems().addAll(
                selectBtn, deleteBtn, createBtn, createPhantom,
                new Separator(),
                randomGenBtn, randomAgentsBtn,
                spacer,
                new Separator(),
                fileBar);
    }

    private void activateCreation(MenuButton createBtn, ToggleButton createPhantom,
                                  String label, CanvasInteractionController.InteractionMode mode) {
        createBtn.setText(label);
        createPhantom.setSelected(true); // déclenche le listener → SELECTED pseudo-class
        if (onModeChange != null) onModeChange.accept(mode);
    }

    // ── Setters pour les callbacks ────────────────────────────────────────────

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
