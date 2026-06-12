package fr.cy.view;

import fr.cy.controller.SimulationController;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.geometry.Point2D;
import javafx.stage.Popup;
import javafx.util.Duration;

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
        Button playPauseBtn = new Button("▶ Play");
        Button resetBtn = new Button("🔄 Reset");

        playPauseBtn.getStyleClass().addAll("action-btn", "play-btn");
        resetBtn.getStyleClass().addAll("action-btn", "danger-btn");

        playPauseBtn.setOnAction(e -> {
            if (simController.isRunning()) {
                simController.pause();
                playPauseBtn.setText("▶ Play");
                playPauseBtn.getStyleClass().removeAll("pause-btn");
                playPauseBtn.getStyleClass().add("play-btn");
            } else {
                boolean hasExits = !simController.getSimulation().getGraph().getExits().isEmpty();
                if (!hasExits) {
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    alert.setTitle("Aucune sortie définie");
                    alert.setHeaderText("La simulation n'a aucun nœud de sortie.");
                    alert.setContentText("Les agents ne pourront pas s'évacuer. Voulez-vous quand même lancer la simulation ?");
                    alert.showAndWait().filter(r -> r == ButtonType.OK).ifPresent(r -> {
                        simController.play();
                        playPauseBtn.setText("⏸ Pause");
                        playPauseBtn.getStyleClass().removeAll("play-btn");
                        playPauseBtn.getStyleClass().add("pause-btn");
                    });
                } else {
                    simController.play();
                    playPauseBtn.setText("⏸ Pause");
                    playPauseBtn.getStyleClass().removeAll("play-btn");
                    playPauseBtn.getStyleClass().add("pause-btn");
                }
            }
        });

        resetBtn.setOnAction(e -> {
            simController.reset();
            playPauseBtn.setText("▶ Play");
            playPauseBtn.getStyleClass().removeAll("pause-btn");
            playPauseBtn.getStyleClass().add("play-btn");
        });

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

        this.getItems().addAll(
                playPauseBtn, resetBtn, sep1,
                stepBtn, stepCountValue,
                decreaseSpeedBtn, speedLabel, increaseSpeedBtn);
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