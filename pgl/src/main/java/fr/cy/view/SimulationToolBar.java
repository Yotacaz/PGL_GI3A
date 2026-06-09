package fr.cy.view;

import fr.cy.controller.SimulationController;
import fr.cy.util.FileManager;
import fr.cy.model.simulation.Simulation;
import fr.cy.model.simulation.SimulationSettings;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Dialog;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.GridPane;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.geometry.Point2D;
import javafx.geometry.Insets;
import javafx.stage.Popup;
import javafx.util.Duration;
import javafx.scene.control.Spinner;
import java.util.List;
import java.util.Optional;

/**
 * The {@code SimulationToolBar} class provides a graphical user interface
 * component
 * for controlling the execution and file management of a simulation.
 * <p>
 * It includes buttons to play, pause, reset, step, adjust simulation speed,
 * and manage simulation files (New, Load, Save).
 * </p>
 */
public class SimulationToolBar extends ToolBar {

    /** The controller managing the simulation logic and state. */
    private final SimulationController simController;

    /**
     * Constructs a new {@code SimulationToolBar}.
     *
     * @param simController The {@link SimulationController} to link UI actions with
     *                      simulation logic.
     */
    public SimulationToolBar(SimulationController simController) {
        this.simController = simController;
        this.getStyleClass().add("custom-toolbar");
        initToolBar();
    }

    /**
     * Initializes the toolbar UI components, sets up event handlers,
     * and constructs the popups for advanced controls.
     */
    private void initToolBar() {
        Button playBtn = new Button("▶ Play");
        Button pauseBtn = new Button("⏸ Pause");
        Button resetBtn = new Button("🔄 Reset");

        playBtn.getStyleClass().addAll("action-btn", "play-btn");
        pauseBtn.getStyleClass().addAll("action-btn", "pause-btn");
        resetBtn.getStyleClass().addAll("action-btn", "danger-btn");

        playBtn.setOnAction(e -> simController.play());
        pauseBtn.setOnAction(e -> simController.pause());
        resetBtn.setOnAction(e -> simController.reset());

        Separator sep1 = new Separator();

        Button stepBtn = new Button("⏭ Step");
        stepBtn.getStyleClass().addAll("action-btn", "step-btn");

        Label stepCountValue = new Label("+" + simController.getStepTicks());
        stepCountValue.getStyleClass().add("step-count-value");

        Label stepHint = new Label("Ticks/step");
        stepHint.getStyleClass().add("step-hint");

        Button stepMinusBtn = new Button("−100");
        Button stepPlusBtn = new Button("+100");
        stepMinusBtn.getStyleClass().addAll("action-btn", "step-adjust-btn");
        stepPlusBtn.getStyleClass().addAll("action-btn", "step-adjust-btn");

        HBox stepAdjustRow = new HBox(8, stepMinusBtn, stepCountValue, stepPlusBtn);
        stepAdjustRow.getStyleClass().add("step-adjust-row");

        VBox stepPopup = new VBox(8, stepHint, stepAdjustRow);
        stepPopup.getStyleClass().add("step-popup");
        Popup stepPopupWindow = new Popup();
        stepPopupWindow.setAutoHide(true);
        stepPopupWindow.getContent().add(stepPopup);

        FadeTransition showPopup = new FadeTransition(Duration.millis(120), stepPopup);
        showPopup.setFromValue(0);
        showPopup.setToValue(1);

        FadeTransition hidePopup = new FadeTransition(Duration.millis(90), stepPopup);
        hidePopup.setFromValue(1);
        hidePopup.setToValue(0);

        PauseTransition hideDelay = new PauseTransition(Duration.millis(120));
        hideDelay.setOnFinished(event -> {
            if (stepPopupWindow.isShowing()) {
                hidePopup.setOnFinished(e -> stepPopupWindow.hide());
                hidePopup.playFromStart();
            }
        });

        stepBtn.setOnMouseEntered(event -> {
            hideDelay.stop();
            if (!stepPopupWindow.isShowing()) {
                Point2D anchorPoint = stepBtn.localToScreen(0, 0);
                if (anchorPoint != null) {
                    stepPopupWindow.show(stepBtn, anchorPoint.getX(), anchorPoint.getY() + stepBtn.getHeight() + 8);
                }
                stepPopup.setOpacity(0);
                showPopup.playFromStart();
            }
        });
        stepBtn.setOnMouseExited(event -> hideDelay.playFromStart());
        stepPopup.setOnMouseEntered(event -> hideDelay.stop());
        stepPopup.setOnMouseExited(event -> hideDelay.playFromStart());

        stepBtn.setOnAction(e -> simController.stepTick());
        stepMinusBtn.setOnAction(e -> {
            int stepTicks = Math.max(1, simController.getStepTicks() - 100);
            simController.setStepTicks(stepTicks);
            stepCountValue.setText("+" + stepTicks);
        });
        stepPlusBtn.setOnAction(e -> {
            int stepTicks = Math.min(1000, simController.getStepTicks() + 100);
            simController.setStepTicks(stepTicks);
            stepCountValue.setText("+" + stepTicks);
        });

        Button decreaseSpeedBtn = new Button("⏪ -");
        Button increaseSpeedBtn = new Button("+ ⏩");
        Label speedLabel = new Label(formatSpeedMultiplier(simController.getSpeed()));

        decreaseSpeedBtn.getStyleClass().addAll("action-btn", "speed-btn");
        increaseSpeedBtn.getStyleClass().addAll("action-btn", "speed-btn");
        speedLabel.getStyleClass().add("speed-badge");

        decreaseSpeedBtn.setOnAction(e -> {
            simController.decreaseSpeed();
            speedLabel.setText(formatSpeedMultiplier(simController.getSpeed()));
        });
        increaseSpeedBtn.setOnAction(e -> {
            simController.increaseSpeed();
            speedLabel.setText(formatSpeedMultiplier(simController.getSpeed()));
        });

        Separator sep2 = new Separator();

        Button newBtn = new Button("📄 New");
        Button loadBtn = new Button("📂 Load");
        Button saveBtn = new Button("💾 Save");

        newBtn.getStyleClass().addAll("action-btn", "file-btn");
        loadBtn.getStyleClass().addAll("action-btn", "file-btn");
        saveBtn.getStyleClass().addAll("action-btn", "file-btn");

        newBtn.setOnAction(e -> showNewSimulationDialog());
        loadBtn.setOnAction(e -> showLoadSimulationDialog());
        saveBtn.setOnAction(e -> showSaveSimulationDialog());

        this.getItems().addAll(
                playBtn, pauseBtn, resetBtn, sep1,
                stepBtn, stepCountValue,
                decreaseSpeedBtn, speedLabel, increaseSpeedBtn,
                sep2,
                newBtn, loadBtn, saveBtn);
    }

