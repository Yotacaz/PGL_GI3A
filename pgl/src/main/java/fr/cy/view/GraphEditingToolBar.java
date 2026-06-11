package fr.cy.view;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import fr.cy.controller.CanvasInteractionController;
import fr.cy.controller.SimulationController;
import fr.cy.model.simulation.Simulation;
import fr.cy.model.simulation.SimulationSettings;
import fr.cy.util.FileManager;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

/**
 * The {@code GraphEditingToolBar} class provides a toolbar for manipulating the
 * graph and simulation entities, and for managing simulation files.
 * <p>
 * Editing mode buttons are aligned to the left; file management buttons
 * (New, Load, Save, Save As) are pushed to the far right via a flexible spacer.
 * </p>
 */
public class GraphEditingToolBar extends ToolBar {

    private final SimulationController simController;

    /** Callback invoked when the user changes the interaction mode. */
    private Consumer<CanvasInteractionController.InteractionMode> onModeChange;

    /** Callback invoked when the user requests random graph generation. */
    private Runnable onGenerateRandom;

    /** Callback invoked when the user requests random agent population. */
    private Runnable onGenerateRandomAgents;

    /**
     * Constructs the {@code GraphEditingToolBar}.
     *
     * @param simController The simulation controller used by file-management dialogs.
     */
    public GraphEditingToolBar(SimulationController simController) {
        this.simController = simController;
        this.getStyleClass().add("custom-toolbar");
        initToolBar();
    }

    /**
     * Initializes the toolbar UI, groups the toggle buttons, and sets up
     * the event handlers for interaction modes, generation triggers, and
     * file management actions.
     */
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

        // ── File management buttons (right side) ─────────────────────────────────
        Button newBtn    = new Button("📄 New");
        Button loadBtn   = new Button("📂 Load");
        Button saveBtn   = new Button("💾 Save");
        Button saveAsBtn = new Button("💾 Save As");

        newBtn.getStyleClass().addAll("action-btn", "file-btn");
        loadBtn.getStyleClass().addAll("action-btn", "file-btn");
        saveBtn.getStyleClass().addAll("action-btn", "file-btn");
        saveAsBtn.getStyleClass().addAll("action-btn", "file-btn");

        newBtn.setOnAction(e    -> showNewSimulationDialog());
        loadBtn.setOnAction(e   -> showLoadSimulationDialog());
        saveBtn.setOnAction(e   -> showSaveSimulationDialog());
        saveAsBtn.setOnAction(e -> showSaveAsSimulationDialog());

        this.getItems().addAll(
                selectBtn, deleteBtn, addNodeBtn, addEdgeBtn, addAgentBtn,
                new Separator(),
                randomGenBtn, randomAgentsBtn,
                spacer,
                new Separator(),
                newBtn, loadBtn, saveBtn, saveAsBtn);
    }

    // ── File-management dialogs ───────────────────────────────────────────────

    private void showSaveSimulationDialog() {
        Simulation sim = simController.getSimulation();
        if (sim == null) {
            showAlert("Erreur", "Aucune simulation active", "Veuillez charger ou créer une simulation d'abord.");
            return;
        }
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Sauvegarde");
        confirmDialog.setHeaderText("Sauvegarder la simulation");
        confirmDialog.setContentText("Sauvegarder la simulation : " + sim.getName() + " ?");
        Optional<ButtonType> result = confirmDialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            FileManager.saveSimulation(sim);
            showAlert("Succès", "Simulation sauvegardée", "La simulation a été sauvegardée avec succès.");
        }
    }

    private void showSaveAsSimulationDialog() {
        Simulation sim = simController.getSimulation();
        if (sim == null) {
            showAlert("Erreur", "Aucune simulation active", "Veuillez charger ou créer une simulation d'abord.");
            return;
        }
        TextInputDialog dialog = new TextInputDialog(sim.getName());
        dialog.setTitle("Sauvegarder sous…");
        dialog.setHeaderText("Enregistrer la simulation sous un nouveau nom");
        dialog.setContentText("Nom du fichier :");
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(name -> {
            String trimmed = name.trim();
            if (!trimmed.isEmpty()) {
                FileManager.saveSimulationAs(sim, trimmed);
                showAlert("Succès", "Simulation sauvegardée", "Sauvegardée sous : " + trimmed);
            }
        });
    }

    private void showLoadSimulationDialog() {
        List<String> simulations = FileManager.getAvailableSimulations();
        if (simulations.isEmpty()) {
            showAlert("Aucune simulation", "Pas de simulations disponibles",
                    "Veuillez créer une nouvelle simulation d'abord.");
            return;
        }
        ChoiceDialog<String> dialog = new ChoiceDialog<>(simulations.get(0), simulations);
        dialog.setTitle("Charger une simulation");
        dialog.setHeaderText("Sélectionnez une simulation à charger");
        dialog.setContentText("Simulations disponibles :");
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(simName -> {
            Simulation loaded = FileManager.loadSimulation(simName);
            if (loaded != null) {
                simController.loadSimulation(loaded);
                showAlert("Succès", "Simulation chargée", "La simulation " + simName + " a été chargée.");
            } else {
                showAlert("Erreur", "Erreur de chargement", "Impossible de charger : " + simName);
            }
        });
    }

    private void showNewSimulationDialog() {
        Dialog<SimulationParams> dialog = new Dialog<>();
        dialog.setTitle("Créer une nouvelle simulation");
        dialog.setHeaderText("Paramètres de la simulation");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField nameField = new TextField("NewSimulation");
        nameField.setPromptText("Nom de la simulation");

        Spinner<Integer> nodeSpinner  = new Spinner<>(1, 1000,  10);  nodeSpinner.setPrefWidth(150);
        Spinner<Integer> edgeSpinner  = new Spinner<>(1, 5000,  20);  edgeSpinner.setPrefWidth(150);
        Spinner<Integer> agentSpinner = new Spinner<>(1, 10000, 50); agentSpinner.setPrefWidth(150);

        grid.add(new Label("Nom :"),    0, 0); grid.add(nameField,    1, 0);
        grid.add(new Label("Nœuds :"), 0, 1); grid.add(nodeSpinner,  1, 1);
        grid.add(new Label("Arêtes :"),0, 2); grid.add(edgeSpinner,  1, 2);
        grid.add(new Label("Agents :"),0, 3); grid.add(agentSpinner, 1, 3);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.setResultConverter(btn -> btn == ButtonType.OK
                ? new SimulationParams(nameField.getText(), nodeSpinner.getValue(),
                        edgeSpinner.getValue(), agentSpinner.getValue())
                : null);

        dialog.showAndWait().ifPresent(params -> {
            try {
                Simulation newSim = new Simulation(params.name, params.nodes, params.edges,
                        params.agents, SimulationSettings.getInstance());
                simController.loadSimulation(newSim);
                showAlert("Succès", "Simulation créée", "La simulation " + params.name + " a été créée.");
            } catch (Exception ex) {
                showAlert("Erreur", "Erreur de création", "Impossible de créer la simulation : " + ex.getMessage());
                ex.printStackTrace();
            }
        });
    }

    private void showAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // ── Transfer object ───────────────────────────────────────────────────────

    private static class SimulationParams {
        final String name; final int nodes, edges, agents;
        SimulationParams(String name, int nodes, int edges, int agents) {
            this.name = name; this.nodes = nodes; this.edges = edges; this.agents = agents;
        }
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
