package fr.cy.view;

import java.util.List;
import java.util.Optional;

import fr.cy.controller.SimulationController;
import fr.cy.model.simulation.Simulation;
import fr.cy.model.simulation.SimulationSettings;
import fr.cy.util.FileManager;
import fr.cy.util.ScenarioBuilder;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

/**
 * Barre de boutons regroupant toutes les actions de gestion de fichiers :
 * New, Load, Demo, Save et Save As.
 */
public class FileManagementBar extends HBox {

    private final SimulationController simController;

    /**
     * Constructs a new {@code FileManagementBar} with buttons for file operations.
     *
     * @param simController the simulation controller used to handle save/load actions
     */
    public FileManagementBar(SimulationController simController) {
        super(4);
        this.simController = simController;
        initButtons();
    }

    private void initButtons() {
        Button newBtn    = new Button("📄 New");
        Button loadBtn   = new Button("📂 Load");
        Button demoBtn   = new Button("🎬 Demo");
        Button saveBtn   = new Button("💾 Save");
        Button saveAsBtn = new Button("💾 Save As");

        newBtn.getStyleClass().addAll("action-btn", "file-btn");
        loadBtn.getStyleClass().addAll("action-btn", "file-btn");
        demoBtn.getStyleClass().addAll("action-btn", "file-btn");
        saveBtn.getStyleClass().addAll("action-btn", "file-btn");
        saveAsBtn.getStyleClass().addAll("action-btn", "file-btn");

        newBtn.setOnAction(e    -> showNewSimulationDialog());
        loadBtn.setOnAction(e   -> showLoadSimulationDialog());
        demoBtn.setOnAction(e   -> showLoadDemoDialog());
        saveBtn.setOnAction(e   -> showSaveSimulationDialog());
        saveAsBtn.setOnAction(e -> showSaveAsSimulationDialog());

        getChildren().addAll(newBtn, loadBtn, demoBtn, saveBtn, saveAsBtn);
    }

    // ── Dialogs ───────────────────────────────────────────────────────────────

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

    private void showLoadDemoDialog() {
        List<String> demos = List.of(
                "Labyrinthe en Flammes",
                "Test Ligne Droite",
                "Test Minimaliste 1 Arête",
                "Test Contournement",
                "Test Dilemme : Feu vs Congestion");

        ChoiceDialog<String> dialog = new ChoiceDialog<>(demos.get(0), demos);
        dialog.setTitle("Charger une démo");
        dialog.setHeaderText("Sélectionnez un scénario de démonstration");
        dialog.setContentText("Scénarios disponibles :");
        dialog.showAndWait().ifPresent(choice -> {
            Simulation demo = switch (choice) {
                case "Labyrinthe en Flammes"             -> ScenarioBuilder.buildComplexScenario();
                case "Test Ligne Droite"                 -> ScenarioBuilder.setupSimplePipelineTest();
                case "Test Minimaliste 1 Arête"          -> ScenarioBuilder.setupMinimalistTest();
                case "Test Contournement"                -> ScenarioBuilder.setupBypassTest();
                case "Test Dilemme : Feu vs Congestion"  -> ScenarioBuilder.setupFireDilemmaTest();
                default -> null;
            };
            if (demo != null) {
                simController.loadSimulation(demo);
            }
        });
    }

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
        dialog.showAndWait().ifPresent(name -> {
            String trimmed = name.trim();
            if (!trimmed.isEmpty()) {
                FileManager.saveSimulationAs(sim, trimmed);
                showAlert("Succès", "Simulation sauvegardée", "Sauvegardée sous : " + trimmed);
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

    // ── Objet de transfert pour la dialog New ─────────────────────────────────

    private static class SimulationParams {
        final String name;
        final int nodes, edges, agents;

        SimulationParams(String name, int nodes, int edges, int agents) {
            this.name = name;
            this.nodes = nodes;
            this.edges = edges;
            this.agents = agents;
        }
    }
}