    /**
     * Displays a confirmation dialog to save the current simulation state.
     */
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

    /**
     * Displays a selection dialog to load a previously saved simulation
     * from the {@link FileManager}.
     */
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
        dialog.setContentText("Simulations disponibles:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(simName -> {
            Simulation loaded = FileManager.loadSimulation(simName);
            if (loaded != null) {
                simController.loadSimulation(loaded);
                showAlert("Succès", "Simulation chargée", "La simulation " + simName + " a été chargée avec succès.");
            } else {
                showAlert("Erreur", "Erreur de chargement", "Impossible de charger la simulation " + simName + ".");
            }
        });
    }

    /**
     * Opens a form dialog to input parameters for creating a new simulation
     * (name, node count, edge count, agent count).
     */
    private void showNewSimulationDialog() {
        Dialog<SimulationParams> dialog = new Dialog<>();
        dialog.setTitle("Créer une nouvelle simulation");
        dialog.setHeaderText("Paramètres de la simulation");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField nameField = new TextField();
        nameField.setPromptText("Nom de la simulation");
        nameField.setText("NewSimulation");

        Spinner<Integer> nodeSpinner = new Spinner<>(1, 1000, 10);
        nodeSpinner.setPrefWidth(150);

        Spinner<Integer> edgeSpinner = new Spinner<>(1, 5000, 20);
        edgeSpinner.setPrefWidth(150);

        Spinner<Integer> agentSpinner = new Spinner<>(1, 10000, 50);
        agentSpinner.setPrefWidth(150);

        grid.add(new Label("Nom :"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Nœuds :"), 0, 1);
        grid.add(nodeSpinner, 1, 1);
        grid.add(new Label("Arêtes :"), 0, 2);
        grid.add(edgeSpinner, 1, 2);
        grid.add(new Label("Agents :"), 0, 3);
        grid.add(agentSpinner, 1, 3);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                return new SimulationParams(
                        nameField.getText(),
                        nodeSpinner.getValue(),
                        edgeSpinner.getValue(),
                        agentSpinner.getValue());
            }
            return null;
        });

        Optional<SimulationParams> result = dialog.showAndWait();
        result.ifPresent(params -> {
            try {
                Simulation newSim = new Simulation(params.name, params.nodes, params.edges, params.agents,
                        SimulationSettings.getInstance());
                simController.loadSimulation(newSim);
                showAlert("Succès", "Simulation créée", "La simulation " + params.name + " a été créée avec succès.");
            } catch (Exception ex) {
                showAlert("Erreur", "Erreur de création", "Impossible de créer la simulation : " + ex.getMessage());
                ex.printStackTrace();
            }
        });
    }

    /**
     * Displays an information alert to the user.
     *
     * @param title   The title of the alert window.
     * @param header  The header text displayed in the alert.
     * @param content The main message content.
     */
    private void showAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Data transfer object used to capture parameters from the 'New Simulation'
     * dialog.
     */
    private static class SimulationParams {
        String name;
        int nodes;
        int edges;
        int agents;

        SimulationParams(String name, int nodes, int edges, int agents) {
            this.name = name;
            this.nodes = nodes;
            this.edges = edges;
            this.agents = agents;
        }
    }

    /**
     * Formats the speed multiplier value for display in the UI.
     *
     * @param speedMultiplier The current speed multiplier.
     * @return A formatted String (e.g., "x1" or "x1,50").
     */
    private String formatSpeedMultiplier(double speedMultiplier) {
        if (Math.abs(speedMultiplier - Math.rint(speedMultiplier)) < 0.001) {
            return "x" + (int) Math.rint(speedMultiplier);
        }
        return String.format("x%.2f", speedMultiplier).replace(".", ",");
    }
}